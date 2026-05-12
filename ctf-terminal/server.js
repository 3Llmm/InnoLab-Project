// ctf-terminal/server.js
require("dotenv").config();
const express = require("express");
const WebSocket = require("ws");
const http = require("http");
const { Client } = require('ssh2');
const net = require("net");
const jwt = require("jsonwebtoken");

const app = express();
const server = http.createServer(app);
const wss = new WebSocket.Server({ server });

const JWT_SECRET = process.env.JWT_SECRET;
if (!JWT_SECRET) {
    console.error("FATAL: JWT_SECRET environment variable is required");
    process.exit(1);
}

const ALLOWED_ORIGINS = (process.env.ALLOWED_ORIGINS || "http://localhost:3000,http://127.0.0.1:3000")
    .split(",")
    .map(s => s.trim())
    .filter(Boolean);

const AUTH_TIMEOUT_MS = parseInt(process.env.AUTH_TIMEOUT_MS || "15000", 10);
const MAX_CONNECTIONS_PER_INSTANCE = parseInt(process.env.MAX_CONNECTIONS_PER_INSTANCE || "1", 10);

// Track active connections per instanceId
const instanceConnections = new Map();

function verifyToken(token) {
    try {
        const decoded = jwt.verify(token, JWT_SECRET);
        if (decoded.type !== "terminal") {
            return null;
        }
        return decoded;
    } catch (err) {
        return null;
    }
}

function isOriginAllowed(origin) {
    if (!origin) return true; // Allow non-browser clients
    if (ALLOWED_ORIGINS.includes("*")) return true;
    return ALLOWED_ORIGINS.some(allowed =>
        origin === allowed || origin.startsWith(allowed + "/") || origin.startsWith(allowed.replace(/\/$/, ""))
    );
}

// Basic CORS for HTTP endpoints
app.use((req, res, next) => {
    res.setHeader("Access-Control-Allow-Origin", "*");
    next();
});

// Health check
app.get("/health", (req, res) => {
    res.json({
        status: "ok",
        connections: wss.clients.size
    });
});

// Helper to check if SSH port is actually responding
async function checkSSHPort(host, port, timeout = 10000) {
    return new Promise((resolve) => {
        const socket = net.createConnection({
            host,
            port,
            timeout,
            lookup: (hostname, options, callback) => {
                require('dns').lookup(hostname, options, callback);
            }
        });

        socket.setTimeout(timeout);

        socket.on('connect', () => {
            socket.destroy();
            console.log(`[SSH Check]  Port ${port} on ${host} is reachable`);
            resolve(true);
        });

        socket.on('timeout', () => {
            console.log(`[SSH Check] Timeout connecting to ${host}:${port}`);
            socket.destroy();
            resolve(false);
        });

        socket.on('error', (err) => {
            console.log(`[SSH Check]  Error connecting to ${host}:${port}: ${err.code}`);
            resolve(false);
        });
    });
}

// Helper to wait for SSH to be ready with exponential backoff
async function waitForSSH(host, port, maxAttempts = 12, baseDelay = 2000) {
    for (let attempt = 1; attempt <= maxAttempts; attempt++) {
        console.log(`[SSH Check] Attempt ${attempt}/${maxAttempts} for ${host}:${port}`);

        const isReady = await checkSSHPort(host, port);

        if (isReady) {
            console.log(`[SSH Check]  SSH is ready on ${host}:${port}`);
            return true;
        }

        if (attempt < maxAttempts) {
            const delay = Math.min(baseDelay * Math.pow(1.5, attempt - 1), 10000);
            console.log(`[SSH Check] Not ready yet, waiting ${Math.round(delay)}ms...`);
            await new Promise(resolve => setTimeout(resolve, delay));
        }
    }

    console.log(`[SSH Check]  SSH failed to become ready after ${maxAttempts} attempts`);
    return false;
}

// Helper to connect SSH with retry
async function connectSSHWithRetry(config, maxRetries = 3) {
    for (let attempt = 1; attempt <= maxRetries; attempt++) {
        try {
            const conn = new Client();

            await new Promise((resolve, reject) => {
                const timeout = setTimeout(() => {
                    conn.removeAllListeners();
                    conn.end();
                    reject(new Error('SSH handshake timeout'));
                }, 10000);

                conn.once('ready', () => {
                    clearTimeout(timeout);
                    resolve();
                });

                conn.once('error', (err) => {
                    clearTimeout(timeout);
                    console.error(`[${config.instanceId}] SSH Error:`, err.message);
                    console.error(`[${config.instanceId}] SSH Error Code:`, err.code);
                    console.error(`[${config.instanceId}] SSH Error Level:`, err.level);
                    reject(err);
                });

                conn.connect(config);
            });

            return conn;

        } catch (err) {
            console.error(`[${config.instanceId}] SSH attempt ${attempt}/${maxRetries} FAILED:`, err.message);

            if (attempt === maxRetries) {
                throw err;
            }

            await new Promise(resolve => setTimeout(resolve, 2000));
        }
    }
}

