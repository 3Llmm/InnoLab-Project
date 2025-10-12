package at.fhtw.ctfbackend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
    }

    @Test
    void shouldGenerateAndValidateToken() {
        String username = "joe.doe";
        String token = jwtUtil.generateToken(username);

        assertNotNull(token);
        assertTrue(jwtUtil.validateToken(token));
        assertEquals(username, jwtUtil.extractUsername(token));
    }

    @Test
    void shouldFailOnInvalidToken() {
        String fakeToken = "invalid.token.string";
        assertFalse(jwtUtil.validateToken(fakeToken));
    }
}
