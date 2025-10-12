// FileEntity.java
package at.fhtw.ctfbackend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class FileEntity {
    @Id
    private String id;
    private String fileName;
    private byte[] content;

    // Getters and setters
}