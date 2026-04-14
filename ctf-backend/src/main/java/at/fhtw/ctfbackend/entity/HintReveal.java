package at.fhtw.ctfbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "hint_reveals",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"username", "challenge_id", "hint_index"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class HintReveal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private ChallengeEntity challenge;

    @Column(name = "hint_index", nullable = false)
    private Integer hintIndex;

    @CreationTimestamp
    @Column(name = "revealed_at", updatable = false)
    private LocalDateTime revealedAt;

    public HintReveal(String username, ChallengeEntity challenge, Integer hintIndex) {
        this.username = username;
        this.challenge = challenge;
        this.hintIndex = hintIndex;
    }
}
