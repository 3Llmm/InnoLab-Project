/*
package at.fhtw.ctfbackend.config;

import at.fhtw.ctfbackend.entity.ChallengeInstanceEntity;
import at.fhtw.ctfbackend.services.EnvironmentService;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;

@Component
public class TerminalWebSocketHandler extends TextWebSocketHandler {

    private final EnvironmentService envService;

    public TerminalWebSocketHandler(EnvironmentService envService) {
        this.envService = envService;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // Extract instanceId from URI with null safety
        URI uri = session.getUri();
        if (uri == null) {
            System.err.println("WebSocket URI is null");
            session.close(CloseStatus.BAD_DATA.withReason("Invalid connection"));
            return;
        }

        String path = uri.getPath();
        if (path == null || path.isEmpty()) {
            System.err.println("WebSocket path is null or empty");
            session.close(CloseStatus.BAD_DATA.withReason("Invalid path"));
            return;
        }

        // Extract instanceId from path: /ws/terminal/{instanceId}
        String instanceId = extractInstanceId(path);
        if (instanceId == null || instanceId.isEmpty()) {
            System.err.println("Could not extract instanceId from path: " + path);
            session.close(CloseStatus.BAD_DATA.withReason("Invalid instance ID"));
            return;
        }

        System.out.println("Terminal connection for instance: " + instanceId);

        // Load instance from database
        ChallengeInstanceEntity inst = envService.getInstance(instanceId);
        if (inst == null) {
            System.err.println("Instance not found: " + instanceId);
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Instance not found"));
            return;
        }

        // Verify instance is running
        if (!"RUNNING".equals(inst.getStatus())) {
            System.err.println("Instance not running: " + instanceId + " (status: " + inst.getStatus() + ")");
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("Instance not running"));
            return;
        }

        try {
            //  Use ProcessBuilder array syntax (no shell injection)
            ProcessBuilder pb = new ProcessBuilder(
                    "docker", "exec", "-i", inst.getContainerName(), "/bin/bash"
            );
            pb.redirectErrorStream(true);
            Process proc = pb.start();

            // Save process in session for cleanup
            session.getAttributes().put("proc", proc);
            session.getAttributes().put("instanceId", instanceId);

            // Read from process stdout/stderr in background thread
            Thread outputThread = new Thread(() -> {
                try (var in = proc.getInputStream()) {
                    byte[] buf = new byte[4096];
                    int len;
                    while ((len = in.read(buf)) != -1 && session.isOpen()) {
                        String output = new String(buf, 0, len);
                        synchronized (session) {
                            if (session.isOpen()) {
                                session.sendMessage(new TextMessage(output));
                            }
                        }
                    }
                } catch (IOException e) {
                    System.err.println("Error reading from container: " + e.getMessage());
                } finally {
                    // Close session when process ends
                    try {
                        if (session.isOpen()) {
                            session.close(CloseStatus.NORMAL.withReason("Process terminated"));
                        }
                    } catch (IOException e) {
                        System.err.println("Error closing session: " + e.getMessage());
                    }
                }
            }, "terminal-output-" + instanceId);
            outputThread.setDaemon(true);
            outputThread.start();

            // Send welcome message
            session.sendMessage(new TextMessage(
                    "\r\n=== Connected to Challenge Container ===\r\n" +
                            "Instance: " + instanceId + "\r\n" +
                            "Container: " + inst.getContainerName() + "\r\n" +
                            "======================================\r\n\r\n"
            ));

        } catch (Exception e) {
            System.err.println("Failed to start docker exec: " + e.getMessage());
            e.printStackTrace();
            session.close(CloseStatus.SERVER_ERROR.withReason("Failed to connect to container"));
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Process proc = (Process) session.getAttributes().get("proc");
        if (proc == null || !proc.isAlive()) {
            session.sendMessage(new TextMessage("\r\n[Process not available]\r\n"));
            return;
        }

        try {
            // Send user input to container stdin
            String payload = message.getPayload();
            proc.getOutputStream().write(payload.getBytes());
            proc.getOutputStream().flush();
        } catch (IOException e) {
            System.err.println("Error writing to container: " + e.getMessage());
            session.close(CloseStatus.SERVER_ERROR.withReason("Connection lost"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String instanceId = (String) session.getAttributes().get("instanceId");
        System.out.println("Terminal connection closed for instance: " + instanceId + " (" + status + ")");

        // Cleanup: destroy the docker exec process
        Process proc = (Process) session.getAttributes().get("proc");
        if (proc != null) {
            proc.destroy();
            try {
                // Wait for process to terminate (max 5 seconds)
                proc.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
                if (proc.isAlive()) {
                    proc.destroyForcibly();
                }
            } catch (InterruptedException e) {
                proc.destroyForcibly();
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String instanceId = (String) session.getAttributes().get("instanceId");
        System.err.println("WebSocket transport error for instance " + instanceId + ": " + exception.getMessage());

        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException e) {
            System.err.println("Error closing session after transport error: " + e.getMessage());
        }
    }


     // Extract instanceId from WebSocket path
     // Expected format: /ws/terminal/{instanceId}

    private String extractInstanceId(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        // Remove leading/trailing slashes
        path = path.trim();
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        // Split by '/' and get last segment
        String[] segments = path.split("/");
        if (segments.length < 3) { // Expected: ["ws", "terminal", "{instanceId}"]
            return null;
        }

        return segments[segments.length - 1];
    }
}
*/