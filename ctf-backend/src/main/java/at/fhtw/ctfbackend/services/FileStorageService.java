package at.fhtw.ctfbackend.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

@Service
public class FileStorageService {

    @Value("${ctf.challenges.dir}")
    private String challengesDir;

    /*
    saves challenge image in challenge dir
    returns path
     */
    public String storeChallengeImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        try {
            // make sure challenge dir exists
            Path challengesPath = Paths.get(challengesDir)
                    .toAbsolutePath()
                    .normalize();
            Files.createDirectories(challengesPath);

            // check filename (no shell injection etc)
            String originalFilename = Objects.requireNonNullElse(
                    file.getOriginalFilename(),
                    "machine.img"
            );
            String sanitized = originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
            String newFileName = System.currentTimeMillis() + "_" + sanitized;

            // save
            Path targetLocation = challengesPath.resolve(newFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return "challenges/" + newFileName;

        } catch (IOException e) {
            throw new RuntimeException("Could not store challenge image", e);
        }
    }
}
