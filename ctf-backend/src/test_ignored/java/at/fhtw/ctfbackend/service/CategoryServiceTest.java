package at.fhtw.ctfbackend.service;

import at.fhtw.ctfbackend.entity.CategoryEntity;
import at.fhtw.ctfbackend.external.ConfluenceClient;
import at.fhtw.ctfbackend.models.Category;
import at.fhtw.ctfbackend.repository.CategoryRepository;
import at.fhtw.ctfbackend.services.CategoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CategoryService}.
 *
 * <p>This class tests all the behaviors of CategoryService using Mockito to
 * mock external dependencies such as {@link CategoryRepository} and {@link ConfluenceClient}.
 * It verifies correct handling of JSON inputs, repository interactions, and
 * exception scenarios without connecting to real systems.</p>
 */
@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository repo;

    @Mock
    private ConfluenceClient confluenceClient;

    private CategoryService service;

    private CategoryEntity entity1;
    private CategoryEntity entity2;

    /**
     * Initializes a new CategoryService instance before each test
     * and prepares example CategoryEntity objects.
     */
    @BeforeEach
    void setUp() {
        service = new CategoryService(repo, confluenceClient);

        entity1 = new CategoryEntity(
                "web",
                "Web Security",
                "Learn about web vulnerabilities",
                "https://example.com/web"
        );

        entity2 = new CategoryEntity(
                "crypto",
                "Cryptography",
                "Master encryption techniques",
                "https://example.com/crypto"
        );
    }

    /**
     * Verifies that listAll() returns all categories from the repository.
     */
    @Test
    void listAll_ReturnsAllCategories() {
        when(repo.findAll()).thenReturn(Arrays.asList(entity1, entity2));

        List<Category> result = service.listAll();

        assertEquals(2, result.size());
        assertEquals("Web Security", result.get(0).getName());
        assertEquals("Cryptography", result.get(1).getName());
        verify(repo, times(1)).findAll();
    }

    /**
     * Verifies that listAll() returns an empty list when no categories exist.
     */
    @Test
    void listAll_EmptyRepository_ReturnsEmptyList() {
        when(repo.findAll()).thenReturn(Collections.emptyList());

        List<Category> result = service.listAll();

        assertTrue(result.isEmpty());
        verify(repo, times(1)).findAll();
    }

    /**
     * Tests successful creation of a single valid category from JSON input.
     */
    @Test
    void createCategory_ValidSingleCategory_ReturnsSuccess() throws JsonProcessingException {
        String json = "{\"id\": \"forensics\", \"name\": \"Digital Forensics\", \"pageId\": \"12345\"}";
        when(confluenceClient.fetchSummaryFromConfluence("12345"))
                .thenReturn("Forensics summary");
        when(repo.save(any(CategoryEntity.class))).thenReturn(entity1);

        String result = service.createCategory(json);

        assertEquals("Category created successfully.", result);
        verify(confluenceClient, times(1)).fetchSummaryFromConfluence("12345");
        verify(repo, times(1)).save(any(CategoryEntity.class));
    }

    /**
     * Tests creation of multiple categories in one JSON array.
     */
    @Test
    void createCategory_ValidArrayOfCategories_ReturnsSuccess() throws JsonProcessingException {
        String json = "[" +
                "{\"id\": \"web\", \"name\": \"Web\", \"pageId\": \"111\"}," +
                "{\"id\": \"crypto\", \"name\": \"Crypto\", \"pageId\": \"222\"}" +
                "]";
        when(confluenceClient.fetchSummaryFromConfluence(anyString()))
                .thenReturn("Summary");
        when(repo.save(any(CategoryEntity.class))).thenReturn(entity1);

        String result = service.createCategory(json);

        assertEquals("All categories created successfully.", result);
        verify(confluenceClient, times(2)).fetchSummaryFromConfluence(anyString());
        verify(repo, times(2)).save(any(CategoryEntity.class));
    }

    /** Ensures that missing 'id' field causes an IllegalArgumentException. */
    @Test
    void createCategory_MissingId_ThrowsException() {
        String json = "{\"name\": \"Web\", \"pageId\": \"12345\"}";

        assertThrows(IllegalArgumentException.class,
                () -> service.createCategory(json));

        verify(repo, never()).save(any());
    }

    /** Ensures that missing 'name' field causes an IllegalArgumentException. */
    @Test
    void createCategory_MissingName_ThrowsException() {
        String json = "{\"id\": \"web\", \"pageId\": \"12345\"}";

        assertThrows(IllegalArgumentException.class,
                () -> service.createCategory(json));

        verify(repo, never()).save(any());
    }

    /** Ensures that missing 'pageId' field causes an IllegalArgumentException. */
    @Test
    void createCategory_MissingPageId_ThrowsException() {
        String json = "{\"id\": \"web\", \"name\": \"Web\"}";

        assertThrows(IllegalArgumentException.class,
                () -> service.createCategory(json));

        verify(repo, never()).save(any());
    }

    /**
     * Tests scenario when category creation fails due to a duplicate constraint.
     */
    @Test
    void createCategory_DuplicateCategory_ReturnsError() throws JsonProcessingException {
        String json = "{\"id\": \"web\", \"name\": \"Web\", \"pageId\": \"12345\"}";
        when(confluenceClient.fetchSummaryFromConfluence("12345"))
                .thenReturn("Summary");
        when(repo.save(any(CategoryEntity.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate"));

        String result = service.createCategory(json);

        assertEquals("Category already exists.", result);
        verify(repo, times(1)).save(any(CategoryEntity.class));
    }

    /**
     * Verifies that generic repository exceptions are handled gracefully.
     */
    @Test
    void createCategory_RepositoryException_ReturnsError() throws JsonProcessingException {
        String json = "{\"id\": \"web\", \"name\": \"Web\", \"pageId\": \"12345\"}";
        when(confluenceClient.fetchSummaryFromConfluence("12345"))
                .thenReturn("Summary");
        when(repo.save(any(CategoryEntity.class)))
                .thenThrow(new RuntimeException("Database error"));

        String result = service.createCategory(json);

        assertTrue(result.contains("Something went wrong with"));
        assertTrue(result.contains("web"));
        verify(repo, times(1)).save(any(CategoryEntity.class));
    }

    /**
     * Tests array input where one category creation fails mid-processing.
     */
    @Test
    void createCategory_ArrayWithOneFailure_ReturnsError() throws JsonProcessingException {
        String json = "[" +
                "{\"id\": \"web\", \"name\": \"Web\", \"pageId\": \"111\"}," +
                "{\"id\": \"crypto\", \"name\": \"Crypto\", \"pageId\": \"222\"}" +
                "]";
        when(confluenceClient.fetchSummaryFromConfluence(anyString()))
                .thenReturn("Summary");
        when(repo.save(any(CategoryEntity.class)))
                .thenReturn(entity1)
                .thenThrow(new DataIntegrityViolationException("Duplicate"));

        String result = service.createCategory(json);

        assertEquals("Category already exists.", result);
        verify(repo, times(2)).save(any(CategoryEntity.class));
    }

    /** Ensures that invalid JSON input throws a JsonProcessingException. */
    @Test
    void createCategory_InvalidJson_ThrowsException() {
        String invalidJson = "not a json";

        assertThrows(JsonProcessingException.class,
                () -> service.createCategory(invalidJson));

        verify(repo, never()).save(any());
    }

    /** Verifies that an empty JSON array input is handled gracefully. */
    @Test
    void createCategory_EmptyArray_ReturnsSuccess() throws JsonProcessingException {
        String json = "[]";

        String result = service.createCategory(json);

        assertEquals("All categories created successfully.", result);
        verify(repo, never()).save(any());
    }

    /** Ensures that passing 'null' JSON string results in an IllegalArgumentException. */
    @Test
    void createCategory_NullJson_ThrowsException() {
        String json = "null";

        assertThrows(IllegalArgumentException.class,
                () -> service.createCategory(json));

        verify(repo, never()).save(any());
    }

    /** Verifies that the correct pageId is used when fetching from Confluence. */
    @Test
    void createCategory_ConfluenceClientFetchesCorrectPageId() throws JsonProcessingException {
        String json = "{\"id\": \"rev\", \"name\": \"Reverse Eng\", \"pageId\": \"99999\"}";
        when(confluenceClient.fetchSummaryFromConfluence("99999"))
                .thenReturn("Rev summary");
        when(repo.save(any(CategoryEntity.class))).thenReturn(entity1);

        service.createCategory(json);

        verify(confluenceClient, times(1)).fetchSummaryFromConfluence("99999");
    }

    /**
     * Ensures that the generated file URL includes the correct pageId and domain.
     */
    @Test
    void createCategory_ConstructsCorrectFileUrl() throws JsonProcessingException {
        String json = "{\"id\": \"pwn\", \"name\": \"Pwn\", \"pageId\": \"54321\"}";
        when(confluenceClient.fetchSummaryFromConfluence("54321"))
                .thenReturn("Pwn summary");

        CategoryEntity[] capturedEntity = new CategoryEntity[1];
        when(repo.save(any(CategoryEntity.class))).thenAnswer(invocation -> {
            capturedEntity[0] = invocation.getArgument(0);
            return capturedEntity[0];
        });

        service.createCategory(json);

        assertNotNull(capturedEntity[0]);
        assertTrue(capturedEntity[0].getFileUrl().contains("54321"));
        assertTrue(capturedEntity[0].getFileUrl().contains("atlassian.net"));
    }
}
