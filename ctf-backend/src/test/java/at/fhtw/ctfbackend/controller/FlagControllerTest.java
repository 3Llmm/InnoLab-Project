package at.fhtw.ctfbackend.controller;

import at.fhtw.ctfbackend.config.GlobalMockConfig;
import at.fhtw.ctfbackend.services.FlagService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FlagController.class)
@Import(GlobalMockConfig.class)
@WithMockUser(username = "testuser")

class FlagControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FlagService flagService;

    @Test
    void submitFlag_CorrectFlag_FirstSubmission_ReturnsSuccess() throws Exception {
        // Arrange
        String challengeId = "web-101";
        String flag = "flag{correct}";

        when(flagService.validateFlag(challengeId, flag)).thenReturn(true);
        when(flagService.recordSolve("testuser", challengeId)).thenReturn(true);

        String requestBody = "{\"challengeId\":\"" + challengeId + "\",\"flag\":\"" + flag + "\"}";

        // Act & Assert
        mockMvc.perform(post("/api/flags/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Correct flag!"))
                .andExpect(jsonPath("$.status").value("success"));

        verify(flagService, times(1)).validateFlag(challengeId, flag);
        verify(flagService, times(1)).recordSolve("testuser", challengeId);
    }

    @Test
    void submitFlag_IncorrectFlag_ReturnsBadRequest() throws Exception {
        // Arrange
        String challengeId = "web-101";
        String flag = "flag{wrong}";

        when(flagService.validateFlag(challengeId, flag)).thenReturn(false);

        String requestBody = "{\"challengeId\":\"" + challengeId + "\",\"flag\":\"" + flag + "\"}";

        // Act & Assert
        mockMvc.perform(post("/api/flags/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Incorrect flag."))
                .andExpect(jsonPath("$.status").value("error"));

        verify(flagService, times(1)).validateFlag(challengeId, flag);
        verify(flagService, never()).recordSolve(anyString(), anyString());
    }

    @Test
    void submitFlag_AlreadySolved_ReturnsWarning() throws Exception {
        // Arrange
        String challengeId = "web-101";
        String flag = "flag{correct}";

        when(flagService.validateFlag(challengeId, flag)).thenReturn(true);
        when(flagService.recordSolve("testuser", challengeId)).thenReturn(false);

        String requestBody = "{\"challengeId\":\"" + challengeId + "\",\"flag\":\"" + flag + "\"}";

        // Act & Assert
        mockMvc.perform(post("/api/flags/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Flag already submitted."))
                .andExpect(jsonPath("$.status").value("warning"));

        verify(flagService, times(1)).validateFlag(challengeId, flag);
        verify(flagService, times(1)).recordSolve("testuser", challengeId);
    }

    @Test
    void submitFlag_NullChallengeId_HandlesCorrectly() throws Exception {
        // Arrange
        String requestBody = "{\"challengeId\":null,\"flag\":\"flag{test}\"}";

        when(flagService.validateFlag(null, "flag{test}")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/flags/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Incorrect flag."));

        verify(flagService, times(1)).validateFlag(null, "flag{test}");
        verify(flagService, never()).recordSolve(anyString(), anyString());
    }

    @Test
    void submitFlag_NullFlag_HandlesCorrectly() throws Exception {
        // Arrange
        String requestBody = "{\"challengeId\":\"web-101\",\"flag\":null}";

        when(flagService.validateFlag("web-101", null)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/flags/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Incorrect flag."));

        verify(flagService, times(1)).validateFlag("web-101", null);
        verify(flagService, never()).recordSolve(anyString(), anyString());
    }

    @Test
    void submitFlag_EmptyRequestBody_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/flags/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(flagService, times(1)).validateFlag(null, null);
    }

    @Test
    void submitFlag_InvalidJson_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/flags/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"invalid\" ")
                        .with(csrf()))
                .andExpect(status().isBadRequest());

        verify(flagService, never()).validateFlag(anyString(), anyString());
    }

    @Test
    void submitFlag_MissingChallengeId_HandlesCorrectly() throws Exception {
        // Arrange
        String requestBody = "{\"flag\":\"flag{test}\"}";

        when(flagService.validateFlag(null, "flag{test}")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/flags/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Incorrect flag."));
    }

    @Test
    void submitFlag_MissingFlag_HandlesCorrectly() throws Exception {
        // Arrange
        String requestBody = "{\"challengeId\":\"web-101\"}";

        when(flagService.validateFlag("web-101", null)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/flags/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Incorrect flag."));
    }

    @Test
    void submitFlag_EmptyStrings_HandlesCorrectly() throws Exception {
        // Arrange
        String requestBody = "{\"challengeId\":\"\",\"flag\":\"\"}";

        when(flagService.validateFlag("", "")).thenReturn(false);

        // Act & Assert
        mockMvc.perform(post("/api/flags/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Incorrect flag."));

        verify(flagService, times(1)).validateFlag("", "");
    }

    @Test
    @WithMockUser(username = "user.with.dots")
    void submitFlag_UsernameWithSpecialChars_HandlesCorrectly() throws Exception {
        // Arrange
        String challengeId = "web-101";
        String flag = "flag{correct}";

        when(flagService.validateFlag(challengeId, flag)).thenReturn(true);
        when(flagService.recordSolve("user.with.dots", challengeId)).thenReturn(true);

        String requestBody = "{\"challengeId\":\"" + challengeId + "\",\"flag\":\"" + flag + "\"}";

        // Act & Assert
        mockMvc.perform(post("/api/flags/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Correct flag!"));

        verify(flagService, times(1)).recordSolve("user.with.dots", challengeId);
    }

    @Test
    void submitFlag_WhitespaceInFlag_TreatedAsInvalid() throws Exception {
        // Arrange
        String challengeId = "web-101";
        String flag = " flag{correct} "; // with whitespace

        when(flagService.validateFlag(challengeId, flag)).thenReturn(false);

        String requestBody = "{\"challengeId\":\"" + challengeId + "\",\"flag\":\"" + flag + "\"}";

        // Act & Assert
        mockMvc.perform(post("/api/flags/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Incorrect flag."));

        verify(flagService, times(1)).validateFlag(challengeId, flag);
    }

    @Test
    void submitFlag_UnknownChallenge_ReturnsError() throws Exception {
        // Arrange
        String challengeId = "unknown-999";
        String flag = "flag{test}";

        when(flagService.validateFlag(challengeId, flag)).thenReturn(false);

        String requestBody = "{\"challengeId\":\"" + challengeId + "\",\"flag\":\"" + flag + "\"}";

        // Act & Assert
        mockMvc.perform(post("/api/flags/submit")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Incorrect flag."));

        verify(flagService, times(1)).validateFlag(challengeId, flag);
        verify(flagService, never()).recordSolve(anyString(), anyString());
    }
}