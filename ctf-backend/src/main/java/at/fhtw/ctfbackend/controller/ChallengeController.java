package at.fhtw.ctfbackend.controller;

import at.fhtw.ctfbackend.models.Challenge;
import at.fhtw.ctfbackend.services.ChallengeService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/challenges")
public class ChallengeController {

    private final ChallengeService challengeService;

    public ChallengeController(ChallengeService challengeService) {
        this.challengeService = challengeService;
    }

    @GetMapping
    public List<Challenge> getChallenges() {
        return challengeService.listAll();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> download(@PathVariable String id) {
        byte[] data = challengeService.getZip(id);
        ByteArrayResource resource = new ByteArrayResource(data);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + id + ".zip\"")
                .body(resource);
    }
}
