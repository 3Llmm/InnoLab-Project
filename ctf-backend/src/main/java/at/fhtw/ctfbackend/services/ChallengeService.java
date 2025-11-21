package at.fhtw.ctfbackend.services;

import at.fhtw.ctfbackend.entity.ChallengeEntity;
import at.fhtw.ctfbackend.models.Challenge;
import at.fhtw.ctfbackend.repository.ChallengeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;
import java.util.HashMap;
import java.util.Map;

@Service
public class ChallengeService {

    private final ChallengeRepository repo;

    public ChallengeService(ChallengeRepository repo) {
        this.repo = repo;
    }

    /**
     * List all challenges for the API.
     */
    public List<Challenge> listAll() {
        return repo.findAll().stream()
                .map(this::toModel)
                .collect(Collectors.toList());
    }

    /**
     * Load the file bytes for a given challenge.
     */
    @Transactional(readOnly = true)
    public byte[] getFile(String challengeId) {
        return repo.findById(challengeId)
                .map(ChallengeEntity::getDownload)
                .orElseThrow(() -> new RuntimeException("Challenge not found: " + challengeId));
    }

    @Transactional
    public Challenge createChallenge(String title, String description, String category,
                                     String difficulty, Integer points, String flag,
                                     byte[] download, String originalFilename, // ← ADD this parameter
                                     String dockerImageName,
                                     Integer defaultSshPort,
                                     Integer defaultVscodePort,
                                     Integer defaultDesktopPort,
                                     Boolean requiresInstance) {

        String challengeId = title.toLowerCase()
                .replaceAll("[^a-z0-9]", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "") + "-" + System.currentTimeMillis();

        ChallengeEntity entity = new ChallengeEntity(
                challengeId,
                title,
                description,
                category,
                difficulty,
                points,
                download,
                flag
        );

        entity.setOriginalFilename(originalFilename);
        entity.setDockerImageName(dockerImageName);
        entity.setDefaultSshPort(defaultSshPort);
        entity.setDefaultVscodePort(defaultVscodePort);
        entity.setDefaultDesktopPort(defaultDesktopPort);
        entity.setRequiresInstance(requiresInstance != null ? requiresInstance : false);

        ChallengeEntity savedEntity = repo.saveAndFlush(entity);
        return toModel(savedEntity);
    }

    @Transactional(readOnly = true)
    public String getOriginalFilename(String challengeId) {
        ChallengeEntity entity = repo.findById(challengeId)
                .orElseThrow(() -> new RuntimeException("Challenge not found: " + challengeId));
        return entity.getOriginalFilename();
    }

    /**
     * Update an existing challenge
     */
    public Challenge updateChallenge(String id, String title, String description, String category,
                                     String difficulty, Integer points, String flag,
                                     byte[] downloadZip) {

        ChallengeEntity existingEntity = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Challenge not found: " + id));

        // Update only the fields that are provided (not null)
        if (title != null) existingEntity.setTitle(title);
        if (description != null) existingEntity.setDescription(description);
        if (category != null) existingEntity.setCategory(category);
        if (difficulty != null) existingEntity.setDifficulty(difficulty);
        if (points != null) existingEntity.setPoints(points);
        if (flag != null) existingEntity.setFlag(flag);
        if (downloadZip != null) existingEntity.setDownload(downloadZip);

        ChallengeEntity updatedEntity = repo.save(existingEntity);
        return toModel(updatedEntity);
    }
    /**
     * Delete a challenge by ID
     */
    public void deleteChallenge(String id) {
        if (!repo.existsById(id)) {
            throw new RuntimeException("Challenge not found: " + id);
        }
        repo.deleteById(id);
    }
    /**
     * Get admin statistics
     */
    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();

        // Total challenges
        long totalChallenges = repo.count();
        stats.put("totalChallenges", totalChallenges);

        // Total users - placeholder for now
        stats.put("totalUsers", "N/A");

        // Total submissions - placeholder for now
        stats.put("totalSubmissions", "N/A");

        // Active challenges (same as total for now)
        stats.put("activeChallenges", totalChallenges);

        // Challenges by category
        List<Object[]> categoryCounts = repo.countChallengesByCategory();
        List<Map<String, Object>> byCategory = categoryCounts.stream()
                .map(result -> Map.of(
                        "category", result[0],
                        "count", result[1]
                ))
                .collect(Collectors.toList());
        stats.put("challengesByCategory", byCategory);

        // Challenges by difficulty
        List<Object[]> difficultyCounts = repo.countChallengesByDifficulty();
        List<Map<String, Object>> byDifficulty = difficultyCounts.stream()
                .map(result -> Map.of(
                        "difficulty", result[0],
                        "count", result[1]
                ))
                .collect(Collectors.toList());
        stats.put("challengesByDifficulty", byDifficulty);

        return stats;
    }
    @Transactional(readOnly = true)
    public Challenge getChallengeById(String id) {
        return repo.findById(id)
                .map(this::toModel)
                .orElseThrow(() -> new RuntimeException("Challenge not found: " + id));
    }
    // --- Internal mapping from entity to API model ---
    private Challenge toModel(ChallengeEntity e) {
        if (e.getId() == null) {
            throw new IllegalStateException("Entity has no ID — cannot map to model");
        }

        String downloadUrl = "http://localhost:8080/api/challenges/" + e.getId() + "/download";

        return new Challenge(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                e.getCategory(),
                e.getDifficulty(),
                e.getPoints(),
                downloadUrl,
                e.getOriginalFilename()
        );
    }


}
