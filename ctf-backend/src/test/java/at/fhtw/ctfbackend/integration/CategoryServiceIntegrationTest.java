package at.fhtw.ctfbackend.integration;

import at.fhtw.ctfbackend.entity.CategoryEntity;
import at.fhtw.ctfbackend.models.Category;
import at.fhtw.ctfbackend.repository.CategoryRepository;
import at.fhtw.ctfbackend.services.CategoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for {@link CategoryService}.
 *
 * Uses Testcontainers setup from {@link BaseIntegrationTest}.
 * Validates JSON parsing, database persistence, mapping, and Confluence integration.
 */
@AutoConfigureMockMvc
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class CategoryServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private CategoryRepository categoryRepository;

    // ────────────────────────────────
    // Category Listing
    // ────────────────────────────────
    @Nested
    @DisplayName("Category Listing")
    class CategoryListing {

        @Test
        @DisplayName("Returns all seeded categories")
        void listAll_ReturnsAllSeeded() {
            List<Category> categories = categoryService.listAll();

            assertNotNull(categories);
            assertEquals(3, categories.size());
            assertTrue(categories.stream().anyMatch(c -> c.getId().equals("web")));
            assertTrue(categories.stream().anyMatch(c -> c.getId().equals("crypto")));
            assertTrue(categories.stream().anyMatch(c -> c.getId().equals("forensics")));
        }

        @Test
        @DisplayName("Correctly maps category details")
        void listAll_MapsDetailsCorrectly() {
            Category web = categoryService.listAll().stream()
                    .filter(c -> c.getId().equals("web"))
                    .findFirst()
                    .orElseThrow();

            assertEquals("web", web.getId());
            assertEquals("Web Exploitation", web.getName());
            assertEquals("Challenges about HTTP, auth, input validation, and server-side bugs.", web.getSummary());
            assertEquals("https://example.com/web-tools.zip", web.getFileUrl());
        }

        @Test
        @DisplayName("Empty database returns empty list")
        void listAll_EmptyDb_ReturnsEmpty() {
            categoryRepository.deleteAll();
            assertTrue(categoryService.listAll().isEmpty());
        }
    }

    // ────────────────────────────────
    // Create Single Category
    // ────────────────────────────────
    @Nested
    @DisplayName("Create Single Category")
    class CreateSingleCategory {

        @Test
        @DisplayName("Creates valid single category successfully")
        void createCategory_Valid_Success() throws JsonProcessingException {
            String json = """
                {
                  "id": "pwn",
                  "name": "Binary Exploitation",
                  "pageId": "12345"
                }
                """;

            String result = categoryService.createCategory(json);
            assertEquals("Category created successfully.", result);

            CategoryEntity saved = categoryRepository.findById("pwn").orElseThrow();
            assertEquals("Binary Exploitation", saved.getName());
            assertEquals("<p>Mock summary for page 12345</p>", saved.getSummary());
            assertTrue(saved.getFileUrl().contains("12345"));
        }

        @Test
        @DisplayName("Missing ID throws exception")
        void missingId_ThrowsException() {
            String json = """
                { "name": "Binary Exploitation", "pageId": "12345" }
                """;
            assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(json));
        }

        @Test
        @DisplayName("Missing name throws exception")
        void missingName_ThrowsException() {
            String json = """
                { "id": "pwn", "pageId": "12345" }
                """;
            assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(json));
        }

        @Test
        @DisplayName("Missing pageId throws exception")
        void missingPageId_ThrowsException() {
            String json = """
                { "id": "pwn", "name": "Binary Exploitation" }
                """;
            assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(json));
        }

        @Test
        @DisplayName("Duplicate category returns proper message")
        void duplicateCategory_ReturnsError() throws JsonProcessingException {
            String json = """
                { "id": "web", "name": "Web Hacking", "pageId": "99999" }
                """;
            assertEquals("Category already exists.", categoryService.createCategory(json));
        }
    }

    // ────────────────────────────────
    // Create Multiple Categories
    // ────────────────────────────────
    @Nested
    @DisplayName("Create Multiple Categories")
    class CreateMultipleCategories {

        @Test
        @DisplayName("Creates multiple valid categories successfully")
        void createMultiple_Valid_Success() throws JsonProcessingException {
            String json = """
                [
                  { "id": "pwn", "name": "Binary Exploitation", "pageId": "11111" },
                  { "id": "misc", "name": "Miscellaneous", "pageId": "22222" }
                ]
                """;

            String result = categoryService.createCategory(json);
            assertEquals("All categories created successfully.", result);
            assertTrue(categoryRepository.existsById("pwn"));
            assertTrue(categoryRepository.existsById("misc"));
        }

        @Test
        @DisplayName("Stops on duplicate within array")
        void createMultiple_WithDuplicate_ReturnsError() throws JsonProcessingException {
            String json = """
                [
                  { "id": "pwn", "name": "Binary Exploitation", "pageId": "11111" },
                  { "id": "web", "name": "Web Hacking", "pageId": "22222" }
                ]
                """;

            assertEquals("Category already exists.", categoryService.createCategory(json));
        }

        @Test
        @DisplayName("Invalid object in array throws exception")
        void createMultiple_Invalid_ThrowsException() {
            String json = """
                [
                  { "id": "pwn", "name": "Binary Exploitation", "pageId": "11111" },
                  { "id": "misc", "pageId": "22222" }
                ]
                """;
            assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory(json));
        }
    }

    // ────────────────────────────────
    // Confluence Integration
    // ────────────────────────────────
    @Nested
    @DisplayName("Confluence Integration")
    class ConfluenceIntegration {

        @Test
        @DisplayName("Fetches mock summary from Confluence")
        void fetchesMockSummary() throws JsonProcessingException {
            String json = """
                { "id": "reversing", "name": "Reverse Engineering", "pageId": "54321" }
                """;
            categoryService.createCategory(json);

            CategoryEntity saved = categoryRepository.findById("reversing").orElseThrow();
            assertEquals("<p>Mock summary for page 54321</p>", saved.getSummary());

        }

        @Test
        @DisplayName("Constructs correct file URL")
        void constructsFileUrlCorrectly() throws JsonProcessingException {
            String json = """
                { "id": "hardware", "name": "Hardware Hacking", "pageId": "67890" }
                """;
            categoryService.createCategory(json);

            CategoryEntity saved = categoryRepository.findById("hardware").orElseThrow();
            assertEquals(
                    "https://technikum-wien-team-kjev9g23.atlassian.net/wiki/spaces/C/pages/67890",
                    saved.getFileUrl()
            );
        }
    }

    // ────────────────────────────────
    // JSON Parsing & Validation
    // ────────────────────────────────
    @Nested
    @DisplayName("JSON Parsing & Validation")
    class JsonParsing {

        @Test
        void invalidJson_ThrowsException() {
            assertThrows(JsonProcessingException.class, () -> categoryService.createCategory("{ invalid json }"));
        }

        @Test
        void nullJson_ThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory("null"));
        }

        @Test
        void emptyObject_ThrowsException() {
            assertThrows(IllegalArgumentException.class, () -> categoryService.createCategory("{}"));
        }

        @Test
        void emptyArray_ReturnsSuccess() throws JsonProcessingException {
            assertEquals("All categories created successfully.", categoryService.createCategory("[]"));
        }

        @Test
        void extraFields_Ignored() throws JsonProcessingException {
            String json = """
                {
                  "id": "osint",
                  "name": "OSINT",
                  "pageId": "11111",
                  "extra": "ignored"
                }
                """;
            assertEquals("Category created successfully.", categoryService.createCategory(json));
            assertTrue(categoryRepository.existsById("osint"));
        }
    }

    // ────────────────────────────────
    // Database Integrity
    // ────────────────────────────────
    @Nested
    @Transactional
    @DisplayName("Database Integrity")
    class DatabaseIntegrity {
        @Test
        @Transactional
        @DisplayName("Category ID must be unique (DB constraint)")
        void uniqueId_ConstraintEnforced() {
            CategoryEntity c1 = new CategoryEntity("unique", "A", "S1", "url1");
            entityManager.persist(c1);
            entityManager.flush();

            entityManager.clear();

            CategoryEntity c2 = new CategoryEntity("unique", "B", "S2", "url2");

            assertThrows(org.hibernate.exception.ConstraintViolationException.class, () -> {
                entityManager.persist(c2);
                entityManager.flush();
            });
        }


        @Test
        void fileUrl_CanBeNull() {
            CategoryEntity category = new CategoryEntity("null-url", "Test", "Summary", null);
            categoryRepository.save(category);

            assertNull(categoryRepository.findById("null-url").orElseThrow().getFileUrl());
        }

        @Test
        void longSummary_StoredCorrectly() {
            String longText = "A".repeat(10_000);
            CategoryEntity category = new CategoryEntity("long-summary", "Test", longText, "url");
            categoryRepository.save(category);

            CategoryEntity retrieved = categoryRepository.findById("long-summary").orElseThrow();
            assertEquals(longText, retrieved.getSummary());
        }
    }

    // ────────────────────────────────
    // Mapping
    // ────────────────────────────────
    @Nested
    @DisplayName("Entity ↔ Model Mapping")
    class Mapping {

        @Test
        void entityToModel_PreservesAllFields() {
            Category crypto = categoryService.listAll().stream()
                    .filter(c -> c.getId().equals("crypto"))
                    .findFirst()
                    .orElseThrow();

            assertEquals("crypto", crypto.getId());
            assertEquals("Cryptography", crypto.getName());
            assertEquals("Classical and modern crypto tasks: XOR, RSA, padding oracles.", crypto.getSummary());
            assertEquals("https://example.com/crypto-handbook.pdf", crypto.getFileUrl());
        }

        @Test
        void modelToEntity_ConsistentRoundtrip() throws JsonProcessingException {
            String json = """
                { "id": "mapping-test", "name": "Mapping Test", "pageId": "99999" }
                """;
            categoryService.createCategory(json);

            CategoryEntity entity = categoryRepository.findById("mapping-test").orElseThrow();
            Category model = categoryService.listAll().stream()
                    .filter(c -> c.getId().equals("mapping-test"))
                    .findFirst()
                    .orElseThrow();

            assertEquals(entity.getId(), model.getId());
            assertEquals(entity.getName(), model.getName());
            assertEquals(entity.getSummary(), model.getSummary());
            assertEquals(entity.getFileUrl(), model.getFileUrl());
        }
    }

    // ────────────────────────────────
    // Edge Cases
    // ────────────────────────────────
    @Nested
    @DisplayName("Edge Cases")
    class EdgeCases {

        @Test
        void specialCharactersInId_Supported() throws JsonProcessingException {
            String json = """
                { "id": "test-category_123", "name": "Test", "pageId": "12345" }
                """;
            assertEquals("Category created successfully.", categoryService.createCategory(json));
            assertTrue(categoryRepository.existsById("test-category_123"));
        }

        @Test
        void unicodeCharacters_Supported() throws JsonProcessingException {
            String json = """
                { "id": "unicode", "name": "Тест 测试 ", "pageId": "12345" }
                """;
            categoryService.createCategory(json);

            CategoryEntity saved = categoryRepository.findById("unicode").orElseThrow();
            assertEquals("Тест 测试 ", saved.getName());
        }

        @Test
        void longName_StoredSuccessfully() throws JsonProcessingException {
            String longName = "A".repeat(500);
            String json = String.format("""
                { "id": "long-name", "name": "%s", "pageId": "12345" }
                """, longName);

            assertEquals("Category created successfully.", categoryService.createCategory(json));
        }
    }
}
