package at.fhtw.ctfbackend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "challenges")
@Getter
@Setter
public class ChallengeEntity {
    @Id
    private String id;  // challenge ID: "rev-101"

    private String title;           // Changed from name to title for consistency

    private String description;

    private String category;        // Store category directly
    private String difficulty;      // Store difficulty directly
    private Integer points;         // Store points directly


    @Lob
    private byte[] downloadZip;

    private String flag;            // Internal only



    private String dockerImageName;  // e.g. "myctf/rev-101"
    private Integer defaultSshPort;      //  22
    private Integer defaultVscodePort;   // 8080 in container
    private Integer defaultDesktopPort;  // 6080 in container


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

    }
}
