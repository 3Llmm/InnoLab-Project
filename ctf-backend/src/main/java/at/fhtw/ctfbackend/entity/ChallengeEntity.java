package at.fhtw.ctfbackend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "challenges")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChallengeEntity {
    @Id
    private String id;

    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String category;
    private String difficulty;
    private Integer points;

    @Lob
    private byte[] downloadZip;

    private String originalFilename;
    private boolean requiresInstance;
    private String flag;



    // Store file metadata as JSON
    @Column(columnDefinition = "TEXT")
    private String dockerFilesJson;

    // Store the actual folder path on disk
    private String challengeFolderPath;

    // Store hints as JSON array
    @Column(columnDefinition = "TEXT")
    private String hintsJson;

    public byte[] getDownload() {
        return downloadZip;
    }

    public void setDownload(byte[] download) {
        this.downloadZip = download;
    }
}
