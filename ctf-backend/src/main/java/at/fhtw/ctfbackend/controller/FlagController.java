package at.fhtw.ctfbackend.controller;

import at.fhtw.ctfbackend.dto.SubmitFlagRequestDto;
import at.fhtw.ctfbackend.repository.ChallengeInstanceRepository;
import at.fhtw.ctfbackend.services.EnvironmentService;
import at.fhtw.ctfbackend.services.FlagService;
import at.fhtw.ctfbackend.services.UserService;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/flags")
public class FlagController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(
        FlagController.class
    );
    private final FlagService flagService;
    private final EnvironmentService envService;
    private final ChallengeInstanceRepository instanceRepo;

    public FlagController(
        FlagService flagService,
        EnvironmentService envService,
        ChallengeInstanceRepository instanceRepo,
        UserService userService
    ) {
        this.flagService = flagService;
        this.envService = envService;
        this.instanceRepo = instanceRepo;
        this.userService = userService;
    }

    @PostMapping("/submit")
    @Transactional //  Add this annotation
    public ResponseEntity<Map<String, Object>> submitFlag(
        Authentication auth,
        @RequestBody SubmitFlagRequestDto request
    ) {
        String username = auth.getName();
        String challengeId = request.getChallengeId();
        String submittedFlag = request.getFlag();

        logger.debug("Got into controller!");

        // NEW: validate using dynamic instance flag
        boolean valid = flagService.validateFlag(
            username,
            challengeId,
            submittedFlag
        );

        if (!valid) {
            return ResponseEntity.badRequest().body(
                Map.of("message", "Incorrect flag.", "status", "error")
            );
        }

        // NEW: Check if user already solved this challenge
        boolean alreadySolved = flagService.hasUserSolvedChallenge(
            username,
            challengeId
        );

        if (alreadySolved) {
            return ResponseEntity.badRequest().body(
                Map.of(
                    "message",
                    "Flag already submitted.",
                    "status",
                    "warning"
                )
            );
        }

        // NEW: record that user solved the challenge
        boolean isNewSolve = flagService.recordSolve(username, challengeId);

        if (!isNewSolve) {
            return ResponseEntity.badRequest().body(
                Map.of("message", "Failed to record solve.", "status", "error")
            );
        }

        // Auto-cleanup dynamic challenge container after successful solve
        try {
            var instances = instanceRepo.findByUserAndChallengeIdAndStatus(
                userService.getRequiredUser(username),
                challengeId,
                "RUNNING"
            );
            if (!instances.isEmpty()) {
                String instanceId = instances.get(0).getInstanceId();
                envService.cleanupAndReleasePort(instanceId);
                logger.info(
                    "Auto-cleaned environment after solve: {}",
                    instanceId
                );
            }
        } catch (Exception cleanupEx) {
            logger.warn(
                "Failed to auto-cleanup environment after solve: {}",
                cleanupEx.getMessage()
            );
        }

        //  Get the updated solve count AFTER the transaction commits
        long solveCount = flagService.getSolveCountForChallenge(challengeId);
        int pointsEarned = flagService.getPointsEarned(username, challengeId);

        Map<String, Object> response = new java.util.HashMap<>();
        response.put("message", "Correct flag!");
        response.put("status", "success");
        response.put("solveCount", solveCount);
        response.put("pointsEarned", pointsEarned);

        return ResponseEntity.ok(response);
    }
}
