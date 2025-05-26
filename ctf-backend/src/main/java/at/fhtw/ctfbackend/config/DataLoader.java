package at.fhtw.ctfbackend.config;

import at.fhtw.ctfbackend.entity.CategoryEntity;
import at.fhtw.ctfbackend.entity.ChallengeEntity;
import at.fhtw.ctfbackend.entity.FileEntity;
import at.fhtw.ctfbackend.external.ConfluenceClient;
import at.fhtw.ctfbackend.repository.CategoryRepository;
import at.fhtw.ctfbackend.repository.ChallengeRepository;
import at.fhtw.ctfbackend.repository.FileRepository;
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

//    @Bean
//    CommandLineRunner initDatabase(CategoryRepository categoryRepo, FileRepository fileRepo, ConfluenceClient confluenceClient) {
//        return args -> {
//            // Save files
//            var resolver = new PathMatchingResourcePatternResolver();
//            Resource[] resources = resolver.getResources("classpath:/files/*.zip");
//
//            for (Resource r : resources) {
//                String filename = r.getFilename();
//                String id = filename.replace(".zip", "");
//
//                if (!fileRepo.existsById(id)) {
//                    byte[] content;
//                    try (InputStream in = r.getInputStream()) {
//                        content = in.readAllBytes();
//                    }
//
//                    FileEntity fileEntity = new FileEntity();
//                    fileEntity.setId(id);
//                    fileEntity.setFileName(filename);
//                    fileEntity.setContent(content);
//
//                    fileRepo.save(fileEntity);
//                }
//            }
//
//            // Save categories
//            if (categoryRepo.count() == 0) {
//                Map<String, String> confluencePageIds = Map.of(
//                        "crypto", "60784643",
//                        "web", "61603886",
//                        "pwn", "61603873",
//                        "forensics", "61505537"
//                );
//
//                List<CategoryEntity> categories = List.of(
//                        new CategoryEntity("crypto", "Cryptography", "", ""),
//                        new CategoryEntity("web", "Web-Exploitation", "", ""),
//                        new CategoryEntity("pwn", "Binary-Exploitation", "", ""),
//                        new CategoryEntity("forensics", "Forensic", "", "")
//                );
//
//                for (CategoryEntity category : categories) {
//                    String pageId = confluencePageIds.get(category.getId());
//                    if (pageId != null) {
//                        String summary = confluenceClient.fetchSummaryFromConfluence(pageId);
//                        String fileUrl = "https://technikum-wien-team-kjev9g23.atlassian.net/wiki/spaces/C/pages/" + pageId;
//
//                        category.setSummary(summary);
//                        category.setFileUrl(fileUrl);
//                    } else {
//                        category.setSummary("No summary available.");
//                        category.setFileUrl("");
//                    }
//                }
//
//                categoryRepo.saveAll(categories);
//            }
//        };
//    }

}
