package at.fhtw.ctfbackend.security;

import at.fhtw.ctfbackend.entity.UserEntity;
import at.fhtw.ctfbackend.services.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminAuthorizationTest {

    private static final String TEST_SECRET = "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa";

    private JwtUtil jwtUtil;
    private JwtAuthenticationFilter filter;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Captor
    private ArgumentCaptor<UsernamePasswordAuthenticationToken> authCaptor;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(TEST_SECRET);
        filter = new JwtAuthenticationFilter(jwtUtil, userService);
        SecurityContextHolder.clearContext();
        lenient().when(request.getMethod()).thenReturn("GET");
    }

    private void setupCookie(String token) {
        Cookie cookie = new Cookie("auth_token", token);
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
    }

    @Test
    void adminUserGrantsAdminRoleFromDatabase() throws Exception {
        String token = jwtUtil.generateToken("testuser", false);
        setupCookie(token);

        UserEntity user = UserEntity.builder()
                .username("testuser")
                .isAdmin(true)
                .isActive(true)
                .build();
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void nonAdminUserDoesNotGetAdminRoleEvenWithAdminJwtClaim() throws Exception {
        String token = jwtUtil.generateToken("testuser", true);
        setupCookie(token);

        UserEntity user = UserEntity.builder()
                .username("testuser")
                .isAdmin(false)
                .isActive(true)
                .build();
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(auth);
        assertTrue(auth.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test
    void inactiveUserIsNotAuthenticated() throws Exception {
        String token = jwtUtil.generateToken("testuser", false);
        setupCookie(token);

        UserEntity user = UserEntity.builder()
                .username("testuser")
                .isAdmin(false)
                .isActive(false)
                .build();
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
    }

    @Test
    void nonExistentUserIsNotAuthenticated() throws Exception {
        String token = jwtUtil.generateToken("nonexistent", false);
        setupCookie(token);

        when(userService.findByUsername("nonexistent")).thenReturn(Optional.empty());

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
    }

    @Test
    void noCookieMeansNoAuthentication() throws Exception {
        when(request.getCookies()).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
    }

    @Test
    void invalidTokenDoesNotAuthenticate() throws Exception {
        setupCookie("invalid-jwt-token");

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
    }

    @Test
    void filterChainIsAlwaysCalled() throws Exception {
        String token = jwtUtil.generateToken("testuser", false);
        setupCookie(token);

        UserEntity user = UserEntity.builder()
                .username("testuser")
                .isAdmin(false)
                .isActive(true)
                .build();
        when(userService.findByUsername("testuser")).thenReturn(Optional.of(user));

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    void optionsRequestIsSkipped() throws Exception {
        when(request.getMethod()).thenReturn("OPTIONS");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain, times(1)).doFilter(request, response);
        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertNull(auth);
    }
}
