package at.fhtw.ctfbackend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.*;
import java.util.*;
import java.util.regex.Pattern;
import java.util.concurrent.TimeUnit;

@Service
public class DockerService {

    // Validation patterns for security
    private static final Pattern CONTAINER_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9][a-zA-Z0-9_.-]{0,62}$");
    private static final Pattern IMAGE_NAME_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9._/-]{0,127}(:[a-zA-Z0-9._-]{0,127})?$");
    private static final Pattern CHALLENGE_ID_PATTERN = Pattern.compile("^[a-z0-9][a-z0-9_.-]{0,62}$");

    // Base path for challenges
    @Value("${challenges.base.path:/app/challenges}")
    private String challengesBasePath;

    /**
     * Build a Docker image from a challenge directory
     */
    public String buildImage(String challengeId, String tag) {
        validateChallengeId(challengeId);
        validateImageTag(tag);

        // Determine challenge directory path
        String challengeDir = getChallengeDirPath(challengeId);

        try {
            List<String> command = new ArrayList<>();
            command.add("docker");
            command.add("build");
            command.add("--no-cache"); // Ensure fresh build
            command.add("-t");
            command.add(tag);
            command.add(challengeDir);

            System.out.println("Building Docker image from: " + challengeDir);
            System.out.println("Command: " + String.join(" ", command));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read and log output in real-time
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("Docker build: " + line);
                    output.append(line).append("\n");
                }
            }

            boolean completed = process.waitFor(5, TimeUnit.MINUTES);

            if (!completed) {
                process.destroy();
                throw new RuntimeException("Docker build timed out after 5 minutes");
            }

            int exitCode = process.exitValue();

            if (exitCode != 0) {
                throw new RuntimeException("Docker build failed with exit code " + exitCode + ":\n" + output);
            }

            System.out.println("✅ Image built successfully: " + tag);
            return tag;

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to build Docker image: " + e.getMessage(), e);
        }
    }

    /**
     * Run a container with security constraints
     */
    public void runContainer(String containerName, String imageName, String flag,
                             int sshPort, int vscodePort, int desktopPort) {

        System.out.println("🟢 === RUN CONTAINER SIMPLIFIED ===");
        System.out.println("📦 Image: " + imageName);
        System.out.println("🏷️ Container: " + containerName);
        System.out.println("🔌 Ports: SSH=" + sshPort + ", VSCode=" + vscodePort + ", Desktop=" + desktopPort);
        System.out.println("🚩 Flag: " + (flag != null ? flag.substring(0, Math.min(flag.length(), 20)) : "null"));

        try {
            // Build command
            List<String> command = Arrays.asList(
                    "docker", "run", "-d",
                    "--name", containerName,
                    "--network", "ctf-isolated",  // Add network back
                    "-e", "FLAG=" + flag,         // Use the flag (without quotes)
                    "-p", sshPort + ":22",
                    "-p", vscodePort + ":8080",
                    "-p", desktopPort + ":6080",
                    imageName
            );

            System.out.println("💻 Command: " + String.join(" ", command));

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.redirectErrorStream(true);  // Merge stdout and stderr
            Process process = pb.start();

            // Read output in real-time
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                System.out.println("📋 Docker output:");
                while ((line = reader.readLine()) != null) {
                    System.out.println("   " + line);
                    output.append(line).append("\n");
                }
            }

            // Wait for process to complete
            int exitCode = process.waitFor();
            System.out.println("🔚 Exit code: " + exitCode);

            if (exitCode != 0) {
                throw new RuntimeException("Docker run failed with exit code " + exitCode + ":\n" + output);
            }

            System.out.println("✅ Container started successfully!");

        } catch (Exception e) {
            System.out.println("❌ ERROR in runContainer: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to run container: " + e.getMessage(), e);
        }
    }

    /**
     * Build and run a challenge in one step
     */
    public String buildAndRun(String challengeId,
                              String containerName,
                              String flag,
                              int sshPort,
                              int vscodePort,
                              int desktopPort) {

        validateChallengeId(challengeId);

        // Build image if needed
        String imageTag = "ctf-" + challengeId.toLowerCase();
        if (!imageExists(imageTag)) {
            System.out.println("Image not found, building: " + imageTag);
            buildImage(challengeId, imageTag);
        }

        // Run container
        runContainer(containerName, imageTag, flag, sshPort, vscodePort, desktopPort);

        return containerName;
    }

    /**
     * Stop and remove a running container
     */
    public void stopContainer(String containerName) {
        validateContainerName(containerName);

        try {
            System.out.println("Stopping container: " + containerName);

            // Stop container (timeout after 10 seconds)
            ProcessBuilder stopCmd = new ProcessBuilder("docker", "stop", "-t", "10", containerName);
            Process stopProc = stopCmd.start();
            stopProc.waitFor();

            // Remove container
            ProcessBuilder rmCmd = new ProcessBuilder("docker", "rm", "-f", containerName);
            Process rmProc = rmCmd.start();
            rmProc.waitFor();

            System.out.println("✅ Container stopped and removed: " + containerName);

        } catch (Exception e) {
            throw new RuntimeException("Failed to stop container: " + e.getMessage(), e);
        }
    }

    /**
     * Check if container exists
     */
    public boolean containerExists(String containerName) {
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
     * Check if image exists locally
     */
    public boolean imageExists(String imageName) {
        validateImageName(imageName);

        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "image", "inspect", imageName);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Remove Docker image
     */
    public void removeImage(String imageName) {
        validateImageName(imageName);

        try {
            System.out.println("Removing image: " + imageName);
            ProcessBuilder pb = new ProcessBuilder("docker", "rmi", "-f", imageName);
            Process p = pb.start();
            p.waitFor();
            System.out.println("✅ Image removed: " + imageName);
        } catch (Exception e) {
            System.err.println("Failed to remove image " + imageName + ": " + e.getMessage());
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
            System.out.println("Container killed: " + containerName);
        } catch (Exception e) {
            System.err.println("Failed to kill container " + containerName + ": " + e.getMessage());
        }
    }

    /**
     * Get container status
     */
    public String getContainerStatus(String containerName) {
        validateContainerName(containerName);

        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "inspect",
                    "--format", "{{.State.Status}}", containerName);
            pb.redirectErrorStream(true);
            Process p = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String status = reader.readLine();
            p.waitFor();

            return status != null ? status : "unknown";
        } catch (Exception e) {
            return "error";
        }
    }

    /**
     * List all running containers for a specific challenge
     */
    public List<String> getRunningContainersForChallenge(String challengeId) {
        validateChallengeId(challengeId);

        List<String> containers = new ArrayList<>();
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "ps",
                    "--filter", "name=ctf-" + challengeId,
                    "--format", "{{.Names}}");
            pb.redirectErrorStream(true);
            Process p = pb.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    containers.add(line.trim());
                }
            }
            p.waitFor();

            return containers;
        } catch (Exception e) {
            System.err.println("Failed to list containers: " + e.getMessage());
            return containers;
        }
    }

    /**
     * Clean up all containers for a specific challenge
     */
    public void cleanupChallengeContainers(String challengeId) {
        validateChallengeId(challengeId);

        List<String> containers = getRunningContainersForChallenge(challengeId);
        for (String container : containers) {
            try {
                stopContainer(container);
            } catch (Exception e) {
                System.err.println("Failed to stop container " + container + ": " + e.getMessage());
            }
        }
    }

    // ===== HELPER METHODS =====

    private String getChallengeDirPath(String challengeId) {
        String dirPath = challengesBasePath + "/" + challengeId;
        File dir = new File(dirPath);

        if (!dir.exists() || !dir.isDirectory()) {
            throw new IllegalArgumentException("Challenge directory not found: " + dirPath);
        }

        // Check if Dockerfile exists
        File dockerfile = new File(dir, "Dockerfile");
        if (!dockerfile.exists()) {
            throw new IllegalArgumentException("Dockerfile not found in: " + dirPath);
        }

        return dirPath;
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

    private void validateImageTag(String tag) {
        if (tag == null || tag.isEmpty()) {
            throw new IllegalArgumentException("Image tag cannot be empty");
        }
        if (tag.contains("..") || tag.contains("/") || tag.matches(".*[^a-z0-9_.-].*")) {
            throw new IllegalArgumentException("Invalid image tag: " + tag);
        }
    }

    private void validateChallengeId(String challengeId) {
        if (challengeId == null || challengeId.isEmpty()) {
            throw new IllegalArgumentException("Challenge ID cannot be empty");
        }
        if (!CHALLENGE_ID_PATTERN.matcher(challengeId).matches()) {
            throw new IllegalArgumentException("Invalid challenge ID: " + challengeId);
        }
    }

    private void validatePort(int port) {
        if (port < 1024 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1024 and 65535: " + port);
        }
    }

    // ===== CONFIGURATION GETTER/SETTER =====

    public String getChallengesBasePath() {
        return challengesBasePath;
    }

    public void setChallengesBasePath(String challengesBasePath) {
        this.challengesBasePath = challengesBasePath;
    }
}