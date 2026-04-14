package at.fhtw.ctfbackend.controller;

import at.fhtw.ctfbackend.entity.HintReveal;
import at.fhtw.ctfbackend.repository.HintRevealRepository;
import at.fhtw.ctfbackend.services.HintService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/hints")
public class HintController {

    private final HintService hintService;
    private final HintRevealRepository hintRevealRepository;

    public HintController(HintService hintService, HintRevealRepository hintRevealRepository) {
        this.hintService = hintService;
        this.hintRevealRepository = hintRevealRepository;
    }

    /**
     * POST /api/hints/reveal
     * Reveals a hint for the logged-in user.
     * Body: { "challengeId": "crypto-101", "hintIndex": 0 }
     */
    @PostMapping("/reveal")
    public ResponseEntity<Map<String, Object>> revealHint(
            Authentication auth,
            @RequestBody Map<String, Object> body) {

        String username = auth.getName();
        String challengeId = (String) body.get("challengeId");
        int hintIndex = (int) body.get("hintIndex");

        try {
            String hintText = hintService.revealHint(username, challengeId, hintIndex);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "hintText", hintText
            ));
        } catch (RuntimeException e) {
            String message = e.getMessage();

            // Time-lock: tell the frontend exactly when the hint unlocks
            if (message != null && message.startsWith("LOCKED_UNTIL:")) {
                String unlockTime = message.replace("LOCKED_UNTIL:", "");
                return ResponseEntity.status(423).body(Map.of(
                        "status", "locked",
                        "unlocksAt", unlockTime
                ));
            }

            return ResponseEntity.badRequest().body(Map.of(
                    "status", "error",
                    "message", message
            ));
        }
    }

    /**
     * GET /api/hints/status/{challengeId}
     * Returns which hints the user has already revealed, and when the next one unlocks.
     * The frontend uses this to show the correct state on page load.
     */
    @GetMapping("/status/{challengeId}")
    public ResponseEntity<Map<String, Object>> getHintStatus(
            Authentication auth,
            @PathVariable String challengeId) {

        String username = auth.getName();
        List<HintReveal> reveals = hintRevealRepository.findByUsernameAndChallengeId(username, challengeId);

        // Build a list of revealed hint indexes
        List<Integer> revealedIndexes = new ArrayList<>();
        LocalDateTime lastRevealedAt = null;

        for (HintReveal reveal : reveals) {
            revealedIndexes.add(reveal.getHintIndex());
            if (lastRevealedAt == null || reveal.getRevealedAt().isAfter(lastRevealedAt)) {
                lastRevealedAt = reveal.getRevealedAt();
            }
        }

        // Calculate when the next hint unlocks (1 minute after the last reveal)
        String nextUnlocksAt = null;
        if (lastRevealedAt != null) {
            nextUnlocksAt = lastRevealedAt.plusSeconds(60).toString();
        }

        return ResponseEntity.ok(Map.of(
                "revealedIndexes", revealedIndexes,
                "nextUnlocksAt", nextUnlocksAt != null ? nextUnlocksAt : ""
        ));
    }
}
