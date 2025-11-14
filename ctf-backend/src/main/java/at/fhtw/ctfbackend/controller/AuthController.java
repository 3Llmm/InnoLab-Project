package at.fhtw.ctfbackend.controller;

import at.fhtw.ctfbackend.models.LdapCredentials;
import at.fhtw.ctfbackend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/api/login")
    public Map<String, String> login(@RequestBody LdapCredentials credentials, HttpServletResponse response) {
        Map<String, String> responseBody = new HashMap<>();

        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword());

            Authentication authentication = authenticationManager.authenticate(authToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String jwtToken = jwtUtil.generateToken(authentication.getName());

            // Set HTTP-only cookie instead of returning token in response body
            Cookie authCookie = new Cookie("auth_token", jwtToken);
            authCookie.setHttpOnly(true);
            authCookie.setSecure(false); // Set to true in production with HTTPS
            authCookie.setPath("/");
            authCookie.setMaxAge(24 * 60 * 60); // 24 hours
            authCookie.setAttribute("SameSite", "Lax");

            response.addCookie(authCookie);

            responseBody.put("status", "success");
            responseBody.put("message", "Welcome, " + authentication.getName() + "!");
            responseBody.put("username", authentication.getName());

            return responseBody;
        } catch (AuthenticationException e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    @PostMapping("/api/logout")
    public Map<String, String> logout(HttpServletResponse response) {
        // Clear the auth cookie
        Cookie authCookie = new Cookie("auth_token", "");
        authCookie.setHttpOnly(true);
        authCookie.setSecure(false);
        authCookie.setPath("/");
        authCookie.setMaxAge(0); // Expire immediately

        response.addCookie(authCookie);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("status", "success");
        responseBody.put("message", "Logged out successfully");

        return responseBody;
    }

    @GetMapping("/api/user/me")
    public Map<String, String> getCurrentUser(Authentication authentication) {
        Map<String, String> response = new HashMap<>();
        if (authentication != null && authentication.isAuthenticated()) {
            response.put("username", authentication.getName());
            response.put("status", "success");
        } else {
            response.put("status", "error");
            response.put("message", "Not authenticated");
        }
        return response;
    }
}