// Extract SSH target from JWT claims or fall back to URL params
function resolveSshTarget(decoded, url) {
    const fromClaims = {
        containerName: decoded.containerName || null,
        sshPort: decoded.sshPort || null,
        instanceId: decoded.instanceId || null
    };

    const fromUrl = {
        containerName: url.searchParams.get("containerName"),
        sshPort: url.searchParams.get("sshPort"),
        instanceId: url.searchParams.get("instanceId")
    };

    // JWT claims take precedence over URL params
    const containerName = fromClaims.containerName || fromUrl.containerName;
    const sshPort = fromClaims.sshPort || fromUrl.sshPort;
    const instanceId = fromClaims.instanceId || fromUrl.instanceId;

    // Validate instanceId match between JWT and URL
    if (fromClaims.instanceId && fromUrl.instanceId && fromClaims.instanceId !== fromUrl.instanceId) {
        return null;
    }

    const sshHost = sshPort ? '127.0.0.1' : containerName;
    const sshPortNum = sshPort ? parseInt(sshPort, 10) : 22;

    return { containerName, sshPort: sshPortNum, sshHost, instanceId };
}

// Perform the full SSH connection flow after authentication
async function handleAuthenticatedConnection(ws, decoded, url) {
    const target = resolveSshTarget(decoded, url);
    if (!target || !target.containerName) {
        ws.send("\r\n\x1b[1;31m Invalid connection target\r\n\x1b[0m\r\n");
        ws.close(1008, "Invalid connection target");
        return;
    }

    const { containerName, sshHost, sshPort: sshPortNum, instanceId } = target;

    console.log(`[${instanceId}] Authenticated connection → Container: ${containerName}, SSH: ${sshHost}:${sshPortNum}`);

    // Enforce max connections per instance
    const currentCount = instanceConnections.get(instanceId) || 0;
    if (currentCount >= MAX_CONNECTIONS_PER_INSTANCE) {
        ws.send(`\r\n\x1b[1;31m Maximum connections reached for this instance\x1b[0m\r\n`);
        ws.close(1008, "Too many connections");
        return;
    }
    instanceConnections.set(instanceId, currentCount + 1);

    const cleanup = () => {
        const count = instanceConnections.get(instanceId) || 1;
        if (count <= 1) {
            instanceConnections.delete(instanceId);
        } else {
            instanceConnections.set(instanceId, count - 1);
        }
    };

    ws.send(`\r\n\x1b[1;36m Waiting for SSH service to start...\x1b[0m\r\n`);

    const sshReady = await waitForSSH(sshHost, sshPortNum);

    if (!sshReady) {
        ws.send(`\r\n\x1b[1;31m SSH service failed to start\x1b[0m\r\n`);
        ws.send(`\x1b[1;33mPlease try again in a moment or contact support\x1b[0m\r\n`);
        cleanup();
        ws.close();
        return;
    }

    ws.send(`\r\n\x1b[1;32m SSH service is ready!\x1b[0m\r\n`);
    ws.send(`\x1b[1;36m Establishing secure connection...\x1b[0m\r\n`);

    let conn;
    let shell = null;

    try {
        conn = await connectSSHWithRetry({
            host: sshHost,
            port: sshPortNum,
            username: 'ctfuser',
            password: 'ctfpassword',
            readyTimeout: 10000,
            tryKeyboard: true,
            instanceId: instanceId
        });
    } catch (err) {
        console.error(`[${instanceId}] All SSH connection attempts failed:`, err.message);
        if (ws.readyState === WebSocket.OPEN) {
            ws.send(`\r\n\x1b[1;31m Failed to establish SSH connection\x1b[0m\r\n`);
            ws.send(`\x1b[1;33mError: ${err.message}\x1b[0m\r\n`);
            ws.close();
        }
        cleanup();
        return;
    }

    console.log(`[${instanceId}] SSH connected successfully to ${containerName}`);
    console.log(`[${instanceId}] Requesting shell...`);

    conn.shell({ term: 'xterm-256color' }, (err, stream) => {
        if (err) {
            console.error(`[${instanceId}] Failed to create shell:`, err.message);
            ws.send(`\r\n\x1b[1;31m Error creating shell: ${err.message}\x1b[0m\r\n`);
            conn.end();
            ws.close();
            cleanup();
            return;
        }

        console.log(`[${instanceId}] Shell created successfully`);
        shell = stream;

        stream.on('data', (data) => {
            if (ws.readyState === WebSocket.OPEN) {
                ws.send(data);
            }
        });

        stream.on('close', () => {
            console.log(`[${instanceId}] Shell closed`);
            ws.close();
            conn.end();
            cleanup();
        });

        if (ws.readyState === WebSocket.OPEN) {
            ws.send(`\r\n\x1b[1;32m\x1b[0m\r\n`);
            ws.send(`\x1b[1;32m    Connected to ${containerName.padEnd(18)} \x1b[0m\r\n`);
            ws.send(`\x1b[1;32m\x1b[0m\r\n`);
            ws.send(`\r\n\x1b[1;33m You are now logged in as ctfuser\x1b[0m\r\n`);
            ws.send(`\x1b[1;33m Start exploring and find the flag!\x1b[0m\r\n\r\n`);
        }
    });

    conn.on('error', (err) => {
        console.error(`[${instanceId}] SSH error for ${containerName}:`, err.message);
        if (ws.readyState === WebSocket.OPEN) {
            ws.send(`\r\n\x1b[1;31m SSH Connection failed: ${err.message}\x1b[0m\r\n`);
            ws.close();
        }
        cleanup();
    });

    ws.on('message', (data) => {
        if (shell && shell.writable) {
            shell.write(data);
        }
    });

    ws.on('close', () => {
        console.log(`[${instanceId}] WebSocket closed for ${containerName}`);
        if (shell) shell.end();
        if (conn) conn.end();
        cleanup();
    });
}

