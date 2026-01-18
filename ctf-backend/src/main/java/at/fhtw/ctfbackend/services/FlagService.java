package at.fhtw.ctfbackend.services;

import at.fhtw.ctfbackend.entity.ChallengeEntity;
import at.fhtw.ctfbackend.repository.ChallengeInstanceRepository;
import at.fhtw.ctfbackend.entity.ChallengeInstanceEntity;
import at.fhtw.ctfbackend.repository.ChallengeRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FlagService {

    private final ChallengeInstanceRepository instanceRepo;
    private final EnvironmentService envService;
    private final ChallengeRepository challengeRepo;
    private final SolveService solveService;

    public FlagService(ChallengeInstanceRepository instanceRepo, EnvironmentService envService, ChallengeRepository challengeRepo, SolveService solveService) {
        this.instanceRepo = instanceRepo;
        this.envService = envService;
        this.challengeRepo = challengeRepo;
        this.solveService = solveService;
    }

    public boolean validateFlag(String username, String challengeId, String submittedFlag) {
        System.out.println(" Validating flag for user: " + username + ", challenge: " + challengeId);

        try {
            // 1. First, get the challenge
            ChallengeEntity challenge = challengeRepo.findById(challengeId)
                    .orElseThrow(() -> new RuntimeException("Challenge not found: " + challengeId));

            // 2. Use the explicit requiresInstance field from the challenge
            boolean requiresInstance = challenge.isRequiresInstance();

            System.out.println("  Challenge " + challengeId + " requires instance: " + requiresInstance);


            boolean isValid;

            // 3. Route to appropriate validation
            if (requiresInstance) {
                isValid = validateDynamicFlag(username, challengeId, submittedFlag);
                System.out.println(" Dynamic validation result: " + isValid);
            } else {
                isValid = validateStaticFlag(challenge, submittedFlag);
                System.out.println(" Static validation result: " + isValid);
            }

            return isValid;
        } catch (Exception e) {
            System.err.println(" Error during flag validation: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    private boolean validateDynamicFlag(String username, String challengeId, String submittedFlag) {
        var instances = instanceRepo.findByUsernameAndChallengeIdAndStatus(
                username, challengeId, "RUNNING"
        );

        if (instances.isEmpty()) {
            System.out.println("No running instance found for dynamic challenge");
            return false;
        }

        ChallengeInstanceEntity inst = instances.get(0);
        String submittedHash = envService.sha256(submittedFlag);
        boolean isValid = submittedHash.equals(inst.getFlagHash());

        System.out.println(
                "Dynamic flag comparison: Submitted hash: " +
                        submittedHash.substring(0, 16) + "..." +
                        " vs Stored hash: " +
                        inst.getFlagHash().substring(0, 16) + "..."
        );

        return isValid;
    }

    private boolean validateStaticFlag(ChallengeEntity challenge, String submittedFlag) {
        // For static challenges, compare directly with stored flag
        if (challenge.getFlag() == null || submittedFlag == null) {
            return false;
        }

        // Direct string comparison for static challenges
        boolean isValid = challenge.getFlag().equals(submittedFlag);

        System.out.println(" Static flag comparison: " +
                "Submitted: " + submittedFlag +
                " vs Stored: " + challenge.getFlag() +
                " -> " + isValid);

        return isValid;
    }
    private final Map<String, Set<String>> solvedByUser = new ConcurrentHashMap<>();

    public boolean recordSolve(String username, String challengeId) {
        System.out.println(" Attempting to record solve for user: " + username + ", challenge: " + challengeId);

        try {
            // First check in-memory cache
            Set<String> solved = solvedByUser.computeIfAbsent(username,
                    __ -> ConcurrentHashMap.newKeySet());
            boolean isNewSolve = solved.add(challengeId);

            System.out.println(" Cache check - User already solved: " + !isNewSolve);

            // Also persist to database using SolveService
            if (isNewSolve) {
                System.out.println(" This is a new solve, recording in database...");
                
                ChallengeEntity challenge = challengeRepo.findById(challengeId)
                        .orElseThrow(() -> new RuntimeException("Challenge not found: " + challengeId));
                
                int pointsEarned = challenge.getPoints() != null ? challenge.getPoints() : 0;
                System.out.println(" Points to award: " + pointsEarned);
                
                boolean dbSuccess = solveService.recordSolve(username, challengeId, pointsEarned);
                System.out.println("  Database record result: " + dbSuccess);
                
                if (dbSuccess) {
                    System.out.println(" Solve recorded successfully for user: " + username + ", challenge: " + challengeId);
                } else {
                    System.err.println(" Failed to record solve in database");
                    // Remove from cache if database failed
                    solved.remove(challengeId);
                    isNewSolve = false;
                }
            } else {
                System.out.println("ℹ  User already solved this challenge, skipping database record");
            }

            System.out.println(
                    " Final result - User: " + username +
                            ", Challenge: " + challengeId +
                            ", New solve: " + isNewSolve
            );

            return isNewSolve;
        } catch (Exception e) {
            System.err.println(" Critical error recording solve: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Set<String> getSolvedChallenges(String username) {
        Set<String> solved = solvedByUser.getOrDefault(username, Collections.emptySet());
        System.out.println("Retrieved solved challenges for " + username + ": " + solved.size() + " challenges");
        return Collections.unmodifiableSet(solved);
    }


    /**
     * Check if a user has solved a specific challenge (using database)
     * @param username The username to check
     * @param challengeId The challenge ID to check
     * @return true if the user has solved the challenge, false otherwise
     */
    public boolean hasUserSolvedChallenge(String username, String challengeId) {
        return solveService.hasUserSolvedChallenge(username, challengeId);
    }

    /**
     * Get the count of users who solved a specific challenge
     * @param challengeId The challenge ID to query
     * @return Number of users who solved the challenge
     */
    public long getSolveCountForChallenge(String challengeId) {
        return solveService.getSolveCountForChallenge(challengeId);
    }
}
