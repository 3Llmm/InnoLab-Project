package at.fhtw.ctfbackend.service;

import at.fhtw.ctfbackend.entity.ChallengeEntity;
import at.fhtw.ctfbackend.models.Challenge;
import at.fhtw.ctfbackend.repository.ChallengeRepository;
import at.fhtw.ctfbackend.services.ChallengeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ChallengeService}.
 *
 * <p>Uses Mockito to simulate repository behavior and verifies that
 * the service correctly handles CRUD operations, data validation,
 * and statistics generation for CTF challenges.</p>
 */
@ExtendWith(MockitoExtension.class)
class ChallengeServiceTest {

    @Mock
    private ChallengeRepository repo;

    @InjectMocks
    private ChallengeService service;

    private ChallengeEntity entity1;
    private ChallengeEntity entity2;
    private byte[] zipContent;

    /**
     * Prepares mock entities and sample binary ZIP data before each test.
     */
    @BeforeEach
    void setUp() {
        zipContent = "fake-zip-content".getBytes();

        entity1 = new ChallengeEntity(
                "web-101",
                "XSS Challenge",
                "Find the XSS vulnerability",
                "web",
                "easy",
                100,
                zipContent,
                "flag{xss_found}"
        );

        entity2 = new ChallengeEntity(
                "crypto-201",
                "RSA Decryption",
                "Decrypt the message",
                "crypto",
                "medium",
                200,
                zipContent,
                "flag{rsa_cracked}"
        );
    }

    // region === Retrieval Tests ===

    /** Ensures that listAll() returns all available challenges. */
    @Test
    void listAll_ReturnsAllChallenges() {
        when(repo.findAll()).thenReturn(Arrays.asList(entity1, entity2));

        List<Challenge> result = service.listAll();

        assertEquals(2, result.size());
        assertEquals("XSS Challenge", result.get(0).getTitle());
        assertEquals("RSA Decryption", result.get(1).getTitle());
        verify(repo, times(1)).findAll();
    }

    /** Ensures that listAll() returns an empty list when repository is empty. */
    @Test
    void listAll_EmptyRepository_ReturnsEmptyList() {
        when(repo.findAll()).thenReturn(Collections.emptyList());

        List<Challenge> result = service.listAll();

        assertTrue(result.isEmpty());
        verify(repo, times(1)).findAll();
    }

    /** Verifies successful retrieval of ZIP file for a valid challenge ID. */
    @Test
    void getZip_ValidChallengeId_ReturnsZipBytes() {
        when(repo.findById("web-101")).thenReturn(Optional.of(entity1));

        byte[] result = service.getZip("web-101");

        assertArrayEquals(zipContent, result);
        verify(repo, times(1)).findById("web-101");
    }

    /** Ensures getZip() throws an exception when the challenge does not exist. */
    @Test
    void getZip_InvalidChallengeId_ThrowsException() {
        when(repo.findById("invalid")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.getZip("invalid"));

        assertTrue(exception.getMessage().contains("Challenge not found"));
        verify(repo, times(1)).findById("invalid");
    }

    // endregion


    // region === Creation Tests ===

