const express = require("express");
const WebSocket = require("ws");
const http = require("http");
const { Client } = require('ssh2');

const app = express();
const server = http.createServer(app);
const wss = new WebSocket.Server({ server });

app.use((req, res, next) => {
    res.setHeader("Access-Control-Allow-Origin", "*");
    res.setHeader("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
    res.setHeader("Access-Control-Allow-Headers", "Content-Type");
    next();
});

app.get("/health", (req, res) => {
    res.json({ 
        status: "ok", 
        activeConnections: wss.clients.size,
        message: "CTF Terminal Gateway is running"
    });
});

wss.on("connection", (ws, req) => {
    const clientIp = req.socket.remoteAddress;
    const url = new URL(req.url, `http://${req.headers.host}`);
    const sshPort = url.searchParams.get("sshPort");
    const instanceId = url.searchParams.get("instanceId");

    console.log(`\n${'='.repeat(60)}`);
    console.log(`🔌 NEW WebSocket Connection`);
    console.log(`   Client IP: ${clientIp}`);
    console.log(`   Instance: ${instanceId}`);
    console.log(`   SSH Port: ${sshPort}`);
    console.log(`${'='.repeat(60)}\n`);

    // Validate parameters
    if (!sshPort) {
        console.error("❌ Missing SSH port parameter");
        ws.send("❌ Error: Missing SSH port parameter\r\n");
        ws.close();
        return;
    }

    const portNumber = parseInt(sshPort);
    if (isNaN(portNumber) || portNumber < 1 || portNumber > 65535) {
        console.error(`❌ Invalid SSH port: ${sshPort}`);
        ws.send(`❌ Error: Invalid SSH port: ${sshPort}\r\n`);
        ws.close();
        return;
    }

    console.log(`✅ Valid parameters - Preparing SSH connection to port ${portNumber}`);

    // Create SSH connection
    const conn = new Client();
    let shellStream = null;

    conn.on('ready', () => {
        console.log('✅ SSH Client Ready - Starting shell...');
        ws.send("✅ SSH connection established\r\n");
        
        conn.shell({
            term: 'xterm-256color',
            cols: 80,
            rows: 24
        }, (err, stream) => {
            if (err) {
                console.error('❌ SSH Shell error:', err);
                ws.send(`❌ SSH Shell error: ${err.message}\r\n`);
                conn.end();
                return;
            }

            shellStream = stream;
            console.log('✅ SSH Shell started successfully');
            ws.send("🔐 Starting shell session...\r\n");

            // Stream data from SSH → WebSocket
            stream.on('data', (data) => {
                const preview = data.toString().substring(0, 50).replace(/\n/g, '\\n');
                console.log(`📥 SSH → WS: ${data.length} bytes - "${preview}"`);
                if (ws.readyState === WebSocket.OPEN) {
                    try {
                        ws.send(data);
                        console.log(`   ✅ Sent to WebSocket`);
                    } catch (err) {
                        console.error(`   ❌ Failed to send: ${err.message}`);
                    }
                } else {
                    console.error(`   ❌ WebSocket not open (state: ${ws.readyState})`);
                }
            });

            stream.on('close', () => {
                console.log('🔌 SSH Shell stream closed');
                if (ws.readyState === WebSocket.OPEN) {
                    ws.send("\r\n🔌 Shell session closed\r\n");
                    ws.close();
                }
                conn.end();
            });

            stream.stderr.on('data', (data) => {
                console.log(`📥 SSH STDERR → WebSocket: ${data.length} bytes`);
                if (ws.readyState === WebSocket.OPEN) {
                    ws.send(data);
                }
            });
        });
    });

    conn.on('error', (err) => {
        console.error('❌ SSH Connection error:', err.message);
        console.error('   Error details:', {
            code: err.code,
            level: err.level,
            description: err.description
        });
        if (ws.readyState === WebSocket.OPEN) {
            ws.send(`❌ SSH Error: ${err.message}\r\n`);
            ws.send(`   Check if SSH is running on port ${portNumber}\r\n`);
            ws.close();
        }
    });

    conn.on('close', (hadError) => {
        console.log(`🔌 SSH Connection closed, Had error: ${hadError}`);
        if (ws.readyState === WebSocket.OPEN && hadError) {
            ws.send("\r\n🔌 SSH connection closed\r\n");
            ws.close();
        }
    });

    // WebSocket messages → SSH stream
    ws.on('message', (message) => {
        const char = message.toString();
        console.log(`📤 WebSocket → SSH: "${char}" (code: ${char.charCodeAt(0)})`);
        
        if (shellStream && shellStream.writable) {
            shellStream.write(message);
            console.log(`   ✅ Written to SSH stream`);
        } else {
            console.error(`   ❌ Shell stream not ready or not writable`);
            console.error(`   Stream state: ${shellStream ? 'exists' : 'null'}, writable: ${shellStream?.writable}`);
            ws.send("\r\n❌ Shell not ready\r\n");
        }
    });

    // Handle WebSocket close
    ws.on('close', () => {
        console.log(`🔌 WebSocket closed for instance ${instanceId}`);
        if (shellStream) {
            shellStream.end();
        }
        conn.end();
    });

    ws.on('error', (error) => {
        console.error(`❌ WebSocket error: ${error.message}`);
        if (shellStream) {
            shellStream.end();
        }
        conn.end();
    });

    // Connect to SSH server
    console.log(`🔐 Initiating SSH connection...`);
    console.log(`   Target: localhost:${portNumber}`);
    console.log(`   User: ctfuser`);
    
    conn.connect({
        host: 'localhost',
        port: portNumber,
        username: 'ctfuser',
        password: 'ctfpassword',
        readyTimeout: 20000,
        debug: (info) => {
            console.log(`   [SSH DEBUG] ${info}`);
        },
        algorithms: {
            kex: [
                'ecdh-sha2-nistp256',
                'ecdh-sha2-nistp384',
                'ecdh-sha2-nistp521',
                'diffie-hellman-group14-sha256',
                'diffie-hellman-group14-sha1'
            ],
            cipher: [
                'aes128-ctr',
                'aes192-ctr',
                'aes256-ctr',
                'aes128-gcm',
                'aes128-gcm@openssh.com',
                'aes256-gcm',
                'aes256-gcm@openssh.com'
            ]
        }
    });
});

const PORT = process.env.PORT || 3001;
server.listen(PORT, () => {
    console.log(`\n${'='.repeat(60)}`);
    console.log(`🚀 CTF Terminal Gateway Started`);
    console.log(`   Port: ${PORT}`);
    console.log(`   Health: http://localhost:${PORT}/health`);
    console.log(`   WebSocket: ws://localhost:${PORT}`);
    console.log(`${'='.repeat(60)}\n`);
});