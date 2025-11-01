package at.fhtw.ctfbackend.controller;

import at.fhtw.ctfbackend.models.Challenge;
import at.fhtw.ctfbackend.services.ChallengeService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Challenge> createChallenge(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam String difficulty,
            @RequestParam Integer points,
            @RequestParam String flag,
            @RequestParam MultipartFile file) {

        try {
            byte[] fileBytes = file.getBytes();

            Challenge createdChallenge = challengeService.createChallenge(
                    title, description, category, difficulty, points, flag, fileBytes
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(createdChallenge);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Challenge> updateChallenge(
            @PathVariable String id,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Integer points,
            @RequestParam(required = false) String flag,
            @RequestParam(required = false) MultipartFile file) {

        try {
            byte[] fileBytes = null;
            if (file != null && !file.isEmpty()) {
                fileBytes = file.getBytes();
            }

            Challenge updatedChallenge = challengeService.updateChallenge(
                    id, title, description, category, difficulty, points, flag, fileBytes
            );

            return ResponseEntity.ok(updatedChallenge);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteChallenge(@PathVariable String id) {
        try {
            challengeService.deleteChallenge(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    @GetMapping("/admin/stats")
    public Map<String, Object> getAdminStats() {
        return challengeService.getAdminStats();
    }
}
