package at.fhtw.ctfbackend.config;

import at.fhtw.ctfbackend.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.userdetails.DefaultLdapAuthoritiesPopulator;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager(LdapContextSource contextSource) {
        FilterBasedLdapUserSearch userSearch =
                new FilterBasedLdapUserSearch("ou=users", "(uid={0})", contextSource);

        BindAuthenticator bindAuthenticator = new BindAuthenticator(contextSource);
        bindAuthenticator.setUserSearch(userSearch);

        LdapAuthenticationProvider provider = new LdapAuthenticationProvider(bindAuthenticator,
                new DefaultLdapAuthoritiesPopulator(contextSource, null));

        return new ProviderManager(List.of(provider));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authManager) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
        // Public (no token needed)
        .requestMatchers("/api/login").permitAll()
        .requestMatchers("/api/auth/**").permitAll()
        .requestMatchers("/ws/**").permitAll()
        .requestMatchers("/api/categories").permitAll()  // Categories are public (theory content)
        .requestMatchers("/api/solves/challenge/*/stats").permitAll()  // Challenge stats are public
        .requestMatchers("/api/solves/challenge/*/count").permitAll()  // Challenge solve counts are public
        .requestMatchers("/api/solves/recent").permitAll()  // Recent solves are public
        .requestMatchers("/api/solves/top-solvers").permitAll()  // Top solvers are public
        .requestMatchers("/api/solves/most-solved").permitAll()  // Most solved challenges are public
        .requestMatchers("/api/solves/total-count").permitAll()  // Total solve count is public

        // Protected (token required)
        .requestMatchers(HttpMethod.POST, "/api/challenges/**").authenticated()
        .requestMatchers(HttpMethod.PUT, "/api/challenges/**").authenticated()
        .requestMatchers(HttpMethod.DELETE, "/api/challenges/**").authenticated()
        .requestMatchers("/api/challenges/**").permitAll()
        .requestMatchers("/api/environment/**").authenticated()
        .requestMatchers("/api/flags/**").authenticated()
        .requestMatchers("/api/user/me").authenticated()
        .requestMatchers("/api/files/**").authenticated()
        .requestMatchers("/api/solves/me").authenticated()  // User's own solves require auth
        .requestMatchers("/api/solves/check/**").authenticated()  // Checking if user solved requires auth
        .requestMatchers("/api/solves/me/**").authenticated()  // User's own stats require auth

        .anyRequest().authenticated()
)


                .authenticationManager(authManager)
                .formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "http://localhost:3002"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Cookie"));
        configuration.setExposedHeaders(List.of("Set-Cookie"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}