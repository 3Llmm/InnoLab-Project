package at.fhtw.ctfbackend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryEntity {

    @Id
    @Column(length = 100, nullable = false, unique = true)
    private String id;

    @Column(length = 2000, nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(name = "file_url", length = 2000)
    private String fileUrl;
}