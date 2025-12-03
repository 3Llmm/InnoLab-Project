package at.fhtw.ctfbackend.controller;

import at.fhtw.ctfbackend.entity.ChallengeEntity;
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

    @GetMapping("/{id}")
    public ResponseEntity<Challenge> getChallenge(@PathVariable String id) {
        try {
            Challenge challenge = challengeService.getChallengeById(id);
            return ResponseEntity.ok(challenge);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<ByteArrayResource> download(@PathVariable String id) {
        // Get the challenge entity to access both file and filename
        Challenge challenge = challengeService.getChallengeById(id);
        byte[] data = challengeService.getFile(id);
        String filename = challengeService.getOriginalFilename(id);

        // Debug logging
        System.out.println("DEBUG: Challenge ID = " + id);
        System.out.println("DEBUG: Retrieved filename = '" + filename + "'");
        System.out.println("DEBUG: Is null? " + (filename == null));
        System.out.println("DEBUG: Is empty? " + (filename != null && filename.trim().isEmpty()));

        ByteArrayResource resource = new ByteArrayResource(data);

        // Better fallback logic
        if (filename == null || filename.trim().isEmpty()) {
            System.out.println("DEBUG: Falling back to file extension detection");
            String fileExtension = determineFileExtension(data);
            filename = id + fileExtension;
        }

        System.out.println("DEBUG: Final filename = '" + filename + "'");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
    // Helper method to determine file extension from content
    private String determineFileExtension(byte[] data) {
        if (data == null || data.length < 4) return "";

        // Check for ZIP file signature
        if (data.length >= 4 && data[0] == 0x50 && data[1] == 0x4B && data[2] == 0x03 && data[3] == 0x04) {
            return ".zip";
        }
        // Check for PDF
        if (data.length >= 4 && data[0] == 0x25 && data[1] == 0x50 && data[2] == 0x44 && data[3] == 0x46) {
            return ".pdf";
        }
        // Check for JPG
        if (data.length >= 3 && data[0] == (byte)0xFF && data[1] == (byte)0xD8 && data[2] == (byte)0xFF) {
            return ".jpg";
        }
        // Check for PNG
        if (data.length >= 8 && data[0] == (byte)0x89 && data[1] == 0x50 && data[2] == 0x4E && data[3] == 0x47) {
            return ".png";
        }

        return ""; // Unknown type
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Challenge> createChallenge(
            @RequestParam String title,
            @RequestParam String description,
            @RequestParam String category,
            @RequestParam String difficulty,
            @RequestParam Integer points,
            @RequestParam String flag,
            @RequestParam(required = false) MultipartFile file,
            @RequestParam(required = false) String dockerImageName,
            @RequestParam(required = false) Integer defaultSshPort,
            @RequestParam(required = false) Integer defaultVscodePort,
            @RequestParam(required = false) Integer defaultDesktopPort,
            @RequestParam(required = false) Boolean requiresInstance) {

        try {
            byte[] fileBytes = file.getBytes();

            Challenge createdChallenge = challengeService.createChallenge(
                    title, description, category, difficulty, points, flag, fileBytes, file.getOriginalFilename(),
                    dockerImageName, defaultSshPort, defaultVscodePort, defaultDesktopPort, requiresInstance
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
