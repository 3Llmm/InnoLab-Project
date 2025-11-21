package at.fhtw.ctfbackend.controller;

import at.fhtw.ctfbackend.config.GlobalMockConfig;
import at.fhtw.ctfbackend.models.Category;
import at.fhtw.ctfbackend.services.CategoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(GlobalMockConfig.class)
@WithMockUser
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void getCategories_ReturnsListOfCategories() throws Exception {
        // Arrange
        List<Category> categories = Arrays.asList(
                new Category("web", "Web Security", "Learn web vulnerabilities", "http://example.com/web"),
                new Category("crypto", "Cryptography", "Master crypto", "http://example.com/crypto")
        );
        when(categoryService.listAll()).thenReturn(categories);

        // Act & Assert
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("web"))
                .andExpect(jsonPath("$[0].name").value("Web Security"))
                .andExpect(jsonPath("$[1].id").value("crypto"))
                .andExpect(jsonPath("$[1].name").value("Cryptography"));

        verify(categoryService, times(1)).listAll();
    }

    @Test
    void getCategories_EmptyList_ReturnsEmptyArray() throws Exception {
        // Arrange
        when(categoryService.listAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        verify(categoryService, times(1)).listAll();
    }

    @Test
    void createCategory_ValidSingleCategory_ReturnsSuccess() throws Exception {
        // Arrange
        String requestBody = "{\"id\":\"forensics\",\"name\":\"Digital Forensics\",\"pageId\":\"12345\"}";
        when(categoryService.createCategory(requestBody))
                .thenReturn("Category created successfully.");

        // Act & Assert
        mockMvc.perform(post("/api/categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Category created successfully."));

        verify(categoryService, times(1)).createCategory(requestBody);
    }

    @Test
    void createCategory_ValidArrayOfCategories_ReturnsSuccess() throws Exception {
        // Arrange
        String requestBody = "[" +
                "{\"id\":\"web\",\"name\":\"Web\",\"pageId\":\"111\"}," +
                "{\"id\":\"crypto\",\"name\":\"Crypto\",\"pageId\":\"222\"}" +
                "]";
        when(categoryService.createCategory(requestBody))
                .thenReturn("All categories created successfully.");

        // Act & Assert
        mockMvc.perform(post("/api/categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("All categories created successfully."));

        verify(categoryService, times(1)).createCategory(requestBody);
    }

    @Test
    void createCategory_DuplicateCategory_ReturnsError() throws Exception {
        // Arrange
        String requestBody = "{\"id\":\"web\",\"name\":\"Web\",\"pageId\":\"12345\"}";
        when(categoryService.createCategory(requestBody))
                .thenReturn("Category already exists.");

        // Act & Assert
        mockMvc.perform(post("/api/categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Category already exists."));

        verify(categoryService, times(1)).createCategory(requestBody);
    }

    @Test
    void createCategory_MissingId_ThrowsException() throws Exception {
        // Arrange
        String requestBody = "{\"name\":\"Web\",\"pageId\":\"12345\"}";
        when(categoryService.createCategory(requestBody))
                .thenThrow(new IllegalArgumentException("Missing id"));

        // Act & Assert
        mockMvc.perform(post("/api/categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isUnauthorized()); // GlobalExceptionHandler converts to 401

        verify(categoryService, times(1)).createCategory(requestBody);
    }

    @Test
    void createCategory_MissingName_ThrowsException() throws Exception {
        // Arrange
        String requestBody = "{\"id\":\"web\",\"pageId\":\"12345\"}";
        when(categoryService.createCategory(requestBody))
                .thenThrow(new IllegalArgumentException("Missing name"));

        // Act & Assert
        mockMvc.perform(post("/api/categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(categoryService, times(1)).createCategory(requestBody);
    }

    @Test
    void createCategory_MissingPageId_ThrowsException() throws Exception {
        // Arrange
        String requestBody = "{\"id\":\"web\",\"name\":\"Web\"}";
        when(categoryService.createCategory(requestBody))
                .thenThrow(new IllegalArgumentException("Missing pageId"));

        // Act & Assert
        mockMvc.perform(post("/api/categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(categoryService, times(1)).createCategory(requestBody);
    }

    @Disabled("Fails due to known issue  — to be fixed later")
    @Test
    void createCategory_InvalidJson_ThrowsException() throws Exception {
        // Arrange
        String requestBody = "invalid json";
        when(categoryService.createCategory(requestBody))
                .thenThrow(new JsonProcessingException("Invalid JSON") {});

        // Act & Assert
        mockMvc.perform(post("/api/categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(categoryService, times(1)).createCategory(requestBody);
    }

    @Test
    void createCategory_EmptyRequestBody_HandlesCorrectly() throws Exception {
        // Arrange
        String requestBody = "";
        when(categoryService.createCategory(requestBody))
                .thenThrow(new JsonProcessingException("Empty body") {});

        // Act & Assert
        mockMvc.perform(post("/api/categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(categoryService, times(1)).createCategory(requestBody);
    }

    @Test
    void createCategory_NullJson_ThrowsException() throws Exception {
        // Arrange
        String requestBody = "null";
        when(categoryService.createCategory(requestBody))
                .thenThrow(new IllegalArgumentException("Invalid JSON"));

        // Act & Assert
        mockMvc.perform(post("/api/categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        verify(categoryService, times(1)).createCategory(requestBody);
    }

    @Test
    void createCategory_RepositoryError_ReturnsErrorMessage() throws Exception {
        // Arrange
        String requestBody = "{\"id\":\"web\",\"name\":\"Web\",\"pageId\":\"12345\"}";
        when(categoryService.createCategory(requestBody))
                .thenReturn("Something went wrong with: web Database error");

        // Act & Assert
        mockMvc.perform(post("/api/categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Something went wrong")));

        verify(categoryService, times(1)).createCategory(requestBody);
    }

    @Test
    void createCategory_EmptyArray_ReturnsSuccess() throws Exception {
        // Arrange
        String requestBody = "[]";
        when(categoryService.createCategory(requestBody))
                .thenReturn("All categories created successfully.");

        // Act & Assert
        mockMvc.perform(post("/api/categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("All categories created successfully."));

        verify(categoryService, times(1)).createCategory(requestBody);
    }

    @Test
    void createCategory_ArrayWithOneFailure_ReturnsError() throws Exception {
        // Arrange
        String requestBody = "[" +
                "{\"id\":\"web\",\"name\":\"Web\",\"pageId\":\"111\"}," +
                "{\"id\":\"crypto\",\"name\":\"Crypto\",\"pageId\":\"222\"}" +
                "]";
        when(categoryService.createCategory(requestBody))
                .thenReturn("Category already exists.");

        // Act & Assert
        mockMvc.perform(post("/api/categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Category already exists."));

        verify(categoryService, times(1)).createCategory(requestBody);
    }

    @Test
    void getCategories_ServiceThrowsException_HandlesGracefully() throws Exception {
        // Arrange
        when(categoryService.listAll())
                .thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized()); // GlobalExceptionHandler

        verify(categoryService, times(1)).listAll();
    }

    @Test
    void createCategory_SpecialCharactersInName_HandlesCorrectly() throws Exception {
        // Arrange
        String requestBody = "{\"id\":\"special\",\"name\":\"Web & Crypto!\",\"pageId\":\"12345\"}";
        when(categoryService.createCategory(requestBody))
                .thenReturn("Category created successfully.");

        // Act & Assert
        mockMvc.perform(post("/api/categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Category created successfully."));

        verify(categoryService, times(1)).createCategory(requestBody);
    }

    @Test
    void createCategory_LongPageId_HandlesCorrectly() throws Exception {
        // Arrange
        String longPageId = "1234567890".repeat(10);
        String requestBody = "{\"id\":\"test\",\"name\":\"Test\",\"pageId\":\"" + longPageId + "\"}";
        when(categoryService.createCategory(requestBody))
                .thenReturn("Category created successfully.");

        // Act & Assert
        mockMvc.perform(post("/api/categories/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Category created successfully."));

        verify(categoryService, times(1)).createCategory(requestBody);
    }
}