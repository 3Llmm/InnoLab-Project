package at.fhtw.ctfbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "challenges")
@Getter
@Setter
public class ChallengeEntity {

    @Id
    private String id;                     // e.g., "rev-101"

    private String title;
    private String description;
    private String category;
    private String difficulty;
    private Integer points;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Solve> solves = new ArrayList<>();

    private byte[] download;
    private String flag;                   // internal only

    @Column(name = "challenge_type")
    private String challengeType;          // STATIC or DYNAMIC

    @Column(name = "original_filename")
    private String originalFilename;

    @Column(name = "flag_hash")
    private String flagHash;

    @Column(name = "requires_instance")
    private Boolean requiresInstance = false;

    private String dockerImageName;
    private Integer defaultSshPort;
    private Integer defaultVscodePort;
    private Integer defaultDesktopPort;

    protected ChallengeEntity() { }

    public ChallengeEntity(String id, String title, String description,
                           String category, String difficulty, Integer points,
                           byte[] download, String flag) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.points = points;
        this.download = download;
        this.flag = flag;
    }
    public String getOriginalFilename() {
        return originalFilename;
    }
}

