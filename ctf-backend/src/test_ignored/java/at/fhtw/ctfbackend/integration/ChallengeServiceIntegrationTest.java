package at.fhtw.ctfbackend.integration;

import at.fhtw.ctfbackend.entity.ChallengeEntity;
import at.fhtw.ctfbackend.models.Challenge;
import at.fhtw.ctfbackend.repository.ChallengeRepository;
import at.fhtw.ctfbackend.services.ChallengeService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.jdbc.Sql;


import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureMockMvc
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    class ChallengeServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Nested
    @DisplayName("Challenge Listing and Retrieval")
    class ChallengeListingScenarios {

        @Test
        @DisplayName("List all seeded challenges")
        void listAll_ReturnsAllSeededChallenges() {
            List<Challenge> challenges = challengeService.listAll();
            assertNotNull(challenges);
            assertEquals(3, challenges.size());
        }

        @Test
        @DisplayName("Verify challenge details are correctly mapped")
        void listAll_MapsDetailsCorrectly() {
            Challenge webChallenge = challengeService.listAll().stream()
                    .filter(c -> c.getId().equals("web-101"))
                    .findFirst()
                    .orElseThrow();
            assertEquals("Intro to Web Exploitation", webChallenge.getTitle());
        }

        @Test
        @DisplayName("Get ZIP file for existing challenge")
        void getZip_ExistingChallenge_ReturnsZipBytes() {
            byte[] zipBytes = challengeService.getZip("web-101");
            assertNotNull(zipBytes);
        }

        @Test
        @DisplayName("Get ZIP file for non-existent challenge throws exception")
        void getZip_NonExistentChallenge_ThrowsException() {
            assertThrows(RuntimeException.class,
                    () -> challengeService.getZip("non-existent-id"));
        }
    }


    @Nested
    @DisplayName("Challenge Creation")
    class ChallengeCreationScenarios {

        @Test
        @DisplayName("Create new challenge with all fields")
        void createChallenge_AllFields_Success() {
            byte[] testZip = "test zip content".getBytes();

            Challenge created = challengeService.createChallenge(
                    "New Web Challenge",
                    "Test description",
                    "web",
                    "MEDIUM",
                    250,
                    "flag{test}",
                    testZip
            );

            assertNotNull(created);
            assertNotNull(created.getId());
            assertTrue(created.getId().startsWith("new-web-challenge"));
            assertEquals("New Web Challenge", created.getTitle());
            assertEquals("Test description", created.getDescription());
            assertEquals("web", created.getCategory());
            assertEquals("MEDIUM", created.getDifficulty());
            assertEquals(250, created.getPoints());

            // Verify it's saved in DB
            assertTrue(challengeRepository.existsById(created.getId()));
        }

        @Test
        @DisplayName("Create challenge without ZIP file")
        void createChallenge_WithoutZip_Success() {
            Challenge created = challengeService.createChallenge(
                    "Theory Challenge",
                    "No files needed",
                    "crypto",
                    "EASY",
                    50,
                    "flag{theory}",
                    null
            );

            assertNotNull(created);
            assertNotNull(created.getId());

            // Instead of testing getZip directly, verify through repository
            ChallengeEntity entity = challengeRepository.findById(created.getId())
                    .orElseThrow();
            assertNull(entity.getDownloadZip());
        }


        @Test
        @DisplayName("Create challenge generates unique ID")
        void createChallenge_GeneratesUniqueId() {
            Challenge first = challengeService.createChallenge(
                    "Same Title",
                    "First",
                    "web",
                    "EASY",
                    100,
                    "flag{1}",
                    null
            );

            // Small delay to ensure different timestamp
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            Challenge second = challengeService.createChallenge(
                    "Same Title",
                    "Second",
                    "web",
                    "EASY",
                    100,
                    "flag{2}",
                    null
            );

            assertNotEquals(first.getId(), second.getId());
        }
    }

    @Nested
    @DisplayName("Challenge Update")
    class ChallengeUpdateScenarios {

        @Test
        @DisplayName("Update single field")
        void updateChallenge_SingleField_Success() {
            Challenge updated = challengeService.updateChallenge(
                    "web-101",
                    null,
                    "Updated description",
                    null,
                    null,
                    null,
                    null,
                    null
            );

            assertEquals("web-101", updated.getId());
            assertEquals("Intro to Web Exploitation", updated.getTitle()); // unchanged
            assertEquals("Updated description", updated.getDescription()); // changed
            assertEquals("web", updated.getCategory()); // unchanged
        }

        @Test
        @DisplayName("Update multiple fields")
        void updateChallenge_MultipleFields_Success() {
            Challenge updated = challengeService.updateChallenge(
                    "web-101",
                    "New Title",
                    "New Description",
                    null,
                    "HARD",
                    500,
                    null,
                    null
            );

            assertEquals("New Title", updated.getTitle());
            assertEquals("New Description", updated.getDescription());
            assertEquals("HARD", updated.getDifficulty());
            assertEquals(500, updated.getPoints());
            assertEquals("web", updated.getCategory()); // unchanged
        }

        @Test
        @DisplayName("Update ZIP file")
        void updateChallenge_UpdateZip_Success() {
            byte[] newZip = "new zip content".getBytes();

            challengeService.updateChallenge(
                    "web-101",
                    null, null, null, null, null, null,
                    newZip
            );

            byte[] retrievedZip = challengeService.getZip("web-101");
            assertArrayEquals(newZip, retrievedZip);
        }

        @Test
        @DisplayName("Update non-existent challenge throws exception")
        void updateChallenge_NonExistent_ThrowsException() {
            assertThrows(RuntimeException.class,
                    () -> challengeService.updateChallenge(
                            "non-existent",
                            "Title", null, null, null, null, null, null
                    ));
        }
    }

    @Nested
    @DisplayName("Challenge Deletion")
    class ChallengeDeletionScenarios {

        @Test
        @DisplayName("Delete existing challenge")
        void deleteChallenge_Existing_Success() {
            assertTrue(challengeRepository.existsById("web-101"));

            challengeService.deleteChallenge("web-101");

            assertFalse(challengeRepository.existsById("web-101"));
        }

        @Test
        @DisplayName("Delete non-existent challenge throws exception")
        void deleteChallenge_NonExistent_ThrowsException() {
            assertThrows(RuntimeException.class,
                    () -> challengeService.deleteChallenge("non-existent"));
        }

        @Test
        @DisplayName("Delete challenge and verify list updated")
        void deleteChallenge_ListUpdated() {
            List<Challenge> beforeDelete = challengeService.listAll();
            int initialCount = beforeDelete.size();

            challengeService.deleteChallenge("web-101");

            List<Challenge> afterDelete = challengeService.listAll();
            assertEquals(initialCount - 1, afterDelete.size());
            assertTrue(afterDelete.stream()
                    .noneMatch(c -> c.getId().equals("web-101")));
        }
    }

    @Nested
    @DisplayName("Admin Statistics")
    class AdminStatisticsScenarios {

        @Test
        @DisplayName("Get admin stats with seeded data")
        void getAdminStats_WithSeededData_ReturnsCorrectCounts() {
            Map<String, Object> stats = challengeService.getAdminStats();

            assertNotNull(stats);
            assertEquals(3L, stats.get("totalChallenges"));
            assertEquals(3L, stats.get("activeChallenges"));
        }

        @Test
        @DisplayName("Admin stats include challenges by category")
        void getAdminStats_IncludesCategoryBreakdown() {
            Map<String, Object> stats = challengeService.getAdminStats();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> byCategory =
                    (List<Map<String, Object>>) stats.get("challengesByCategory");

            assertNotNull(byCategory);
            assertTrue(byCategory.size() > 0);

            // Verify web category has 1 challenge
            Map<String, Object> webStats = byCategory.stream()
                    .filter(m -> "web".equals(m.get("category")))
                    .findFirst()
                    .orElseThrow();
            assertEquals(1L, webStats.get("count"));

            // Verify crypto category has 2 challenges
            Map<String, Object> cryptoStats = byCategory.stream()
                    .filter(m -> "crypto".equals(m.get("category")))
                    .findFirst()
                    .orElseThrow();
            assertEquals(2L, cryptoStats.get("count"));
        }

        @Test
        @DisplayName("Admin stats include challenges by difficulty")
        void getAdminStats_IncludesDifficultyBreakdown() {
            Map<String, Object> stats = challengeService.getAdminStats();

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> byDifficulty =
                    (List<Map<String, Object>>) stats.get("challengesByDifficulty");

            assertNotNull(byDifficulty);
            assertTrue(byDifficulty.size() > 0);

            // Verify difficulty counts
            long totalInBreakdown = byDifficulty.stream()
                    .mapToLong(m -> (Long) m.get("count"))
                    .sum();
            assertEquals(3L, totalInBreakdown);
        }

        @Test
        @DisplayName("Admin stats after adding new challenge")
        void getAdminStats_AfterAddingChallenge_UpdatesCounts() {
            challengeService.createChallenge(
                    "New Challenge",
                    "Test",
                    "forensics",
                    "EASY",
                    100,
                    "flag{test}",
                    null
            );

            Map<String, Object> stats = challengeService.getAdminStats();

            assertEquals(4L, stats.get("totalChallenges"));
            assertEquals(4L, stats.get("activeChallenges"));
        }
    }
}