package at.fhtw.ctfbackend.services;

import at.fhtw.ctfbackend.entity.ChallengeInstanceEntity;
import at.fhtw.ctfbackend.repository.ChallengeInstanceRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class EnvironmentCleanupService {

    private final ChallengeInstanceRepository instanceRepo;
    private final DockerService dockerService;

    public EnvironmentCleanupService(ChallengeInstanceRepository instanceRepo,
                                     DockerService dockerService) {
        this.instanceRepo = instanceRepo;
        this.dockerService = dockerService;
    }

    // Run every minute
    @Scheduled(fixedDelay = 60000)
    public void cleanupExpired() {
        Instant now = Instant.now();

        var all = instanceRepo.findAll();
        for (ChallengeInstanceEntity inst : all) {
            if (inst.getStatus().equals("RUNNING") &&
                    inst.getExpiresAt().isBefore(now)) {

                System.out.println("Cleaning expired instance: " + inst.getInstanceId());

                dockerService.stopContainer(inst.getContainerName());

                inst.setStatus("EXPIRED");
                instanceRepo.save(inst);
            }
        }
    }
}
