package at.fhtw.ctfbackend.integration;

import at.fhtw.ctfbackend.CtfbackendApplication;
import at.fhtw.ctfbackend.config.MockConfluenceConfig;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;

/**
 * Base class for all integration tests.
 *
 * Provides:
 * - Real PostgreSQL and OpenLDAP containers via Testcontainers
 * - Full Spring Boot context with test profile
 * - Mocked external dependencies (Confluence)
 * - Dynamic property injection for containerized services
 */
@SpringBootTest(
        classes = {CtfbackendApplication.class, MockConfluenceConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
@ActiveProfiles("test")
public abstract class BaseIntegrationTest {

    /** PostgreSQL container (reused across tests) */
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16"))
            .withDatabaseName("ctf")
            .withUsername("ctfuser")
            .withPassword("ctfpass")
            .withReuse(false)
            .waitingFor(Wait.forListeningPort())
            .withStartupTimeout(Duration.ofSeconds(60));

    /** OpenLDAP container for authentication-related tests */
    @Container
    static final GenericContainer<?> LDAP = new GenericContainer<>(DockerImageName.parse("osixia/openldap:1.5.0"))
            .withExposedPorts(389)
            .withEnv("LDAP_ORGANISATION", "CTF")
            .withEnv("LDAP_DOMAIN", "ctf.local")
            .withEnv("LDAP_ADMIN_PASSWORD", "admin")
            .withEnv("LDAP_CONFIG_PASSWORD", "admin")
            .withEnv("LDAP_REMOVE_CONFIG_AFTER_SETUP", "false")
            .withReuse(true)
            .waitingFor(Wait.forLogMessage(".*slapd starting.*", 1))
            .withStartupTimeout(Duration.ofSeconds(60));

    /**
     * Injects dynamic container properties into the Spring context.
     * Executed after container startup but before the application context loads.
     */
    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        // Database properties
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.show-sql", () -> "true");
        registry.add("spring.sql.init.mode", () -> "never");

        // LDAP properties
        registry.add("spring.ldap.urls",
                () -> String.format("ldap://%s:%d", LDAP.getHost(), LDAP.getMappedPort(389)));
        registry.add("spring.ldap.base", () -> "dc=ctf,dc=local");
        registry.add("spring.ldap.username", () -> "cn=admin,dc=ctf,dc=local");
        registry.add("spring.ldap.password", () -> "admin");

        // Mock external API credentials
        registry.add("CONFLUENCE_EMAIL", () -> "test@example.com");
        registry.add("CONFLUENCE_API_TOKEN", () -> "dummy-token-for-testing");
    }
}
