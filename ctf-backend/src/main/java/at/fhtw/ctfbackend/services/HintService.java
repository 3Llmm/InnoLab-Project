package at.fhtw.ctfbackend.services;

import at.fhtw.ctfbackend.entity.ChallengeEntity;
import at.fhtw.ctfbackend.entity.HintReveal;
import at.fhtw.ctfbackend.repository.ChallengeRepository;
import at.fhtw.ctfbackend.repository.HintRevealRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class HintService {

    // Penalties per hint index: Hint 0 = 10%, Hint 1 = 20%, Hint 2 = 25%
    private static final int[] HINT_PENALTIES = {10, 20, 25};

    // How many seconds a user must wait before unlocking the next hint
    private static final int LOCK_SECONDS = 60;

    private final HintRevealRepository hintRevealRepository;
    private final ChallengeRepository challengeRepository;

    public HintService(HintRevealRepository hintRevealRepository, ChallengeRepository challengeRepository) {
        this.hintRevealRepository = hintRevealRepository;
        this.challengeRepository = challengeRepository;
    }

    /**
     * Reveals a hint for a user.
     * Returns the hint text if successful
     */
    public String revealHint(String username, String challengeId, int hintIndex) {
        ChallengeEntity challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found: " + challengeId));

        // Parse hints from the challenge
        List<String> hints = parseHints(challenge.getHintsJson());

        if (hintIndex < 0 || hintIndex >= hints.size()) {
            throw new RuntimeException("Invalid hint index");
        }

        // Check if user already revealed this hint
        Optional<HintReveal> existing = hintRevealRepository
                .findByUsernameAndChallengeIdAndHintIndex(username, challengeId, hintIndex);
        if (existing.isPresent()) {
            // Already revealed — just return the text again
            return hints.get(hintIndex);
        }

        // Check time lock: the previous hint must have been revealed at least LOCK_SECONDS ago
        if (hintIndex > 0) {
            Optional<HintReveal> previousReveal = hintRevealRepository
                    .findByUsernameAndChallengeIdAndHintIndex(username, challengeId, hintIndex - 1);

            if (previousReveal.isEmpty()) {
                throw new RuntimeException("You must reveal the previous hint first");
            }

            LocalDateTime unlockTime = previousReveal.get().getRevealedAt().plusSeconds(LOCK_SECONDS);
            if (LocalDateTime.now().isBefore(unlockTime)) {
                throw new RuntimeException("LOCKED_UNTIL:" + unlockTime);
            }
        }

        // All checks passed — save the reveal and return the hint text
        HintReveal reveal = new HintReveal(username, challenge, hintIndex);
        hintRevealRepository.save(reveal);

        return hints.get(hintIndex);
    }

    /**
     * Returns how many percent of points should be deducted for a user on a challenge.
     * Called at flag-submit time.
     */
    public int calculatePenaltyPercent(String username, String challengeId) {
        List<HintReveal> reveals = hintRevealRepository
                .findByUsernameAndChallengeId(username, challengeId);

        int totalPenalty = 0;
        for (HintReveal reveal : reveals) {
            int index = reveal.getHintIndex();
            if (index < HINT_PENALTIES.length) {
                totalPenalty += HINT_PENALTIES[index];
            }
        }

        // Cap at 100% just in case
        return Math.min(totalPenalty, 100);
    }

    /**
     * Parses the hints JSON string from the challenge into a plain Java list.
     * The hints are stored as a JSON array like: ["Hint 1 text", "Hint 2 text"]
     */
    private List<String> parseHints(String hintsJson) {
        if (hintsJson == null || hintsJson.isBlank()) {
            return List.of();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(hintsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of();
        }
    }
}
