package at.fhtw.ctfbackend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import lombok.*;

@Setter
@Getter
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryEntity {

    @Id
    private String id; // e.g. "crypto"

    private String name; // e.g. "Cryptography"

    @Lob
    private String summary; // large text for description

    String fileUrl; // URL to the file (e.g. "https://example.com/crypto.zip")

    // Later: tools, resources, etc.
}
