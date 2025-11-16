package at.fhtw.ctfbackend.services;

import at.fhtw.ctfbackend.repository.ChallengeInstanceRepository;
import at.fhtw.ctfbackend.entity.ChallengeInstanceEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FlagService {

    private final ChallengeInstanceRepository instanceRepo;
    private final EnvironmentService envService;

    public FlagService(ChallengeInstanceRepository instanceRepo, EnvironmentService envService) {
        this.instanceRepo = instanceRepo;
        this.envService = envService;
    }

    //  Dynamic flag validation
    public boolean validateFlag(String username, String challengeId, String submittedFlag) {

        var instances = instanceRepo.findByUsernameAndChallengeIdAndStatus(
                username, challengeId, "RUNNING"
        );

        if (instances.isEmpty()) return false;

        ChallengeInstanceEntity inst = instances.get(0);

        String submittedHash = envService.sha256(submittedFlag);
        return submittedHash.equals(inst.getFlagHash());
    }

    //  Track solved challenges (optional)
    private final Map<String, Set<String>> solvedByUser = new ConcurrentHashMap<>();

    public boolean recordSolve(String username, String challengeId) {
        Set<String> solved = solvedByUser.computeIfAbsent(username,
                __ -> ConcurrentHashMap.newKeySet());
        return solved.add(challengeId); // true if NEW
    }

    public Set<String> getSolvedChallenges(String username) {
        return Collections.unmodifiableSet(
                solvedByUser.getOrDefault(username, Collections.emptySet())
        );
    }
}
