package at.fhtw.ctfbackend.repository;

import at.fhtw.ctfbackend.entity.ChallengeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChallengeRepository extends JpaRepository<ChallengeEntity, String> {

    @Query("SELECT c.category, COUNT(c) FROM ChallengeEntity c GROUP BY c.category")
    List<Object[]> countChallengesByCategory();

    @Query("SELECT c.difficulty, COUNT(c) FROM ChallengeEntity c GROUP BY c.difficulty")
    List<Object[]> countChallengesByDifficulty();
}