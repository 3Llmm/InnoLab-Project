package at.fhtw.ctfbackend.controller;

import at.fhtw.ctfbackend.config.GlobalMockConfig;
import at.fhtw.ctfbackend.models.Challenge;
import at.fhtw.ctfbackend.services.ChallengeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ChallengeController.class)
@Import(GlobalMockConfig.class)
@WithMockUser
class ChallengeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChallengeService challengeService;

    @Test
    void getChallenges_ReturnsListOfChallenges() throws Exception {
        // Arrange
        List<Challenge> challenges = Arrays.asList(
                new Challenge("web-101", "XSS Challenge", "Find XSS", "web", "easy", 100, "http://localhost/download"),
                new Challenge("crypto-201", "RSA", "Decrypt", "crypto", "medium", 200, "http://localhost/download")
        );
        when(challengeService.listAll()).thenReturn(challenges);

        // Act & Assert
        mockMvc.perform(get("/api/challenges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("web-101"))
                .andExpect(jsonPath("$[0].title").value("XSS Challenge"))
                .andExpect(jsonPath("$[1].id").value("crypto-201"));

        verify(challengeService, times(1)).listAll();
    }

    @Test
    void getChallenges_EmptyList_ReturnsEmptyArray() throws Exception {
        // Arrange
        when(challengeService.listAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/challenges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(challengeService, times(1)).listAll();
    }

    @Test
    void download_ValidId_ReturnsZipFile() throws Exception {
        // Arrange
        String challengeId = "web-101";
        byte[] zipData = "fake-zip-content".getBytes();
        when(challengeService.getZip(challengeId)).thenReturn(zipData);

        // Act & Assert
        mockMvc.perform(get("/api/challenges/{id}/download", challengeId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(header().string("Content-Disposition",
                        "attachment; filename=\"" + challengeId + ".zip\""))
                .andExpect(content().bytes(zipData));

        verify(challengeService, times(1)).getZip(challengeId);
    }

    @Test
    void download_InvalidId_ThrowsException() throws Exception {
        // Arrange
        String invalidId = "invalid";
        when(challengeService.getZip(invalidId))
                .thenThrow(new RuntimeException("Challenge not found: " + invalidId));

        // Act & Assert
        mockMvc.perform(get("/api/challenges/{id}/download", invalidId))
                .andExpect(status().isUnauthorized()); // Handled by GlobalExceptionHandler

        verify(challengeService, times(1)).getZip(invalidId);
    }

    @Test
    void createChallenge_ValidData_ReturnsCreated() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.zip", "application/zip", "zip-content".getBytes()
        );

        Challenge created = new Challenge(
                "test-123", "Test Challenge", "Description",
                "web", "easy", 100, "http://localhost/download"
        );

        when(challengeService.createChallenge(
                anyString(), anyString(), anyString(), anyString(), anyInt(), anyString(), any(byte[].class)
        )).thenReturn(created);

        // Act & Assert
        mockMvc.perform(multipart("/api/challenges")
                        .file(file)
                        .param("title", "Test Challenge")
                        .param("description", "Description")
                        .param("category", "web")
                        .param("difficulty", "easy")
                        .param("points", "100")
                        .param("flag", "flag{test}")
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("test-123"))
                .andExpect(jsonPath("$.title").value("Test Challenge"));

        verify(challengeService, times(1)).createChallenge(
                eq("Test Challenge"), eq("Description"), eq("web"),
                eq("easy"), eq(100), eq("flag{test}"), any(byte[].class)
        );
    }

    @Test
    void createChallenge_MissingFile_HandlesError() throws Exception {
        // Act & Assert
        mockMvc.perform(multipart("/api/challenges")
                        .param("title", "Test Challenge")
                        .param("description", "Description")
                        .param("category", "web")
                        .param("difficulty", "easy")
                        .param("points", "100")
                        .param("flag", "flag{test}")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(challengeService, never()).createChallenge(
                anyString(), anyString(), anyString(), anyString(), anyInt(), anyString(), any()
        );
    }

    @Test
    void updateChallenge_ValidData_ReturnsUpdated() throws Exception {
        // Arrange
        String id = "web-101";
        MockMultipartFile file = new MockMultipartFile(
                "file", "updated.zip", "application/zip", "new-content".getBytes()
        );

        Challenge updated = new Challenge(
                id, "Updated Title", "Updated Desc",
                "crypto", "hard", 500, "http://localhost/download"
        );

        when(challengeService.updateChallenge(
                anyString(), anyString(), anyString(), anyString(),
                anyString(), anyInt(), anyString(), any(byte[].class)
        )).thenReturn(updated);

        // Act & Assert
        mockMvc.perform(multipart("/api/challenges/{id}", id)
                        .file(file)
                        .param("title", "Updated Title")
                        .param("description", "Updated Desc")
                        .param("category", "crypto")
                        .param("difficulty", "hard")
                        .param("points", "500")
                        .param("flag", "flag{updated}")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.category").value("crypto"));

        verify(challengeService, times(1)).updateChallenge(
                eq(id), eq("Updated Title"), eq("Updated Desc"), eq("crypto"),
                eq("hard"), eq(500), eq("flag{updated}"), any(byte[].class)
        );
    }

    @Test
    void updateChallenge_PartialUpdate_WithoutFile() throws Exception {
        // Arrange
        String id = "web-101";
        Challenge updated = new Challenge(
                id, "New Title", "Old Desc", "web", "easy", 100, "http://localhost/download"
        );

        when(challengeService.updateChallenge(
                anyString(), anyString(), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()
        )).thenReturn(updated);

        // Act & Assert
        mockMvc.perform(multipart("/api/challenges/{id}", id)
                        .param("title", "New Title")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Title"));

        verify(challengeService, times(1)).updateChallenge(
                eq(id), eq("New Title"), isNull(), isNull(), isNull(), isNull(), isNull(), isNull()
        );
    }

    @Test
    void deleteChallenge_ExistingId_ReturnsNoContent() throws Exception {
        // Arrange
        String id = "web-101";
        doNothing().when(challengeService).deleteChallenge(id);

        // Act & Assert
        mockMvc.perform(delete("/api/challenges/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(challengeService, times(1)).deleteChallenge(id);
    }

    @Test
    void deleteChallenge_NonExistingId_ReturnsNotFound() throws Exception {
        // Arrange
        String id = "invalid";
        doThrow(new RuntimeException("Challenge not found"))
                .when(challengeService).deleteChallenge(id);

        // Act & Assert
        mockMvc.perform(delete("/api/challenges/{id}", id)
                        .with(csrf()))
                .andExpect(status().isNotFound());

        verify(challengeService, times(1)).deleteChallenge(id);
    }

    @Test
    void getAdminStats_ReturnsStatistics() throws Exception {
        // Arrange
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalChallenges", 10L);
        stats.put("activeChallenges", 10L);
        stats.put("totalUsers", "N/A");
        stats.put("totalSubmissions", "N/A");
        stats.put("challengesByCategory", Arrays.asList(
                Map.of("category", "web", "count", 5L),
                Map.of("category", "crypto", "count", 5L)
        ));
        stats.put("challengesByDifficulty", Arrays.asList(
                Map.of("difficulty", "easy", "count", 4L),
                Map.of("difficulty", "medium", "count", 3L),
                Map.of("difficulty", "hard", "count", 3L)
        ));

        when(challengeService.getAdminStats()).thenReturn(stats);

        // Act & Assert
        mockMvc.perform(get("/api/challenges/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalChallenges").value(10))
                .andExpect(jsonPath("$.activeChallenges").value(10))
                .andExpect(jsonPath("$.challengesByCategory.length()").value(2))
                .andExpect(jsonPath("$.challengesByDifficulty.length()").value(3));

        verify(challengeService, times(1)).getAdminStats();
    }

    @Test
    void createChallenge_ServiceThrowsException_ReturnsError() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.zip", "application/zip", "content".getBytes()
        );

        when(challengeService.createChallenge(
                anyString(), anyString(), anyString(), anyString(), anyInt(), anyString(), any(byte[].class)
        )).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        mockMvc.perform(multipart("/api/challenges")
                        .file(file)
                        .param("title", "Test")
                        .param("description", "Desc")
                        .param("category", "web")
                        .param("difficulty", "easy")
                        .param("points", "100")
                        .param("flag", "flag{test}")
                        .with(csrf()))
                .andExpect(status().isUnauthorized()); // GlobalExceptionHandler converts RuntimeException

        verify(challengeService, times(1)).createChallenge(
                anyString(), anyString(), anyString(), anyString(), anyInt(), anyString(), any(byte[].class)
        );
    }
}