package at.fhtw.ctfbackend.services;

import at.fhtw.ctfbackend.dto.SolveResponse;
import at.fhtw.ctfbackend.entity.ChallengeEntity;
import at.fhtw.ctfbackend.entity.Solve;
import at.fhtw.ctfbackend.entity.UserEntity;
import at.fhtw.ctfbackend.repository.ChallengeRepository;
import at.fhtw.ctfbackend.repository.SolveRepository;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SolveService {

    private static final Logger logger = LoggerFactory.getLogger(SolveService.class);

    private final SolveRepository solveRepository;
    private final ChallengeRepository challengeRepository;
    private final UserService userService;

    public SolveService(
        SolveRepository solveRepository,
        ChallengeRepository challengeRepository,
        UserService userService
    ) {
        this.solveRepository = solveRepository;
        this.challengeRepository = challengeRepository;
        this.userService = userService;
    }

    @Transactional
    public boolean recordSolve(String username, String challengeId, int pointsEarned) {
        UserEntity user = userService.getRequiredUser(username);
        logger.debug(
            " SolveService.recordSolve called for user: {}, challenge: {}",
            username,
            challengeId
        );

        try {
            Optional<Solve> existingSolve = solveRepository.findByUserAndChallengeId(
                user,
                challengeId
            );

            if (existingSolve.isPresent()) {
                logger.debug("  User already solved this challenge, returning false");
                return false;
            }

            logger.debug(" This is a new solve, creating database record...");

            ChallengeEntity challenge = challengeRepository
                .findById(challengeId)
                .orElseThrow(() ->
                    new RuntimeException("Challenge not found: " + challengeId)
                );

            Solve solve = new Solve(user, challenge, pointsEarned);
            Solve savedSolve = solveRepository.saveAndFlush(solve);

            logger.debug(" Solve saved to database with ID: {}", savedSolve.getId());
            return true;
        } catch (Exception e) {
            logger.error(" Error in SolveService.recordSolve: {}", e.getMessage());
            logger.error("Error in SolveService.recordSolve", e);
            return false;
        }
    }

    public List<SolveResponse> getSolvedChallengesByUser(String username) {
        UserEntity user = userService.getRequiredUser(username);
        return solveRepository.findByUser(user).stream().map(this::toDto).toList();
    }

    public List<SolveResponse> getSolversForChallenge(String challengeId) {
        return solveRepository.findByChallengeId(challengeId).stream().map(this::toDto).toList();
    }

    public boolean hasUserSolvedChallenge(String username, String challengeId) {
        UserEntity user = userService.getRequiredUser(username);
        return solveRepository.findByUserAndChallengeId(user, challengeId).isPresent();
    }

    public long getSolveCountForChallenge(String challengeId) {
        logger.debug(" getSolveCountForChallenge called for: {}", challengeId);
        long countJpa = solveRepository.countByChallengeId(challengeId);
        logger.debug(" JPA Count result: {}", countJpa);
        long countNative = solveRepository.countByChallengeIdNative(challengeId);
        logger.debug(" Native Count result: {}", countNative);
        return countNative;
    }

    public long getSolveCountForUser(String username) {
        UserEntity user = userService.getRequiredUser(username);
        return solveRepository.countByUser(user);
    }

    public List<SolveResponse> getRecentSolves(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return solveRepository.findRecentSolves(pageable).stream().map(this::toDto).toList();
    }

    public Map<String, Long> getTopSolvers(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = solveRepository.findTopSolvers(pageable);
        return results.stream().collect(Collectors.toMap(result -> (String) result[0], result -> (Long) result[1]));
    }

    public Map<String, Long> getMostSolvedChallenges(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<Object[]> results = solveRepository.findMostSolvedChallenges(pageable);
        return results.stream().collect(Collectors.toMap(result -> (String) result[0], result -> (Long) result[1]));
    }

    public List<SolveResponse> getSolvesByCategory(String category) {
        return solveRepository.findByCategory(category).stream().map(this::toDto).toList();
    }

    public List<SolveResponse> getSolvesByDifficulty(String difficulty) {
        return solveRepository.findByDifficulty(difficulty).stream().map(this::toDto).toList();
    }

    public List<SolveResponse> getSolvesByTimeRange(LocalDateTime start, LocalDateTime end) {
        return solveRepository.findBySolvedAtBetween(start, end).stream().map(this::toDto).toList();
    }

    public long getTotalSolveCount() {
        return solveRepository.count();
    }

    public Optional<Solve> getSolveRecord(String username, String challengeId) {
        UserEntity user = userService.getRequiredUser(username);
        return solveRepository.findByUserAndChallengeId(user, challengeId);
    }

    public int getPointsEarned(String username, String challengeId) {
        UserEntity user = userService.getRequiredUser(username);
        return solveRepository.findByUserAndChallengeId(user, challengeId).map(Solve::getPointsEarned).orElse(0);
    }

    public void deleteSolve(Long solveId) {
        solveRepository.deleteById(solveId);
    }

    public Map<String, Object> getChallengeStatistics(String challengeId) {
        logger.debug(" getChallengeStatistics called for challenge: {}", challengeId);
        Map<String, Object> stats = new HashMap<>();
        long solveCount = getSolveCountForChallenge(challengeId);
        logger.debug(" Solve count from database: {}", solveCount);
        ChallengeEntity challenge = challengeRepository.findById(challengeId).orElseThrow(() -> new RuntimeException("Challenge not found: " + challengeId));
        stats.put("challengeId", challengeId);
        stats.put("challengeTitle", challenge.getTitle());
        stats.put("solveCount", solveCount);
        stats.put("category", challenge.getCategory());
        stats.put("difficulty", challenge.getDifficulty());
        stats.put("points", challenge.getPoints());
        stats.put("solveRate", "N/A");
        logger.debug(" Returning stats: {}", stats);
        return stats;
    }

    public Map<String, Object> getUserStatistics(String username) {
        Map<String, Object> stats = new HashMap<>();
        long totalSolves = getSolveCountForUser(username);
        List<SolveResponse> userSolves = getSolvedChallengesByUser(username);
        stats.put("username", username);
        stats.put("totalSolves", totalSolves);
        int totalPoints = userSolves.stream().mapToInt(SolveResponse::getPointsEarned).sum();
        stats.put("totalPoints", totalPoints);
        stats.put("categoryDistribution", Map.of());
        stats.put("difficultyDistribution", Map.of());
        return stats;
    }

    private SolveResponse toDto(Solve solve) {
        return new SolveResponse(
            solve.getId(),
            solve.getUsername(),
            solve.getChallenge().getId(),
            solve.getChallenge().getTitle(),
            solve.getSolvedAt(),
            solve.getPointsEarned()
        );
    }
}