    /** Verifies that a challenge is created successfully with valid data. */
    @Test
    void createChallenge_ValidData_ReturnsCreatedChallenge() {
        when(repo.save(any(ChallengeEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        Challenge result = service.createChallenge(
                "New Challenge",
                "Test description",
                "web",
                "hard",
                300,
                "flag{test}",
                zipContent
        );

        assertNotNull(result);
        assertEquals("New Challenge", result.getTitle());
        assertEquals("web", result.getCategory());
        assertEquals("hard", result.getDifficulty());
        assertEquals(300, result.getPoints());
        assertTrue(result.getId().startsWith("new-challenge-"));
        verify(repo, times(1)).save(any(ChallengeEntity.class));
    }

    /** Ensures creation succeeds even if ZIP file is null. */
    @Test
    void createChallenge_WithNullZip_CreatesSuccessfully() {
        when(repo.save(any(ChallengeEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        Challenge result = service.createChallenge(
                "No Zip Challenge",
                "Description",
                "misc",
                "easy",
                50,
                "flag{no_zip}",
                null
        );

        assertNotNull(result);
        assertEquals("No Zip Challenge", result.getTitle());
        verify(repo, times(1)).save(any(ChallengeEntity.class));
    }

    // endregion


    // region === Update Tests ===

    /** Tests that updateChallenge() correctly updates all fields when provided. */
    @Test
    void updateChallenge_AllFields_UpdatesSuccessfully() {
        when(repo.findById("web-101")).thenReturn(Optional.of(entity1));
        when(repo.save(any(ChallengeEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        Challenge result = service.updateChallenge(
                "web-101",
                "Updated Title",
                "Updated Description",
                "pwn",
                "hard",
                500,
                "flag{updated}",
                "new-zip".getBytes()
        );

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals("pwn", result.getCategory());
        assertEquals("hard", result.getDifficulty());
        assertEquals(500, result.getPoints());
        verify(repo, times(1)).findById("web-101");
        verify(repo, times(1)).save(any(ChallengeEntity.class));
    }

    /** Verifies that only provided fields are updated, others remain unchanged. */
    @Test
    void updateChallenge_PartialFields_UpdatesOnlyProvided() {
        when(repo.findById("web-101")).thenReturn(Optional.of(entity1));
        when(repo.save(any(ChallengeEntity.class))).thenAnswer(i -> i.getArguments()[0]);

        Challenge result = service.updateChallenge(
                "web-101",
                "New Title",
                null,
                null,
                null,
                null,
                null,
                null
        );

        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
        verify(repo, times(1)).save(any(ChallengeEntity.class));
    }

    /** Ensures exception is thrown when updating a non-existent challenge. */
    @Test
    void updateChallenge_InvalidId_ThrowsException() {
        when(repo.findById("invalid")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.updateChallenge("invalid", "Title", null, null, null, null, null, null));

        assertTrue(exception.getMessage().contains("Challenge not found"));
        verify(repo, times(1)).findById("invalid");
        verify(repo, never()).save(any());
    }

    // endregion


    // region === Deletion Tests ===

    /** Ensures successful deletion for existing challenge ID. */
    @Test
    void deleteChallenge_ExistingId_DeletesSuccessfully() {
        when(repo.existsById("web-101")).thenReturn(true);
        doNothing().when(repo).deleteById("web-101");

        assertDoesNotThrow(() -> service.deleteChallenge("web-101"));

        verify(repo, times(1)).existsById("web-101");
        verify(repo, times(1)).deleteById("web-101");
    }

    /** Ensures deleteChallenge() throws an exception when challenge doesn't exist. */
    @Test
    void deleteChallenge_NonExistingId_ThrowsException() {
        when(repo.existsById("invalid")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> service.deleteChallenge("invalid"));

        assertTrue(exception.getMessage().contains("Challenge not found"));
        verify(repo, times(1)).existsById("invalid");
        verify(repo, never()).deleteById(any());
    }

    // endregion


    // region === Statistics Tests ===

    /**
     * Verifies that getAdminStats() returns correct totals and category/difficulty breakdowns.
     */
    @Test
    void getAdminStats_ReturnsCorrectStatistics() {
        when(repo.count()).thenReturn(5L);
        when(repo.countChallengesByCategory()).thenReturn(Arrays.asList(
                new Object[]{"web", 2L},
                new Object[]{"crypto", 3L}
        ));
        when(repo.countChallengesByDifficulty()).thenReturn(Arrays.asList(
                new Object[]{"easy", 2L},
                new Object[]{"medium", 2L},
                new Object[]{"hard", 1L}
        ));

        Map<String, Object> stats = service.getAdminStats();

        assertEquals(5L, stats.get("totalChallenges"));
        assertEquals(5L, stats.get("activeChallenges"));
        assertEquals("N/A", stats.get("totalUsers"));
        assertEquals("N/A", stats.get("totalSubmissions"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> byCategory = (List<Map<String, Object>>) stats.get("challengesByCategory");
        assertEquals(2, byCategory.size());
        assertEquals("web", byCategory.get(0).get("category"));
        assertEquals(2L, byCategory.get(0).get("count"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> byDifficulty = (List<Map<String, Object>>) stats.get("challengesByDifficulty");
        assertEquals(3, byDifficulty.size());
        assertEquals("easy", byDifficulty.get(0).get("difficulty"));
        assertEquals(2L, byDifficulty.get(0).get("count"));

        verify(repo, times(1)).count();
        verify(repo, times(1)).countChallengesByCategory();
        verify(repo, times(1)).countChallengesByDifficulty();
    }

    /** Ensures getAdminStats() handles an empty repository gracefully. */
    @Test
    void getAdminStats_EmptyRepository_ReturnsZeroStats() {
        when(repo.count()).thenReturn(0L);
        when(repo.countChallengesByCategory()).thenReturn(Collections.emptyList());
        when(repo.countChallengesByDifficulty()).thenReturn(Collections.emptyList());

        Map<String, Object> stats = service.getAdminStats();

        assertEquals(0L, stats.get("totalChallenges"));
        assertEquals(0L, stats.get("activeChallenges"));

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> byCategory = (List<Map<String, Object>>) stats.get("challengesByCategory");
        assertTrue(byCategory.isEmpty());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> byDifficulty = (List<Map<String, Object>>) stats.get("challengesByDifficulty");
        assertTrue(byDifficulty.isEmpty());
    }

    // endregion
}
