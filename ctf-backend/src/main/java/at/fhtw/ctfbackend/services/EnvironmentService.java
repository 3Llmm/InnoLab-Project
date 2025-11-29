package at.fhtw.ctfbackend.services;

import at.fhtw.ctfbackend.entity.ChallengeEntity;
import at.fhtw.ctfbackend.entity.ChallengeInstanceEntity;
import at.fhtw.ctfbackend.repository.ChallengeInstanceRepository;
import at.fhtw.ctfbackend.repository.ChallengeRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.ServerSocket;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class EnvironmentService {

    private final DockerService dockerService;
    private final ChallengeInstanceRepository instanceRepo;
    private final ChallengeRepository challengeRepo;

    // Thread-safe port allocation tracking
    private final Set<Integer> allocatedPorts = ConcurrentHashMap.newKeySet();
    private static final int SSH_BASE = 30000;
    private static final int VSCODE_BASE = 31000;
    private static final int DESKTOP_BASE = 32000;
    private static final int PORT_RANGE = 1000;

    public EnvironmentService(
            ChallengeInstanceRepository instanceRepo,
            DockerService dockerService,
            ChallengeRepository challengeRepo) {

        this.instanceRepo = instanceRepo;
        this.dockerService = dockerService;
        this.challengeRepo = challengeRepo;

        // Initialize with existing allocated ports
        loadAllocatedPorts();
    }

    public ChallengeInstanceEntity startEnvironment(String username, String challengeId) {

        // 1. Check if already has running instance
        var existing = instanceRepo.findByUsernameAndChallengeIdAndStatus(
                username, challengeId, "RUNNING"
        );
        if (!existing.isEmpty()) return existing.get(0);

        // 2. Load challenge metadata
        ChallengeEntity challenge = challengeRepo.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found: " + challengeId));

        // 3. Generate dynamic flag
        String realFlag = generateFlag(challengeId);
        String flagHash = sha256(realFlag);

        // 4. Allocate available ports (with retry logic)
        int maxRetries = 3;
        int sshPort = 0, vscodePort = 0, desktopPort = 0;

        for (int attempt = 0; attempt < maxRetries; attempt++) {
            try {
                sshPort = allocatePort(SSH_BASE);
                vscodePort = allocatePort(VSCODE_BASE);
                desktopPort = allocatePort(DESKTOP_BASE);
                break; // Success
            } catch (RuntimeException e) {
                if (attempt == maxRetries - 1) {
                    // Release any partially allocated ports
                    if (sshPort != 0) releasePort(sshPort);
                    if (vscodePort != 0) releasePort(vscodePort);
                    if (desktopPort != 0) releasePort(desktopPort);
                    throw new RuntimeException("Failed to allocate ports after " + maxRetries + " attempts", e);
                }
                // Wait before retry
                try { Thread.sleep(100); } catch (InterruptedException ignored) {}
            }
        }

        // 5. Create instance entry
        String instanceId = UUID.randomUUID().toString();
        String containerName = "ctf-" + instanceId.substring(0, 8); // Shorter container names

        ChallengeInstanceEntity inst = new ChallengeInstanceEntity();
        inst.setInstanceId(instanceId);
        inst.setUsername(username);
        inst.setChallengeId(challengeId);
        inst.setContainerName(containerName);
        inst.setFlagHash(flagHash);
        inst.setCreatedAt(Instant.now());
        inst.setExpiresAt(Instant.now().plusSeconds(3600)); // 1 hour
        inst.setStatus("RUNNING");
        inst.setSshPort(sshPort);
        inst.setVscodePort(vscodePort);
        inst.setDesktopPort(desktopPort);

        try {
            instanceRepo.save(inst);

            // 6. Start Docker container with real FLAG
            dockerService.runContainer(
                    containerName,
                    challenge.getDockerImageName(),
                    realFlag,
                    sshPort,
                    vscodePort,
                    desktopPort
            );

            return inst;

        } catch (Exception e) {
            // Rollback port allocations if container start fails
            releasePort(sshPort);
            releasePort(vscodePort);
            releasePort(desktopPort);
            throw new RuntimeException("Failed to start container", e);
        }
    }

    public ChallengeInstanceEntity getInstance(String instanceId) {
        return instanceRepo.findByInstanceId(instanceId).orElse(null);
    }

    public boolean stopEnvironment(String instanceId) {
        var instOpt = instanceRepo.findByInstanceId(instanceId);
        if (instOpt.isEmpty()) return false;

        var inst = instOpt.get();

        try {
            dockerService.stopContainer(inst.getContainerName());
        } catch (Exception e) {
            System.out.println("Container already stopped or missing");
        }

        // Release ports back to pool
        releasePort(inst.getSshPort());
        releasePort(inst.getVscodePort());
        releasePort(inst.getDesktopPort());

        inst.setStatus("STOPPED");
        instanceRepo.save(inst);

        return true;
    }


    // ===== PORT MANAGEMENT =====

    /**
     * Allocate an available port from the given base range
     */
    private synchronized int allocatePort(int basePort) {
        for (int offset = 0; offset < PORT_RANGE; offset++) {
            int port = basePort + offset;

            // Skip if already allocated
            if (allocatedPorts.contains(port)) {
                continue;
            }

            // Verify port is actually available on system
            if (isPortAvailable(port)) {
                allocatedPorts.add(port);
                return port;
            }
        }
        throw new RuntimeException("No available ports in range " + basePort + "-" + (basePort + PORT_RANGE));
    }

    /**
     * Release port back to available pool
     */
    private void releasePort(int port) {
        allocatedPorts.remove(port);
    }

    /**
     * Check if port is available by attempting to bind to it
     */
    private boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            socket.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Load currently allocated ports from database on startup
     */
    private void loadAllocatedPorts() {
        List<ChallengeInstanceEntity> runningInstances =
                instanceRepo.findAll().stream()
                        .filter(inst -> "RUNNING".equals(inst.getStatus()))
                        .toList();

        for (ChallengeInstanceEntity inst : runningInstances) {
            allocatedPorts.add(inst.getSshPort());
            allocatedPorts.add(inst.getVscodePort());
            allocatedPorts.add(inst.getDesktopPort());
        }

        System.out.println("Loaded " + allocatedPorts.size() + " allocated ports from database");
    }

    // ===== UTILITY METHODS =====

    private String generateFlag(String challengeId) {
        byte[] random = new byte[8];
        new SecureRandom().nextBytes(random);
        StringBuilder sb = new StringBuilder();
        for (byte b : random) sb.append(String.format("%02x", b));
        return "FLAG{" + challengeId + "_" + sb + "}";
    }

    public String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(s.getBytes());
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}