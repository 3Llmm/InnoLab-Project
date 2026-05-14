package at.fhtw.ctfbackend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String username;

    @Column(length = 255)
    private String email;

    @Column(length = 255)
    private String displayName;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isAdmin = false;

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    private LocalDateTime lastLoginAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public String getEffectiveEmail() {
        if (email == null || email.isBlank()) {
            return username + "@technikum-wien.at";
        }
        return email;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
