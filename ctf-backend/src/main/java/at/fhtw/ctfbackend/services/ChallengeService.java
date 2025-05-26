package at.fhtw.ctfbackend.services;

import at.fhtw.ctfbackend.entity.ChallengeEntity;
import at.fhtw.ctfbackend.models.Challenge;
import at.fhtw.ctfbackend.repository.ChallengeRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
     * Load the ZIP bytes for a given challenge.
     */
    public byte[] getZip(String challengeId) {
        return repo.findById(challengeId)
                .map(ChallengeEntity::getDownloadZip)
                .orElseThrow(() -> new RuntimeException("Challenge not found: " + challengeId));
    }

    // --- Internal mapping from entity to API model ---
    private Challenge toModel(ChallengeEntity e) {
        String downloadUrl = "http://localhost:8080/api/challenges/" + e.getId() + "/download";
        return new Challenge(
                e.getId(),
                e.getTitle(),
                e.getDescription(),
                downloadUrl
        );
    }

}
