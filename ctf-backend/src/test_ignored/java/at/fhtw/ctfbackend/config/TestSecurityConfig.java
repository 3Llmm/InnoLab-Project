package at.fhtw.ctfbackend.config;


import at.fhtw.ctfbackend.security.JwtAuthenticationFilter;
import at.fhtw.ctfbackend.security.JwtUtil;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Test-specific security configuration.
 * This replaces LDAP authentication with in-memory authentication for testing.
 */
@TestConfiguration
@EnableWebSecurity
public class TestSecurityConfig {

    @Bean
    @Primary
    public UserDetailsService testUserDetailsService() {
        // Create test users for integration tests
        return new InMemoryUserDetailsManager(
                User.withUsername("testuser")
                        .password("{noop}password123") // {noop} means no password encoding
                        .roles("USER")
                        .build(),
                User.withUsername("admin")
                        .password("{noop}admin123")
                        .roles("ADMIN", "USER")
                        .build()
        );
    }

    @Bean
    @Primary
    public JwtUtil testJwtUtil() {
        return new JwtUtil();
    }

    @Bean
    @Primary
    public JwtAuthenticationFilter testJwtAuthenticationFilter(JwtUtil jwtUtil) {
        return new JwtAuthenticationFilter();
    }

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/login").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}