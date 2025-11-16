package at.fhtw.ctfbackend.services;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
public class DockerService {

    // Validation patterns for security
    private static final Pattern CONTAINER_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9_.-]{0,62}$");
    private static final Pattern IMAGE_NAME_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9._/-]{0,127}(:[a-zA-Z0-9._-]{0,127})?$");

    /**
     * Run a container with security constraints
     */
    public void runContainer(String containerName,
                             String imageName,
                             String flag,
                             int sshPort,
                             int vscodePort,
                             int desktopPort) {

        // INPUT VALIDATION
        validateContainerName(containerName);
        validateImageName(imageName);
        validatePort(sshPort);
        validatePort(vscodePort);
        validatePort(desktopPort);

        if (flag == null || flag.isEmpty()) {
            throw new IllegalArgumentException("Flag cannot be empty");
        }

        try {
            List<String> command = new ArrayList<>();
            command.add("docker");
            command.add("run");
            command.add("-d");

            // === SECURITY CONSTRAINTS ===

            // Resource limits
            command.add("--memory=512m");
            command.add("--memory-swap=512m");
            command.add("--cpus=1.0");
            command.add("--pids-limit=100");

            // Network isolation
            command.add("--network=ctf-isolated");

            // Security options
            command.add("--security-opt=no-new-privileges");
            command.add("--cap-drop=ALL");
            command.add("--cap-add=CHOWN");
            command.add("--cap-add=SETUID");
            command.add("--cap-add=SETGID");
          //  command.add("--read-only"); // Read-only root filesystem
            command.add("--tmpfs=/tmp:rw,noexec,nosuid,size=100m");
            command.add("--tmpfs=/var/tmp:rw,noexec,nosuid,size=100m");

            // Prevent privilege escalation
            command.add("--security-opt=apparmor=docker-default");

            // Container name (validated)
            command.add("--name");
            command.add(containerName);

            // Environment variable (flag is passed safely)
            command.add("-e");
            command.add("FLAG=" + flag);

            // Port mappings (validated)
            command.add("-p");
            command.add(sshPort + ":22");
            command.add("-p");
            command.add(vscodePort + ":8080");
            command.add("-p");
            command.add(desktopPort + ":6080");

            // Automatic cleanup
            command.add("--rm=false"); // We manage cleanup manually

            // Image name (validated)
            command.add(imageName);

            System.out.println("RUNNING DOCKER (SECURE): " + String.join(" ", command));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            int exitCode = process.waitFor();

            if (exitCode != 0) {
                // Read error output
                String error = new String(process.getInputStream().readAllBytes());
                throw new RuntimeException("Docker run failed with exit code " + exitCode + ": " + error);
            }

        } catch (Exception e) {
            throw new RuntimeException("Failed to launch container: " + e.getMessage(), e);
        }
    }

    /**
     * Stop and remove a running container
     */
    public void stopContainer(String containerName) {
        validateContainerName(containerName);

        try {
            // Stop container (timeout after 10 seconds)
            ProcessBuilder stopCmd = new ProcessBuilder("docker", "stop", "-t", "10", containerName);
            Process stopProc = stopCmd.start();
            stopProc.waitFor();

            // Remove container
            ProcessBuilder rmCmd = new ProcessBuilder("docker", "rm", "-f", containerName);
            Process rmProc = rmCmd.start();
            rmProc.waitFor();

        } catch (Exception e) {
            throw new RuntimeException("Failed to stop container: " + e.getMessage(), e);
        }
    }

    /**
     * Check if container exists
     */
    public boolean exists(String containerName) {
        validateContainerName(containerName);

        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "inspect", containerName);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Kill a container forcefully (for emergency cleanup)
     */
    public void killContainer(String containerName) {
        validateContainerName(containerName);

        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "kill", containerName);
            Process p = pb.start();
            p.waitFor();
        } catch (Exception e) {
            System.err.println("Failed to kill container " + containerName + ": " + e.getMessage());
        }
    }

    // ===== VALIDATION METHODS =====

    private void validateContainerName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Container name cannot be empty");
        }
        if (name.length() > 63) {
            throw new IllegalArgumentException("Container name too long (max 63 chars)");
        }
        if (!CONTAINER_NAME_PATTERN.matcher(name).matches()) {
            throw new IllegalArgumentException("Invalid container name: " + name);
        }
    }

    private void validateImageName(String image) {
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("Image name cannot be empty");
        }
        if (!IMAGE_NAME_PATTERN.matcher(image).matches()) {
            throw new IllegalArgumentException("Invalid image name: " + image);
        }
    }

    private void validatePort(int port) {
        if (port < 1024 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1024 and 65535: " + port);
        }
    }
}