package at.fhtw.ctfbackend.integration;

import at.fhtw.ctfbackend.services.ChallengeService;
import at.fhtw.ctfbackend.services.FlagService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.jdbc.Sql;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

@AutoConfigureMockMvc
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class PerformanceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private ChallengeService challengeService;

    @Autowired
    private FlagService flagService;

    @Nested
    @DisplayName("Database Query Performance")
    class DatabasePerformanceTests {

        @Test
        @DisplayName("List all challenges performs in reasonable time")
        @Timeout(value = 2, unit = TimeUnit.SECONDS)
        void listChallenges_Performance() {
            for (int i = 0; i < 100; i++) {
                challengeService.listAll();
            }
        }

        @Test
        @DisplayName("Admin stats query performs in reasonable time")
        @Timeout(value = 3, unit = TimeUnit.SECONDS)
        void adminStats_Performance() {
            for (int i = 0; i < 50; i++) {
                challengeService.getAdminStats();
            }
        }

        @Test
        @DisplayName("Get ZIP file performs in reasonable time")
        @Timeout(value = 1, unit = TimeUnit.SECONDS)
        void getZip_Performance() {
            for (int i = 0; i < 50; i++) {
                challengeService.getZip("web-101");
            }
        }
    }

    @Nested
    @DisplayName("Concurrent User Load Tests")
    class ConcurrentLoadTests {

        @Test
        @DisplayName("100 concurrent users listing challenges")
        void concurrentUsers_ListChallenges() throws InterruptedException, ExecutionException {
            int userCount = 100;
            ExecutorService executor = Executors.newFixedThreadPool(userCount);
            List<Future<Boolean>> futures = new ArrayList<>();

            for (int i = 0; i < userCount; i++) {
                Future<Boolean> future = executor.submit(() -> {
                    try {
                        challengeService.listAll();
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                });
                futures.add(future);
            }

            // All should succeed
            for (Future<Boolean> future : futures) {
                assertTrue(future.get(), "Concurrent list operation should succeed");
            }

            executor.shutdown();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }

        @Test
        @DisplayName("50 concurrent flag submissions")
        void concurrentUsers_SubmitFlags() throws InterruptedException, ExecutionException {
            int userCount = 50;
            ExecutorService executor = Executors.newFixedThreadPool(userCount);
            List<Future<Boolean>> futures = new ArrayList<>();

            for (int i = 0; i < userCount; i++) {
                final String user = "user" + i;
                Future<Boolean> future = executor.submit(() -> {
                    try {
                        boolean valid = flagService.validateFlag("web-101", "flag{leet_xss}");
                        if (valid) {
                            return flagService.recordSolve(user, "web-101");
                        }
                        return false;
                    } catch (Exception e) {
                        return false;
                    }
                });
                futures.add(future);
            }

            // Count successes
            long successCount = futures.stream()
                    .map(f -> {
                        try {
                            return f.get();
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .filter(result -> result)
                    .count();

            assertEquals(userCount, successCount, "All unique users should successfully record solves");

            executor.shutdown();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @Nested
    @DisplayName("Memory Usage Tests")
    class MemoryUsageTests {

        @Test
        @DisplayName("Creating many challenges doesn't cause memory leak")
        void createManyChallenges_NoMemoryLeak() {
            Runtime runtime = Runtime.getRuntime();
            long initialMemory = runtime.totalMemory() - runtime.freeMemory();

            // Create 100 challenges
            for (int i = 0; i < 100; i++) {
                challengeService.createChallenge(
                        "Challenge " + i,
                        "Description " + i,
                        "web",
                        "EASY",
                        100,
                        "flag{test" + i + "}",
                        ("content " + i).getBytes()
                );
            }

            System.gc(); // Suggest garbage collection
            long finalMemory = runtime.totalMemory() - runtime.freeMemory();

            // Memory increase should be reasonable (less than 100MB for 100 challenges)
            long memoryIncrease = finalMemory - initialMemory;
            assertTrue(memoryIncrease < 100 * 1024 * 1024,
                    "Memory increase should be reasonable: " + (memoryIncrease / 1024 / 1024) + "MB");
        }
    }

    @Nested
    @DisplayName("Large Dataset Tests")
    class LargeDatasetTests {

        @Test
        @DisplayName("System handles 1000 challenges")
        @Timeout(value = 30, unit = TimeUnit.SECONDS)
        void handle1000Challenges() {
            // Create 1000 challenges
            for (int i = 0; i < 1000; i++) {
                challengeService.createChallenge(
                        "Challenge " + i,
                        "Description",
                        "web",
                        "EASY",
                        100,
                        "flag{" + i + "}",
                        null
                );
            }

            // List all should still be reasonably fast
            long start = System.currentTimeMillis();
            var challenges = challengeService.listAll();
            long duration = System.currentTimeMillis() - start;

            assertEquals(1003, challenges.size()); // 3 seeded + 1000 created
            assertTrue(duration < 5000, "Listing 1000 challenges should take less than 5 seconds");
        }

        @Test
        @DisplayName("System handles 10000 flag submissions")
        @Timeout(value = 60, unit = TimeUnit.SECONDS)
        void handle10000FlagSubmissions() {
            // 100 users solving 100 challenges each
            for (int user = 0; user < 100; user++) {
                for (int challenge = 0; challenge < 100; challenge++) {
                    String challengeId = "challenge-" + challenge;
                    flagService.recordSolve("user" + user, challengeId);
                }
            }
            // Should complete within timeout
        }
    }

    @Nested
    @DisplayName("Response Time Tests")
    class ResponseTimeTests {

        @Test
        @DisplayName("99% of challenge list requests complete within 500ms")
        void listChallenges_P99ResponseTime() {
            List<Long> responseTimes = new ArrayList<>();

            for (int i = 0; i < 100; i++) {
                long start = System.nanoTime();
                challengeService.listAll();
                long duration = (System.nanoTime() - start) / 1_000_000; // Convert to ms
                responseTimes.add(duration);
            }

            responseTimes.sort(Long::compareTo);
            long p99 = responseTimes.get(98); // 99th percentile

            assertTrue(p99 < 500, "99% of requests should complete within 500ms, got: " + p99 + "ms");
        }

        @Test
        @DisplayName("Average flag validation time is under 100ms")
        void flagValidation_AverageResponseTime() {
            List<Long> responseTimes = new ArrayList<>();

            for (int i = 0; i < 100; i++) {
                long start = System.nanoTime();
                flagService.validateFlag("web-101", "flag{leet_xss}");
                long duration = (System.nanoTime() - start) / 1_000_000; // Convert to ms
                responseTimes.add(duration);
            }

            double average = responseTimes.stream()
                    .mapToLong(Long::longValue)
                    .average()
                    .orElse(0);

            assertTrue(average < 100, "Average validation time should be under 100ms, got: " + average + "ms");
        }
    }

    @Nested
    @DisplayName("Database Connection Pool Tests")
    class ConnectionPoolTests {

        @Test
        @DisplayName("System handles connection pool exhaustion gracefully")
        void connectionPoolExhaustion_HandledGracefully() throws InterruptedException {
            // Simulate many concurrent database operations
            int threadCount = 200; // More than typical connection pool size
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            List<Future<Boolean>> futures = new ArrayList<>();

            for (int i = 0; i < threadCount; i++) {
                Future<Boolean> future = executor.submit(() -> {
                    try {
                        challengeService.listAll();
                        return true;
                    } catch (Exception e) {
                        // Should not throw exceptions, should wait for available connection
                        return false;
                    }
                });
                futures.add(future);
            }

            // All should eventually succeed (or fail gracefully)
            long successCount = futures.stream()
                    .map(f -> {
                        try {
                            return f.get(30, TimeUnit.SECONDS);
                        } catch (Exception e) {
                            return false;
                        }
                    })
                    .filter(result -> result)
                    .count();

            // At least 90% should succeed
            assertTrue(successCount >= threadCount * 0.9,
                    "At least 90% of operations should succeed even under high load");

            executor.shutdown();
            assertTrue(executor.awaitTermination(40, TimeUnit.SECONDS));
        }
    }
}