package at.fhtw.ctfbackend.controller;

import at.fhtw.ctfbackend.models.LdapCredentials;
import at.fhtw.ctfbackend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/api/login")
    public Map<String, String> login(@RequestBody LdapCredentials credentials) {
        Map<String, String> response = new HashMap<>();

        try {
            //  class provided by Spring Security that implements the Authentication interface. It is used to store the username and password for authentication.
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(credentials.getUsername(), credentials.getPassword());

            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(authToken);

            // Store authentication in security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // Generate JWT token
            String jwtToken = jwtUtil.generateToken(authentication.getName());

            // Return welcome message
            response.put("token", jwtToken);
            response.put("status", "success");
            response.put("message", "Welcome, " + authentication.getName() + "!");

            return response;
        } catch (AuthenticationException e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }
}