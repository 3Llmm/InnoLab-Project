import React, { useEffect, useRef } from "react";
import { Terminal } from "xterm";
import "xterm/css/xterm.css";

interface TerminalProps {
    instanceId: string;
    backendUrl: string; // e.g. "wss://ctf.example.com"
}

const TerminalComponent: React.FC<TerminalProps> = ({ instanceId, backendUrl }) => {
    const terminalRef = useRef<HTMLDivElement>(null);
    const wsRef = useRef<WebSocket | null>(null);

    useEffect(() => {
        const term = new Terminal({
            cursorBlink: true,
            theme: {
                background: "#1e1e1e",
                foreground: "#dcdcdc",
            },
        });

        term.open(terminalRef.current!);

        // Connect WebSocket to backend
        const ws = new WebSocket(`${backendUrl}/ws/terminal/${instanceId}`);
        wsRef.current = ws;

        ws.onopen = () => term.write("Connected to challenge shell...\r\n");
        ws.onmessage = (msg) => term.write(msg.data);
        ws.onerror = () => term.write("\r\n[WebSocket Error]\r\n");

        term.onData((data) => {
            if (ws.readyState === WebSocket.OPEN) {
                ws.send(data);
            }
        });

        return () => {
            term.dispose();
            ws.close();
        };
    }, [instanceId, backendUrl]);

    return (
        <div
            ref={terminalRef}
            style={{
                width: "100%",
                height: "400px",
                backgroundColor: "#1e1e1e",
                borderRadius: "8px",
            }}
        />
    );
};

export default TerminalComponent;
