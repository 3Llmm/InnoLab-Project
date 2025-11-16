package at.fhtw.ctfbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "challenge_instances")
@Getter
@Setter
public class ChallengeInstanceEntity {

    @Id
    private String instanceId;  // UUID from backend

    private String username;    // FH username

    private String challengeId; // FK to ChallengeEntity

    private String containerName;

    private String flagHash;    // SHA-256 hash of flag (NOT the flag itself)

    private Instant createdAt;
    private Instant expiresAt;

    private String status;      // RUNNING, STOPPED, EXPIRED

    private Integer sshPort;
    private Integer vscodePort;
    private Integer desktopPort;
}
