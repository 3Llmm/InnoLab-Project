const { spawn } = require('child_process');
const express = require('express');
const webSocket = require('ws');
const http = require('http');
const pty = require('node-pty');


const app = express();
const server = http.createServer(app);
const wss = new webSocket.Server({ server });

wss.on('connection', (ws) => {
    console.log('WebSocket connected');

    const docker = pty.spawn('docker', ['run', '-it', '--rm', 'ctf-kali-vm-tools', 'bash'], {
        name: 'xterm-color',
        cols: 80,
        rows: 30,
        cwd: process.env.HOME,
        env: process.env
    });


    docker.on('data', (data) => {
    console.log('STDOUT:', data.toString());
    ws.send(data.toString());
    });

  


    // Forward frontend input to container
    ws.on('message', (msg) => {
        docker.write(msg);
    });

 

    // Cleanup on disconnect
    ws.on('close', () => {
        docker.kill(); 
        console.log('Shell session ended');
    });
});

server.listen(3001, () => {
  console.log('Shell Gateway is listening on http://localhost:3001');
});
