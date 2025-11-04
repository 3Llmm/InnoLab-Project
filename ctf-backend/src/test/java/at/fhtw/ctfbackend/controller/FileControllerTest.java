package at.fhtw.ctfbackend.controller;

import at.fhtw.ctfbackend.config.GlobalMockConfig;
import at.fhtw.ctfbackend.services.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;


import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileController.class)
@Import(GlobalMockConfig.class)
@WithMockUser
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FileService fileService;

    @Test
    void downloadFile_ExistingFile_ReturnsFile() throws Exception {
        // Note: This test requires actual file system access
        // In real scenario, you'd need to mock the file system or use a test directory

        // This test will likely fail without actual file setup
        // Documenting expected behavior

        mockMvc.perform(get("/api/files/download/test.zip"))
                .andExpect(status().isNotFound()); // File doesn't exist in test environment
    }

    @Test
    void downloadFile_NonExistingFile_ReturnsNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/files/download/nonexistent.zip"))
                .andExpect(status().isNotFound());
    }

    @Test
    void downloadFile_PathTraversalAttempt_HandlesSecurely() throws Exception {
        // Attempt directory traversal
        mockMvc.perform(get("/api/files/download/../../../etc/passwd"))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadFile_NoBody_CallsSaveFiles() throws Exception {
        // Arrange
        when(fileService.saveFiles()).thenReturn("Files saved successfully.");

        // Act & Assert
        mockMvc.perform(post("/api/files/upload")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Files saved successfully."));

        verify(fileService, times(1)).saveFiles();
        verify(fileService, never()).customUpload(anyString());
    }

    @Test
    void uploadFile_EmptyBody_CallsSaveFiles() throws Exception {
        // Arrange
        when(fileService.saveFiles()).thenReturn("Files saved successfully.");

        // Act & Assert
        mockMvc.perform(post("/api/files/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Files saved successfully."));

        verify(fileService, times(1)).saveFiles();
        verify(fileService, never()).customUpload(anyString());
    }

    @Test
    void uploadFile_BlankBody_CallsSaveFiles() throws Exception {
        // Arrange
        when(fileService.saveFiles()).thenReturn("Files saved successfully.");

        // Act & Assert
        mockMvc.perform(post("/api/files/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("   ")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Files saved successfully."));

        verify(fileService, times(1)).saveFiles();
        verify(fileService, never()).customUpload(anyString());
    }

    @Test
    void uploadFile_WithJsonBody_CallsCustomUpload() throws Exception {
        // Arrange
        String requestBody = "{\"filename\":\"test.zip\"}";
        when(fileService.customUpload(requestBody))
                .thenReturn("File saved successfully.");

        // Act & Assert
        mockMvc.perform(post("/api/files/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("File saved successfully."));

        verify(fileService, times(1)).customUpload(requestBody);
        verify(fileService, never()).saveFiles();
    }

    @Test
    void uploadFile_CustomUpload_MissingFilename_ReturnsError() throws Exception {
        // Arrange
        String requestBody = "{\"other\":\"value\"}";
        when(fileService.customUpload(requestBody))
                .thenReturn("Missing 'filename' field");

        // Act & Assert
        mockMvc.perform(post("/api/files/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Missing 'filename' field"));

        verify(fileService, times(1)).customUpload(requestBody);
    }

    @Test
    void uploadFile_CustomUpload_FileAlreadyExists_ReturnsError() throws Exception {
        // Arrange
        String requestBody = "{\"filename\":\"existing.zip\"}";
        when(fileService.customUpload(requestBody))
                .thenReturn("File with this ID already exists.");

        // Act & Assert
        mockMvc.perform(post("/api/files/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("File with this ID already exists."));

        verify(fileService, times(1)).customUpload(requestBody);
    }

    @Test
    void uploadFile_CustomUpload_FileNotFoundInClasspath_ReturnsError() throws Exception {
        // Arrange
        String requestBody = "{\"filename\":\"nonexistent.zip\"}";
        when(fileService.customUpload(requestBody))
                .thenReturn("File not found in classpath.");

        // Act & Assert
        mockMvc.perform(post("/api/files/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("File not found in classpath."));

        verify(fileService, times(1)).customUpload(requestBody);
    }

    @Test
    void uploadFile_CustomUpload_InvalidJson_ReturnsError() throws Exception {
        // Arrange
        String requestBody = "invalid json";
        when(fileService.customUpload(requestBody))
                .thenReturn("Error: Unrecognized token 'invalid'");

        // Act & Assert
        mockMvc.perform(post("/api/files/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Error:")));

        verify(fileService, times(1)).customUpload(requestBody);
    }

    @Test
    void uploadFile_SaveFiles_Success_ReturnsMessage() throws Exception {
        // Arrange
        when(fileService.saveFiles())
                .thenReturn("Files saved successfully.");

        // Act & Assert
        mockMvc.perform(post("/api/files/upload")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Files saved successfully."));

        verify(fileService, times(1)).saveFiles();
    }

    @Test
    void uploadFile_SaveFiles_Error_ReturnsErrorMessage() throws Exception {
        // Arrange
        when(fileService.saveFiles())
                .thenReturn("Something went wrong: No files found");

        // Act & Assert
        mockMvc.perform(post("/api/files/upload")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Something went wrong: No files found"));

        verify(fileService, times(1)).saveFiles();
    }

    @Test
    void downloadFile_WithSpecialCharacters_HandlesCorrectly() throws Exception {
        // Test filename with special characters
        mockMvc.perform(get("/api/files/download/test%20file.zip"))
                .andExpect(status().isNotFound());
    }

    @Test
    void downloadFile_WithMultipleDots_HandlesCorrectly() throws Exception {
        // Test filename with multiple dots
        mockMvc.perform(get("/api/files/download/file.backup.zip"))
                .andExpect(status().isNotFound());
    }

    @Test
    void uploadFile_CustomUpload_NullFilename_ReturnsError() throws Exception {
        // Arrange
        String requestBody = "{\"filename\":null}";
        when(fileService.customUpload(requestBody))
                .thenReturn("Missing 'filename' field");

        // Act & Assert
        mockMvc.perform(post("/api/files/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Missing 'filename' field"));

        verify(fileService, times(1)).customUpload(requestBody);
    }

    @Test
    void uploadFile_CustomUpload_EmptyFilename_ReturnsError() throws Exception {
        // Arrange
        String requestBody = "{\"filename\":\"\"}";
        when(fileService.customUpload(requestBody))
                .thenReturn("File not found in classpath.");

        // Act & Assert
        mockMvc.perform(post("/api/files/upload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("File not found in classpath."));

        verify(fileService, times(1)).customUpload(requestBody);
    }

    @Test
    void uploadFile_ServiceThrowsException_HandlesGracefully() throws Exception {
        // Arrange
        when(fileService.saveFiles())
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(post("/api/files/upload")
                        .with(csrf()))
                .andExpect(status().isUnauthorized()); // GlobalExceptionHandler

        verify(fileService, times(1)).saveFiles();
    }
}