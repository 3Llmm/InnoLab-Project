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
    private String id;

    private String title;
    private String description;

    @Lob
    private byte[] downloadZip;       // store the .zip bytes

    private String flag;              // internal only

    protected ChallengeEntity() { }

    public ChallengeEntity(String id, String title, String description, byte[] downloadZip, String flag) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.downloadZip = downloadZip;
        this.flag = flag;
    }
}
