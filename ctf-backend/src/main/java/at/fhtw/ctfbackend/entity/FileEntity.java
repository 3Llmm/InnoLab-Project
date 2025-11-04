// FileEntity.java
package at.fhtw.ctfbackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class FileEntity {
    @Id
    private String id;
    private String fileName;

    @Lob
    @Column(columnDefinition = "BYTEA")
    private byte[] content;

    // Getters and setters
}