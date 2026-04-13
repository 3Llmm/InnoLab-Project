package at.fhtw.ctfbackend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitConfig {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Value("${rate-limit.global.requests:100}")
    private int globalRequests;

    @Value("${rate-limit.global.duration-seconds:60}")
    private int globalDuration;

    @Value("${rate-limit.login.requests:10}")
    private int loginRequests;

    @Value("${rate-limit.login.duration-seconds:60}")
    private int loginDuration;

    @Value("${rate-limit.flag.requests:30}")
    private int flagRequests;

    @Value("${rate-limit.flag.duration-seconds:60}")
    private int flagDuration;

    @Value("${rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    private String getBucketType(String path) {
        if (path.contains("/login")) {
            return "login";
        } else if (path.contains("/flags") || path.contains("/solves/check")) {
            return "flag";
        } else {
            return "global";
        }
    }

    private Bucket createBucket(int requests, int durationSeconds) {
        Refill refill = Refill.greedy(requests, Duration.ofSeconds(durationSeconds));
        Bandwidth limit = Bandwidth.classic(requests, refill);
        return Bucket.builder().addLimit(limit).build();
    }

    public Bucket resolveBucket(String key, String path) {
        String bucketType = getBucketType(path);
        String bucketKey = key + ":" + bucketType;
        
        return buckets.computeIfAbsent(bucketKey, k -> {
            if ("login".equals(bucketType)) {
                return createBucket(loginRequests, loginDuration);
            } else if ("flag".equals(bucketType)) {
                return createBucket(flagRequests, flagDuration);
            } else {
                return createBucket(globalRequests, globalDuration);
            }
        });
    }

    public boolean isEnabled() {
        return rateLimitEnabled;
    }

    public int getAvailableTokens(Bucket bucket) {
        return (int) bucket.getAvailableTokens();
    }
}
