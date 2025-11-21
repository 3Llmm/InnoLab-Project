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

    public FlagService(ChallengeInstanceRepository instanceRepo, EnvironmentService envService, ChallengeRepository challengeRepo) {
        this.instanceRepo = instanceRepo;
        this.envService = envService;
        this.challengeRepo = challengeRepo;
    }

    public boolean validateFlag(String username, String challengeId, String submittedFlag) {
        System.out.println("Validating flag for user: " + username + ", challenge: " + challengeId);

        // 1. First, get the challenge
        ChallengeEntity challenge = challengeRepo.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found: " + challengeId));

        // 2. Determine if instance is needed based on Docker image presence
        boolean requiresInstance = challenge.getDockerImageName() != null &&
                !challenge.getDockerImageName().isEmpty();

        System.out.println("Challenge " + challengeId + " requires instance: " + requiresInstance);

        boolean isValid;

        // 3. Route to appropriate validation
        if (requiresInstance) {
            isValid = validateDynamicFlag(username, challengeId, submittedFlag);
            System.out.println("Dynamic validation result: " + isValid);
        } else {
            isValid = validateStaticFlag(challenge, submittedFlag);
            System.out.println("Static validation result: " + isValid);
        }

        return isValid;
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

        System.out.println("🔍 Static flag comparison: " +
                "Submitted: " + submittedFlag +
                " vs Stored: " + challenge.getFlag() +
                " -> " + isValid);

        return isValid;
    }
    private final Map<String, Set<String>> solvedByUser = new ConcurrentHashMap<>();

    public boolean recordSolve(String username, String challengeId) {
        Set<String> solved = solvedByUser.computeIfAbsent(username,
                __ -> ConcurrentHashMap.newKeySet());
        boolean isNewSolve = solved.add(challengeId);

        System.out.println(
                "Record solve - User: " + username +
                        ", Challenge: " + challengeId +
                        ", New solve: " + isNewSolve
        );

        return isNewSolve;
    }

    public Set<String> getSolvedChallenges(String username) {
        Set<String> solved = solvedByUser.getOrDefault(username, Collections.emptySet());
        System.out.println("Retrieved solved challenges for " + username + ": " + solved.size() + " challenges");
        return Collections.unmodifiableSet(solved);
    }
}
