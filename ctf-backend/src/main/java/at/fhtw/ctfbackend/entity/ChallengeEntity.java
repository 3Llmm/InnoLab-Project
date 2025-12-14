package at.fhtw.ctfbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "challenges")
@Getter
@Setter
public class ChallengeEntity {
    @Id
    private String id;

    private String title;
    private String description;
    private String category;
    private String difficulty;
    private Integer points;

    @Lob
    private byte[] downloadZip;

    private String originalFilename;
    private boolean requiresInstance;
    private String flag;

    private String dockerImageName;

    // Store file metadata as JSON
    @Column(columnDefinition = "TEXT")
    private String dockerFilesJson;

    // Store the actual folder path on disk
    private String challengeFolderPath;

    // Store hints as JSON array
    @Column(columnDefinition = "TEXT")
    private String hintsJson;

    protected ChallengeEntity() { }

    public ChallengeEntity(String id, String title, String description,
                           String category, String difficulty, Integer points,
                           byte[] downloadZip, String flag) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.difficulty = difficulty;
        this.points = points;
        this.downloadZip = downloadZip;
        this.flag = flag;
        this.dockerFilesJson = "{}"; // Initialize empty JSON
    }

    public byte[] getDownload() {
        return this.downloadZip;
    }

    public void setDownload(byte[] download) {
        this.downloadZip = download;
    }
}
