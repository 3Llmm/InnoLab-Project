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
import java.util.List;
import java.util.Map;

@Configuration
public class DataLoader {

    private static final Map<String, String> FLAGS = Map.of(
            "web-101", "flag{leet_xss}",
            "rev-201", "flag{reverse_master}"
    );

    private static final Map<String, String> TITLES = Map.of(
            "web-101", "Basic Web Exploit",
            "rev-201", "Reverse Engineering Task"
    );

    private static final Map<String, String> DESCRIPTIONS = Map.of(
            "web-101", "Find the flag hidden in the login form.",
            "rev-201", "Analyze the binary and extract the flag."
    );

    private static final Map<String, String> CATEGORIES = Map.of(
            "web-101", "web-exploitation",
            "rev-201", "reverse-engineering"
    );

    private static final Map<String, String> DIFFICULTIES = Map.of(
            "web-101", "easy",
            "rev-201", "medium"
    );

    private static final Map<String, Integer> POINTS = Map.of(
            "web-101", 100,
            "rev-201", 200
    );

    @Bean
    CommandLineRunner initDatabase(ChallengeRepository repo) {
        return args -> {
            var resolver = new PathMatchingResourcePatternResolver();
            Resource[] resources = resolver.getResources("classpath:/files/*.zip");

            for (Resource r : resources) {
                String filename = r.getFilename();
                String id = filename.replace(".zip", "");

                if (repo.existsById(id)) continue;

                byte[] zipBytes;
                try (InputStream in = r.getInputStream()) {
                    zipBytes = in.readAllBytes();
                }

                ChallengeEntity entity = new ChallengeEntity(
                        id,
                        TITLES.getOrDefault(id, id),
                        DESCRIPTIONS.getOrDefault(id, ""),
                        CATEGORIES.getOrDefault(id, "web-exploitation"),
                        DIFFICULTIES.getOrDefault(id, "easy"),
                        POINTS.getOrDefault(id, 100),
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
