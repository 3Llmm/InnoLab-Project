package at.fhtw.ctfbackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity {

    @Id
    @Column(length = 100, nullable = false, unique = true)
    private String id; // e.g. "crypto"

    @Column(length = 2000, nullable = false)
    private String name; // e.g. "Cryptography"

    @Column(columnDefinition = "TEXT")
    private String summary; // Long description

    @Column(name = "file_url", length = 2000)
    private String fileUrl; // Optional URL (e.g. "https://example.com/crypto.zip")
}