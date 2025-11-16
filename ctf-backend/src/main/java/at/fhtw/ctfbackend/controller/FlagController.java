package at.fhtw.ctfbackend.controller;

import at.fhtw.ctfbackend.models.SubmitFlagRequest;
import at.fhtw.ctfbackend.services.FlagService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/flags")
public class FlagController {

    private final FlagService flagService;

    public FlagController(FlagService flagService) {
        this.flagService = flagService;
    }

    @PostMapping("/submit")
    public ResponseEntity<Map<String, String>> submitFlag(
            Authentication auth,
            @RequestBody SubmitFlagRequest request) {

        String username = auth.getName(); // FH username from JWT
        String challengeId = request.getChallengeId();
        String submittedFlag = request.getFlag();

        System.out.println("Got into controller!");

        // NEW: validate using dynamic instance flag
        boolean valid = flagService.validateFlag(username, challengeId, submittedFlag);

        if (!valid) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message", "Incorrect flag.",
                            "status",  "error"
                    ));
        }

        // NEW: record that user solved the challenge (optional)
        boolean isNewSolve = flagService.recordSolve(username, challengeId);

        if (!isNewSolve) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message", "Flag already submitted.",
                            "status",  "warning"
                    ));
        }

        return ResponseEntity.ok(Map.of(
                "message", "Correct flag!",
                "status",  "success"
        ));
    }
}
