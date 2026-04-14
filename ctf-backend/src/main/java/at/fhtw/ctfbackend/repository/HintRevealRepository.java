package at.fhtw.ctfbackend.repository;

import at.fhtw.ctfbackend.entity.HintReveal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HintRevealRepository extends JpaRepository<HintReveal, Long> {

    List<HintReveal> findByUsernameAndChallengeId(String username, String challengeId);

    Optional<HintReveal> findByUsernameAndChallengeIdAndHintIndex(String username, String challengeId, Integer hintIndex);

}