// WebSocket connection handler
wss.on("connection", async (ws, req) => {
    const url = new URL(req.url, `http://${req.headers.host}`);
    const urlInstanceId = url.searchParams.get("instanceId") || "unknown";

    console.log(`[${urlInstanceId}] New WebSocket connection from ${req.socket.remoteAddress}`);

    // Origin validation
    const origin = req.headers.origin;
    if (origin && !isOriginAllowed(origin)) {
        console.warn(`[${urlInstanceId}] Blocked connection from disallowed origin: ${origin}`);
        ws.close(1008, "Origin not allowed");
        return;
    }

    const urlToken = url.searchParams.get("token");

    // If token is in URL (old protocol), handle it but log a warning
    if (urlToken) {
        console.warn(`[${urlInstanceId}] DEPRECATED: Token in URL. Update frontend to use auth message protocol.`);
        const decoded = verifyToken(urlToken);
        if (!decoded) {
            ws.send("Error: Invalid or expired token.\r\n");
            ws.close(1008, "Invalid token");
            return;
        }
        await handleAuthenticatedConnection(ws, decoded, url);
        return;
    }

    // New protocol: wait for auth message as first message
    let authHandled = false;
    const authTimeout = setTimeout(() => {
        if (!authHandled && ws.readyState === WebSocket.OPEN) {
            authHandled = true;
            console.warn(`[${urlInstanceId}] Authentication timeout`);
            ws.close(1008, "Authentication timeout");
        }
    }, AUTH_TIMEOUT_MS);

    ws.once('message', async (data) => {
        if (authHandled) return;
        authHandled = true;
        clearTimeout(authTimeout);

        try {
            const msg = JSON.parse(data.toString());
            if (!msg || msg.type !== 'auth' || !msg.token) {
                ws.send("Error: Authentication required. Send { type: \"auth\", token: \"...\" }\r\n");
                ws.close(1008, "Auth message expected");
                return;
            }

            const decoded = verifyToken(msg.token);
            if (!decoded) {
                ws.send("Error: Invalid or expired token.\r\n");
                ws.close(1008, "Invalid token");
                return;
            }

            await handleAuthenticatedConnection(ws, decoded, url);
        } catch (e) {
            ws.send("Error: Invalid auth message format. Send JSON { type: \"auth\", token: \"...\" }\r\n");
            ws.close(1008, "Invalid auth message");
        }
    });
});

const PORT = process.env.PORT || 3001;
server.listen(PORT, '0.0.0.0', () => {
    console.log(` CTF Terminal Gateway running on port ${PORT}`);
    console.log(` Listening on 0.0.0.0:${PORT}`);
    console.log(` Allowed origins: ${ALLOWED_ORIGINS.join(", ") || "(none - all origins allowed)"}`);
    console.log(` Auth timeout: ${AUTH_TIMEOUT_MS}ms`);
    console.log(` Max connections per instance: ${MAX_CONNECTIONS_PER_INSTANCE}`);
});