package at.fhtw.ctfbackend.security;

import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void generateToken_ValidUsername_ReturnsToken() {
        // Arrange
        String username = "testuser";

        // Act
        String token = jwtUtil.generateToken(username);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void extractUsername_ValidToken_ReturnsUsername() {
        // Arrange
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        // Act
        String extractedUsername = jwtUtil.extractUsername(token);

        // Assert
        assertEquals(username, extractedUsername);
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        // Arrange
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        // Act
        boolean isValid = jwtUtil.validateToken(token);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void validateToken_InvalidToken_ReturnsFalse() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act
        boolean isValid = jwtUtil.validateToken(invalidToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_MalformedToken_ReturnsFalse() {
        // Arrange
        String malformedToken = "not-a-jwt";

        // Act
        boolean isValid = jwtUtil.validateToken(malformedToken);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void validateToken_EmptyToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtUtil.validateToken("");

        // Assert
        assertFalse(isValid);
    }

    @Test
    void extractUsername_InvalidToken_ThrowsException() {
        // Arrange
        String invalidToken = "invalid.jwt.token";

        // Act & Assert
        assertThrows(JwtException.class, () -> jwtUtil.extractUsername(invalidToken));
    }

    @Test
    void generateToken_EmptyUsername_GeneratesToken() {
        // Arrange
        String username = "";

        // Act
        String token = jwtUtil.generateToken(username);

        // Assert
        assertNotNull(token);
        assertEquals("", jwtUtil.extractUsername(token));
    }

    @Test
    void generateToken_UsernameWithSpecialCharacters_GeneratesToken() {
        // Arrange
        String username = "user@example.com";

        // Act
        String token = jwtUtil.generateToken(username);

        // Assert
        assertNotNull(token);
        assertEquals(username, jwtUtil.extractUsername(token));
    }

    @Test
    void generateToken_LongUsername_GeneratesToken() {
        // Arrange
        String username = "a".repeat(1000);

        // Act
        String token = jwtUtil.generateToken(username);

        // Assert
        assertNotNull(token);
        assertEquals(username, jwtUtil.extractUsername(token));
    }

    @Test
    void generateToken_MultipleTokens_AreDifferent() {
        // Arrange
        String username = "testuser";

        // Act
        String token1 = jwtUtil.generateToken(username);
        try {
            Thread.sleep(10); // Small delay to ensure different timestamps
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        String token2 = jwtUtil.generateToken(username);

        // Assert
        assertNotEquals(token1, token2); // Different issued times
    }

    @Test
    void validateToken_TokenFromDifferentKey_ReturnsFalse() {
        // Create a token from one instance
        String token = jwtUtil.generateToken("testuser");

        // Create a new instance (different key)
        JwtUtil differentJwtUtil = new JwtUtil();

        // Act
        boolean isValid = differentJwtUtil.validateToken(token);

        // Assert
        assertFalse(isValid); // Should fail because keys are different
    }

    @Test
    void extractUsername_TamperedToken_ThrowsException() {
        // Arrange
        String username = "testuser";
        String token = jwtUtil.generateToken(username);

        // Tamper with the token
        String tamperedToken = token.substring(0, token.length() - 5) + "XXXXX";

        // Act & Assert
        assertThrows(JwtException.class, () -> jwtUtil.extractUsername(tamperedToken));
    }

    @Test
    void generateToken_NullUsername_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> jwtUtil.generateToken(null));
    }

    @Test
    void validateToken_NullToken_ReturnsFalse() {
        // Act
        boolean isValid = jwtUtil.validateToken(null);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void generateToken_UsernameWithSpaces_GeneratesToken() {
        // Arrange
        String username = "user with spaces";

        // Act
        String token = jwtUtil.generateToken(username);

        // Assert
        assertNotNull(token);
        assertEquals(username, jwtUtil.extractUsername(token));
    }

    @Test
    void generateToken_UsernameWithUnicode_GeneratesToken() {
        // Arrange
        String username = "用户名"; // Chinese characters

        // Act
        String token = jwtUtil.generateToken(username);

        // Assert
        assertNotNull(token);
        assertEquals(username, jwtUtil.extractUsername(token));
    }

    @Test
    void tokenLifecycle_GenerateValidateExtract_WorksCorrectly() {
        // Arrange
        String username = "lifecycletest";

        // Act
        String token = jwtUtil.generateToken(username);
        boolean isValid = jwtUtil.validateToken(token);
        String extractedUsername = jwtUtil.extractUsername(token);

        // Assert
        assertTrue(isValid);
        assertEquals(username, extractedUsername);
    }

    @Test
    void validateToken_TokenWithExtraWhitespace_ReturnsFalse() {
        // Arrange
        String username = "testuser";
        String token = jwtUtil.generateToken(username);
        String tokenWithWhitespace = "  " + token + "  ";

        // Act
        boolean isValid = jwtUtil.validateToken(tokenWithWhitespace);

        // Assert
        assertFalse(isValid);
    }
}