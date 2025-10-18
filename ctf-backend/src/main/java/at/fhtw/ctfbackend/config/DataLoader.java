package at.fhtw.ctfbackend.config;

import at.fhtw.ctfbackend.entity.CategoryEntity;
import at.fhtw.ctfbackend.entity.ChallengeEntity;
import at.fhtw.ctfbackend.external.ConfluenceClient;
import at.fhtw.ctfbackend.repository.CategoryRepository;
import at.fhtw.ctfbackend.repository.ChallengeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;

@Configuration
public class DataLoader {

    // Map IDs to flags and metadata for the prototype
    private static final Map<String, String> FLAGS = Map.of(
            "web-101", "flag{leet_xss}",
            "rev-201", "flag{reverse_master}",
            "file-102", "flag{example_flag}"

    );
    private static final Map<String, String> TITLES = Map.of(
            "web-101", "Basic Web Exploit",
            "rev-201", "Reverse Engineering Task",
            "file-102", "Trail of Bits Web"

    );
    private static final Map<String, String> DESCRIPTIONS = Map.of(
            "web-101", "Find the flag hidden in the login form.",
            "rev-201", "Analyze the binary and extract the flag.",
            "file-102", "Imported from the Trail of Bits GitHub repo"

    );

    @Bean
    CommandLineRunner initDatabase(ChallengeRepository repo) {
        return args -> {
            var resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:/files/*.zip");

            for (Resource r : resources) {
                String filename = r.getFilename();                  // e.g. "web-101.zip"
                String id = filename.replace(".zip", "");           // e.g. "web-101"

                // only load once
                if (repo.existsById(id)) continue;

                byte[] zipBytes;
                try (InputStream in = r.getInputStream()) {
                    zipBytes = in.readAllBytes();
                }

                ChallengeEntity entity = new ChallengeEntity(
                        id,
                        TITLES.getOrDefault(id, id),
                        DESCRIPTIONS.getOrDefault(id, ""),
                        zipBytes,
                        FLAGS.getOrDefault(id, "")
                );



                repo.save(entity);
            }
        };
    }

    @Bean
    CommandLineRunner loadCategories(CategoryRepository repo, ConfluenceClient confluenceClient) {
        return args -> {
                // Map your category IDs to their actual Confluence page IDs
                Map<String, String> confluencePageIds = Map.of(
                        "crypto", "60784643",
                        "web", "61603886",
                        "pwn", "61603873",
                        "forensics", "61505537"
                );

                List<CategoryEntity> categories = List.of(
                        new CategoryEntity("crypto", "Cryptography", "", ""),
                        new CategoryEntity("web", "Web-Exploitation", "", ""),
                        new CategoryEntity("pwn", "Binary-Exploitation", "", ""),
                        new CategoryEntity("forensics", "Forensic", "", "")
                );

                for (CategoryEntity category : categories) {
                    String pageId = confluencePageIds.get(category.getId());
                    if (pageId != null) {
                        String summary = confluenceClient.fetchSummaryFromConfluence(pageId);
                        String fileUrl = "https://technikum-wien-team-kjev9g23.atlassian.net/wiki/spaces/C/pages/" + pageId;

                        category.setSummary(summary);
                        category.setFileUrl(fileUrl);
                    } else {
                        category.setSummary("No summary available.");
                        category.setFileUrl("");
                    }
                }

                repo.saveAll(categories);

        };
    }

}