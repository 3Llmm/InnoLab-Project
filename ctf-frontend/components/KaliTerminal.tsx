"use client";

import React, { useEffect, useRef, useState } from "react";
import { Terminal } from "xterm";
import { FitAddon } from "@xterm/addon-fit";
import "xterm/css/xterm.css";
import { X } from "lucide-react";

interface KaliTerminalProps {
    instanceId?: string;
    sshPort?: number;
    containerName?: string;
    onClose: () => void;
}
export default function KaliTerminal({ instanceId, sshPort, containerName, onClose }: KaliTerminalProps) {
    const terminalRef = useRef<HTMLDivElement>(null);
    const wsRef = useRef<WebSocket | null>(null);
    const terminalInstanceRef = useRef<Terminal | null>(null);
    const fitAddonRef = useRef<FitAddon | null>(null);
    const isInitializedRef = useRef(false); // Prevent double initialization
    const [connectionStatus, setConnectionStatus] = useState<
        "connecting" | "connected" | "disconnected" | "error" | "missing_props"
    >("connecting");
    const [debugInfo, setDebugInfo] = useState<string>("");

    const addDebugInfo = (info: string) => {
        console.log(`[DEBUG] ${info}`);
        setDebugInfo(prev => `${new Date().toLocaleTimeString()}: ${info}\n${prev}`);
    };

    useEffect(() => {
        // Validation
        if (!sshPort || !instanceId) {
            setConnectionStatus("missing_props");
            return;
        }

        if (!terminalRef.current) {
            return;
        }

        // CRITICAL: Prevent React Strict Mode double initialization
        if (isInitializedRef.current) {
            addDebugInfo(" Already initialized, preventing duplicate");
            return;
        }

        isInitializedRef.current = true;
        addDebugInfo(" Starting initialization...");

        try {
            // Initialize terminal
            const terminal = new Terminal({
                cursorBlink: true,
                fontSize: 14,
                fontFamily: 'Consolas, "Courier New", monospace',
                theme: {
                    background: "#1e1e1e",
                    foreground: "#00ff00",
                    cursor: "#00ff00",
                },
                cols: 80,
                rows: 24,
                allowTransparency: false,
                convertEol: true,
            });

            const fitAddon = new FitAddon();
            terminal.loadAddon(fitAddon);
            
            terminalInstanceRef.current = terminal;
            fitAddonRef.current = fitAddon;

            terminal.open(terminalRef.current);
            addDebugInfo(" Terminal opened");

            // Welcome message
            terminal.writeln("\r\n\x1b[1;32m\x1b[0m");
            terminal.writeln("\x1b[1;32m    CTF Challenge Terminal             \x1b[0m");
            terminal.writeln("\x1b[1;32m\x1b[0m");
            terminal.writeln("");
            terminal.writeln(`Instance: ${instanceId}`);
            terminal.writeln(`SSH Port: ${sshPort}`);
            terminal.writeln("");

            // Fit and focus
            setTimeout(() => {
                try {
                    fitAddon.fit();
                    terminal.focus();
                    addDebugInfo(" Terminal fitted and focused");
                } catch (error) {
                    addDebugInfo(` Fit error: ${error}`);
                }
            }, 100);

            // WebSocket connection
            const terminalUrl = process.env.NEXT_PUBLIC_TERMINAL_URL || "ws://localhost:3001";
            const containerName = `ctf-${instanceId.substring(0, 8)}`;
            const wsUrl = `${terminalUrl}/?instanceId=${instanceId}&containerName=${containerName}`;
            
            addDebugInfo(` Connecting WebSocket: ${wsUrl}`);
            terminal.writeln(`Connecting to: ${terminalUrl}`);
            terminal.writeln("");

            const ws = new WebSocket(wsUrl);
            ws.binaryType = 'arraybuffer'; // Handle binary data properly
            wsRef.current = ws;

            ws.onopen = () => {
                addDebugInfo(" WebSocket OPEN");
                setConnectionStatus("connected");
                terminal.writeln("\r\n\x1b[1;32m Connected!\x1b[0m");
                terminal.writeln("\x1b[1;33m Establishing SSH...\x1b[0m");
                terminal.writeln("");
                
                setTimeout(() => {
                    terminal.focus();
                    addDebugInfo(" Refocused");
                }, 500);
            };

            ws.onmessage = (event) => {
                try {
                    let data = event.data;
                    
                    // Handle different data types
                    if (data instanceof ArrayBuffer) {
                        // Convert ArrayBuffer to Uint8Array
                        const uint8Array = new Uint8Array(data);
                        addDebugInfo(` ArrayBuffer: ${uint8Array.length} bytes`);
                        terminal.write(uint8Array);
                    } else if (typeof data === 'string') {
                        addDebugInfo(` String: ${data.length} chars`);
                        terminal.write(data);
                    } else if (data instanceof Blob) {
                        // Handle Blob (shouldn't happen with arraybuffer type, but just in case)
                        addDebugInfo(` Blob: ${data.size} bytes (converting...)`);
                        data.arrayBuffer().then(buffer => {
                            terminal.write(new Uint8Array(buffer));
                        });
                    } else {
                        addDebugInfo(` Unknown data type: ${typeof data}`);
                        console.log('Raw data:', data);
                    }
                } catch (error) {
                    addDebugInfo(` Write error: ${error}`);
                    console.error('Message handler error:', error);
                }
            };

            ws.onerror = (error) => {
                addDebugInfo(` WebSocket error`);
                setConnectionStatus("error");
                terminal.writeln("\r\n\x1b[1;31m Connection Failed\x1b[0m");
            };

            ws.onclose = (event) => {
                addDebugInfo(` WebSocket closed: ${event.code} - ${event.reason}`);
                setConnectionStatus("disconnected");
                
                if (event.code !== 1000) {
                    terminal.writeln(`\r\n\x1b[1;33m Disconnected (${event.code})\x1b[0m`);
                }
            };

            // Terminal input handler
            const disposable = terminal.onData((data) => {
                if (ws.readyState === WebSocket.OPEN) {
                    try {
                        ws.send(data);
                        addDebugInfo(` Sent: char ${data.charCodeAt(0)}`);
                    } catch (error) {
                        addDebugInfo(` Send error: ${error}`);
                    }
                } else {
                    addDebugInfo(` WS not open: ${ws.readyState}`);
                    terminal.write('\r\n\x1b[1;31m Not connected\x1b[0m\r\n');
                }
            });

            // Cleanup
            return () => {
                addDebugInfo(" Cleanup triggered");
                
                disposable.dispose();
                
                if (ws.readyState === WebSocket.OPEN || ws.readyState === WebSocket.CONNECTING) {
                    ws.close(1000, "Unmounting");
                }
                
                try {
                    terminal.dispose();
                } catch (error) {
                    console.error("Dispose error:", error);
                }
                
                terminalInstanceRef.current = null;
                wsRef.current = null;
                isInitializedRef.current = false; // Reset for remount
            };

        } catch (error) {
            addDebugInfo(` Init error: ${error}`);
            setConnectionStatus("error");
        }
    }, [instanceId, sshPort]);

    // Resize handler
    useEffect(() => {
        const handleResize = () => {
            try {
                if (fitAddonRef.current && terminalRef.current) {
                    fitAddonRef.current.fit();
                }
            } catch (error) {
                // Ignore
            }
        };
        
        window.addEventListener("resize", handleResize);
        return () => window.removeEventListener("resize", handleResize);
    }, []);

    const handleTerminalClick = () => {
        if (terminalInstanceRef.current) {
            terminalInstanceRef.current.focus();
            addDebugInfo(' Clicked & focused');
        }
    };

    const getStatusColor = () => {
        switch (connectionStatus) {
            case "connected": return "text-green-500";
            case "connecting": return "text-yellow-500";
            case "error": return "text-red-500";
            case "missing_props": return "text-orange-500";
            default: return "text-gray-500";
        }
    };

    const getStatusMessage = () => {
        switch (connectionStatus) {
            case "connected": return " CONNECTED - Ready";
            case "connecting": return " CONNECTING...";
            case "error": return " ERROR";
            case "missing_props": return " MISSING DATA";
            default: return " DISCONNECTED";
        }
    };

    if (!sshPort || !instanceId) {
        return (
            <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
                <div className="bg-card border border-border rounded-lg shadow-2xl w-full max-w-md p-6">
                    <h2 className="text-lg font-semibold text-red-500 mb-4">Terminal Error</h2>
                    <p className="text-muted-foreground mb-4">
                        Missing instance data.
                    </p>
                    <div className="text-sm bg-muted p-3 rounded mb-4">
                        <p><strong>Instance ID:</strong> {instanceId || "undefined"}</p>
                        <p><strong>SSH Port:</strong> {sshPort || "undefined"}</p>
                    </div>
                    <button
                        onClick={onClose}
                        className="w-full bg-primary text-primary-foreground py-2 rounded-lg hover:bg-primary/90"
                    >
                        Close
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="fixed inset-0 bg-black/80 backdrop-blur-sm z-50 flex items-center justify-center p-4">
            <div className="bg-card border border-border rounded-lg shadow-2xl w-full max-w-6xl h-[90vh] flex flex-col">
                <div className="flex items-center justify-between p-4 border-b border-border bg-muted/50">
                    <div className="flex-1">
                        <h2 className="text-lg font-semibold">Challenge Terminal</h2>
                            <div className="flex items-center gap-4 text-sm text-muted-foreground">
                                <div>
                                    Status: <span className={getStatusColor()}>{getStatusMessage()}</span>
                                </div>
                                <div>Instance: {instanceId}</div>
                                <div>Container: {`ctf-${instanceId}`}</div>
                            </div>
                    </div>
                    <button onClick={onClose} className="p-2 hover:bg-muted rounded-lg">
                        <X className="w-5 h-5" />
                    </button>
                </div>

                <div className="flex-1 p-4 overflow-hidden flex flex-col">
                    <div 
                        ref={terminalRef}
                        className="w-full h-full rounded-lg border border-border cursor-text"
                        style={{ background: "#1e1e1e" }}
                        onClick={handleTerminalClick}
                        onMouseDown={handleTerminalClick}
                    />
                    
                    <details className="mt-2 text-xs">
                        <summary className="cursor-pointer text-muted-foreground hover:text-foreground p-2 bg-muted rounded">
                             Debug Info
                        </summary>
                        <pre className="mt-1 p-2 bg-muted rounded text-xs max-h-32 overflow-auto whitespace-pre-wrap">
                            {debugInfo || "No debug info..."}
                        </pre>
                    </details>
                </div>

                <div className="p-3 border-t border-border text-xs text-muted-foreground bg-muted/30">
                    <div className="flex justify-between items-center">
                        <div><strong>Credentials:</strong> ctfuser / ctfpassword</div>
                        <div> Click terminal to type</div>
                    </div>
                </div>
            </div>
        </div>
    );
}