"use client";

import React, { useEffect, useRef, useState } from "react";
import { Terminal } from "xterm";
import { FitAddon } from "@xterm/addon-fit";
import "xterm/css/xterm.css";
import { X } from "lucide-react";

interface KaliTerminalProps {
    instanceId: string;   // 🆕 REQUIRED
    sshPort: number;      // 🆕 REQUIRED
    onClose: () => void;
}

export default function KaliTerminal({ instanceId, sshPort, onClose }: KaliTerminalProps) {
    const terminalRef = useRef<HTMLDivElement>(null);
    const wsRef = useRef<WebSocket | null>(null);
    const [connectionStatus, setConnectionStatus] = useState<
        "connecting" | "connected" | "disconnected" | "error"
    >("connecting");

    useEffect(() => {
        if (!terminalRef.current) return;

        // Setup the terminal UI
        const term = new Terminal({
            cursorBlink: true,
            fontSize: 14,
            fontFamily: 'Menlo, Monaco, "Courier New", monospace',
            theme: {
                background: "#1e1e1e",
                foreground: "#00ff00",
                cursor: "#00ff00",
            },
        });

        const fitAddon = new FitAddon();
        term.loadAddon(fitAddon);

        term.open(terminalRef.current);
        fitAddon.fit();

        term.writeln("\x1b[1;32m╔════════════════════════════════════════╗\x1b[0m");
        term.writeln("\x1b[1;32m║   🐉 Connecting to Challenge Shell...  ║\x1b[0m");
        term.writeln("\x1b[1;32m╚════════════════════════════════════════╝\x1b[0m");
        term.writeln("");

        // 🧩 CONNECT TO TERMINAL GATEWAY VIA WEBSOCKET
        const wsUrl = `${process.env.NEXT_PUBLIC_TERMINAL_URL}/?instanceId=${instanceId}&sshPort=${sshPort}`;
        console.log("Connecting WS:", wsUrl);

        const ws = new WebSocket(wsUrl);
        wsRef.current = ws;

        ws.onopen = () => {
            setConnectionStatus("connected");
            term.writeln("\x1b[1;32m✓ Connected to Challenge Instance!\x1b[0m\n");
        };

        ws.onmessage = (event) => {
            term.write(event.data);
        };

        ws.onerror = (err) => {
            console.error("WebSocket Error:", err);
            setConnectionStatus("error");
            term.writeln("\n\x1b[1;31m[WebSocket Error]\x1b[0m\n");
        };

        ws.onclose = () => {
            setConnectionStatus("disconnected");
            term.writeln("\n\x1b[1;31m[Connection Closed]\x1b[0m\n");
        };

        // Send user input → WebSocket → Node → SSH
        term.onData((data) => {
            if (ws.readyState === WebSocket.OPEN) {
                ws.send(data);
            }
        });

        const handleResize = () => fitAddon.fit();
        window.addEventListener("resize", handleResize);

        return () => {
            window.removeEventListener("resize", handleResize);
            ws.close();
            term.dispose();
        };
    }, [instanceId, sshPort]);

    return (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
            <div className="bg-card border border-border rounded-lg shadow-2xl w-full max-w-6xl h-[80vh] flex flex-col">
                {/* Header */}
                <div className="flex items-center justify-between p-4 border-b border-border">
                    <div>
                        <h2 className="text-lg font-semibold">Challenge Terminal</h2>
                        <p className="text-sm text-muted-foreground">
                            Status:{" "}
                            <span
                                className={
                                    connectionStatus === "connected"
                                        ? "text-green-500"
                                        : connectionStatus === "connecting"
                                            ? "text-yellow-500"
                                            : "text-red-500"
                                }
                            >
                                {connectionStatus.toUpperCase()}
                            </span>
                        </p>
                    </div>

                    <button
                        onClick={onClose}
                        className="p-2 hover:bg-muted rounded-lg"
                        aria-label="Close terminal"
                    >
                        <X className="w-5 h-5" />
                    </button>
                </div>

                {/* Terminal */}
                <div className="flex-1 p-4 overflow-hidden">
                    <div
                        ref={terminalRef}
                        className="w-full h-full rounded-lg"
                        style={{ background: "#1e1e1e" }}
                    />
                </div>

                {/* Footer */}
                <div className="p-4 border-t border-border text-xs text-muted-foreground bg-muted/50">
                    Tools: bash, python3, curl, wget, nmap, netcat
                </div>
            </div>
        </div>
    );
}
