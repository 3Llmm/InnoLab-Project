package at.fhtw.ctfbackend.services;

import at.fhtw.ctfbackend.entity.ChallengeInstanceEntity;
import at.fhtw.ctfbackend.repository.ChallengeInstanceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class EnvironmentCleanupService {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentCleanupService.class);

    private final ChallengeInstanceRepository instanceRepo;
    private final EnvironmentService envService;

    public EnvironmentCleanupService(ChallengeInstanceRepository instanceRepo,
                                     EnvironmentService envService) {
        this.instanceRepo = instanceRepo;
        this.envService = envService;
    }

    // Run every minute
    @Scheduled(fixedDelay = 60000)
    public void cleanupExpired() {
        Instant now = Instant.now();

        var all = instanceRepo.findAll();
        for (ChallengeInstanceEntity inst : all) {
            if ("RUNNING".equals(inst.getStatus()) && inst.getExpiresAt().isBefore(now)) {
                logger.info("Cleaning expired instance: {}", inst.getInstanceId());
                envService.cleanupAndReleasePort(inst.getInstanceId());
            }
        }
    }
}
