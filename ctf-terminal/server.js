const express = require("express");
const webSocket = require("ws");
const http = require("http");
const pty = require("node-pty");

const app = express();
const server = http.createServer(app);
const wss = new webSocket.Server({ server });

// Allow frontend access
app.use((req, res, next) => {
    res.setHeader("Access-Control-Allow-Origin", "*");
    next();
});

// Health check
app.get("/health", (req, res) => {
    res.json({ status: "ok", activeConnections: wss.clients.size });
});

wss.on("connection", (ws, req) => {
    const clientIp = req.socket.remoteAddress;

    // Extract query params
    const paramsString = req.url.includes("?") ? req.url.split("?")[1] : "";
    const params = new URLSearchParams(paramsString);

    const instanceId = params.get("instanceId");
    const sshPort = params.get("sshPort");

    console.log(`🔌 WebSocket connected from ${clientIp}`);
    console.log(`📦 Instance ID: ${instanceId}`);
    console.log(`🔐 SSH Port: ${sshPort}`);

    if (!sshPort) {
        ws.send("Error: Missing SSH port");
        ws.close();
        return;
    }

    // ✨ Start SSH session using node-pty
    const ssh = pty.spawn(
        "ssh",
        [
            `ctf-user@host.docker.internal`, // Or your Docker host IP
            "-p",
            sshPort,
            "-o", "StrictHostKeyChecking=no",
            "-o", "UserKnownHostsFile=/dev/null"
        ],
        {
            name: "xterm-color",
            cols: 80,
            rows: 30,
            cwd: process.env.HOME,
            env: process.env
        }
    );

    console.log("🚀 SSH session started");

    // Pipe SSH output → WebSocket
    ssh.on("data", (data) => {
        if (ws.readyState === ws.OPEN) {
            ws.send(data.toString());
        }
    });

    // If SSH exits, close WebSocket
    ssh.on("exit", (code) => {
        console.log(`💀 SSH process exited with code: ${code}`);
        if (ws.readyState === ws.OPEN) ws.close();
    });

    // Pipe WebSocket input → SSH
    ws.on("message", (msg) => {
        ssh.write(msg);
    });

    // Cleanup websocket close
    ws.on("close", () => {
        console.log(`🔌 WebSocket disconnected from ${clientIp}`);
        ssh.kill();
    });

    ws.on("error", (error) => {
        console.error(`❌ WebSocket error: ${error.message}`);
        ssh.kill();
    });
});

server.listen(3001, () => {
    console.log("🚀 Terminal Gateway running at http://localhost:3001");
    console.log("🔍 Health check: http://localhost:3001/health");
});
