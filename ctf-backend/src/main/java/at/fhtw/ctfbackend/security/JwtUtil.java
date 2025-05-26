package at.fhtw.ctfbackend.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    // Secret key (you can load this from config or environment later)
    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // Token validity: 24 hours
    private final long expirationMillis = 1000 * 60 * 60 * 24;

    // Generate JWT
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMillis))
                .signWith(key)
                .compact();
    }

    // Get username from token
    public String extractUsername(String token) {
        return parseToken(token).getBody().getSubject();
    }

    // Validate token
    public boolean validateToken(String token) {
        try {
            parseToken(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // Parse token (helper method)
    private Jws<Claims> parseToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
    }
}
