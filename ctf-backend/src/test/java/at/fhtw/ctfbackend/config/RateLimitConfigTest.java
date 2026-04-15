package at.fhtw.ctfbackend.config;

import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitConfigTest {

    private RateLimitConfig rateLimitConfig;

    @BeforeEach
    void setUp() {
        rateLimitConfig = new RateLimitConfig();
        ReflectionTestUtils.setField(rateLimitConfig, "globalRequests", 100);
        ReflectionTestUtils.setField(rateLimitConfig, "globalDuration", 60);
        ReflectionTestUtils.setField(rateLimitConfig, "loginRequests", 10);
        ReflectionTestUtils.setField(rateLimitConfig, "loginDuration", 60);
        ReflectionTestUtils.setField(rateLimitConfig, "flagRequests", 30);
        ReflectionTestUtils.setField(rateLimitConfig, "flagDuration", 60);
        ReflectionTestUtils.setField(rateLimitConfig, "rateLimitEnabled", true);
    }

    @Test
    void resolveBucket_LoginPath_ReturnsLoginBucket() {
        Bucket bucket = rateLimitConfig.resolveBucket("user1", "/api/login");
        assertNotNull(bucket);
        assertEquals(10, rateLimitConfig.getAvailableTokens(bucket));
    }

    @Test
    void resolveBucket_FlagPath_ReturnsFlagBucket() {
        Bucket bucket = rateLimitConfig.resolveBucket("user1", "/api/flags/check");
        assertNotNull(bucket);
        assertEquals(30, rateLimitConfig.getAvailableTokens(bucket));
    }

    @Test
    void resolveBucket_SolvesCheckPath_ReturnsFlagBucket() {
        Bucket bucket = rateLimitConfig.resolveBucket("user1", "/api/solves/check");
        assertNotNull(bucket);
        assertEquals(30, rateLimitConfig.getAvailableTokens(bucket));
    }

    @Test
    void resolveBucket_OtherPath_ReturnsGlobalBucket() {
        Bucket bucket = rateLimitConfig.resolveBucket("user1", "/api/challenges");
        assertNotNull(bucket);
        assertEquals(100, rateLimitConfig.getAvailableTokens(bucket));
    }

    @Test
    void resolveBucket_SameKey_ReturnsSameBucket() {
        Bucket bucket1 = rateLimitConfig.resolveBucket("user1", "/api/challenges");
        Bucket bucket2 = rateLimitConfig.resolveBucket("user1", "/api/challenges");
        assertSame(bucket1, bucket2);
    }

    @Test
    void resolveBucket_DifferentKeys_ReturnsDifferentBuckets() {
        Bucket bucket1 = rateLimitConfig.resolveBucket("user1", "/api/challenges");
        Bucket bucket2 = rateLimitConfig.resolveBucket("user2", "/api/challenges");
        assertNotSame(bucket1, bucket2);
    }

    @Test
    void resolveBucket_DifferentPaths_ReturnsDifferentBuckets() {
        Bucket bucket1 = rateLimitConfig.resolveBucket("user1-login", "/api/login");
        Bucket bucket2 = rateLimitConfig.resolveBucket("user1", "/api/challenges");
        assertNotSame(bucket1, bucket2);
    }

    @Test
    void isEnabled_ReturnsTrueWhenEnabled() {
        assertTrue(rateLimitConfig.isEnabled());
    }

    @Test
    void isEnabled_ReturnsFalseWhenDisabled() {
        ReflectionTestUtils.setField(rateLimitConfig, "rateLimitEnabled", false);
        assertFalse(rateLimitConfig.isEnabled());
    }

    @Test
    void getAvailableTokens_ReturnsCorrectCount() {
        Bucket bucket = rateLimitConfig.resolveBucket("user1", "/api/challenges");
        assertEquals(100, rateLimitConfig.getAvailableTokens(bucket));
    }

    @Test
    void bucket_ConsumesTokensAfterTryConsume() {
        Bucket bucket = rateLimitConfig.resolveBucket("user1", "/api/challenges");
        assertTrue(bucket.tryConsume(1));
        assertEquals(99, rateLimitConfig.getAvailableTokens(bucket));
    }

    @Test
    void bucket_BlocksWhenExhausted() {
        RateLimitConfig limitedConfig = new RateLimitConfig();
        ReflectionTestUtils.setField(limitedConfig, "globalRequests", 1);
        ReflectionTestUtils.setField(limitedConfig, "globalDuration", 60);
        ReflectionTestUtils.setField(limitedConfig, "loginRequests", 10);
        ReflectionTestUtils.setField(limitedConfig, "loginDuration", 60);
        ReflectionTestUtils.setField(limitedConfig, "flagRequests", 30);
        ReflectionTestUtils.setField(limitedConfig, "flagDuration", 60);
        ReflectionTestUtils.setField(limitedConfig, "rateLimitEnabled", true);

        Bucket bucket = limitedConfig.resolveBucket("user1", "/api/challenges");
        assertTrue(bucket.tryConsume(1));
        assertFalse(bucket.tryConsume(1));
    }
}