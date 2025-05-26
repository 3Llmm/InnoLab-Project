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

        String user = auth.getName(); // your username from JWT
        String challengeId = request.getChallengeId();
        String flag = request.getFlag();
        System.out.println("Got into controller!");


        if (!flagService.validateFlag(challengeId, flag)) {
            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message", "Incorrect flag.",
                            "status",  "error"
                    ));
        }

        boolean isNew = flagService.recordSolve(user, challengeId);
        if (!isNew) {
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
