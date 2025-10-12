package at.fhtw.ctfbackend.services;

import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FlagService {

    // Hard‑coded flags for prototype
    private final Map<String, String> challengeFlags = Map.of(
            "web-101", "flag{leet_xss}",
            "rev-201", "flag{reverse_master}"
    );

    // Track solved challenges per user
    private final Map<String, Set<String>> solvedByUser = new ConcurrentHashMap<>();

    /**
     * Validate a submitted flag for a given challenge.
     */
    public boolean validateFlag(final String challengeId, final String flag) {
        String expected = challengeFlags.get(challengeId);
        return expected != null && expected.equals(flag);
    }

    /**
     * Record that a user solved a challenge. Returns false if already solved.
     */
    public boolean recordSolve(final String username, final String challengeId) {
        // Get or create the user’s solved‑set
        Set<String> solved = solvedByUser.computeIfAbsent(username, __ -> ConcurrentHashMap.newKeySet());
        // Returns true if this is a new solve
        return solved.add(challengeId);
    }

    /**
     * Optional helper: List challenges a user has solved.
     */
    public Set<String> getSolvedChallenges(final String username) {
        return Collections.unmodifiableSet(
                solvedByUser.getOrDefault(username, Collections.emptySet())
        );
    }
}
