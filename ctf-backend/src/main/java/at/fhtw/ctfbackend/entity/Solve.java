package at.fhtw.ctfbackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "solves",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"username", "challenge_id"})
        }
)
@Getter
@Setter
@NoArgsConstructor
public class Solve {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private ChallengeEntity challenge;


    @CreationTimestamp
    @Column(name = "solved_at", updatable = false)
    private LocalDateTime solvedAt;

    @Column(name = "points_earned")
    private Integer pointsEarned = 0;

    public Solve(String username, ChallengeEntity challenge, Integer pointsEarned) {
        this.username = username;
        this.challenge = challenge;
        this.pointsEarned = pointsEarned;
    }
}
