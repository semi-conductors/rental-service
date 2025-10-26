package com.rentmate.service.rental.shared.utility;

import com.rentmate.service.rental.shared.exception.UnauthorizedAccessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.servlet.http.HttpServletRequest;
import io.jsonwebtoken.security.Keys;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
@Component
public class JwtUtils {
    private final SecretKey key;

    public JwtUtils(@Value("${jwt.secret-key:this-is-not-a-key-at-all}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUserId(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            } else {
                throw new UnauthorizedAccessException("Invalid or missing Bearer token");
            }

            Claims claims = extractAllClaims(token);
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            e.printStackTrace();
            throw new UnauthorizedAccessException("Invalid or expired JWT token");
        }
    }
    public  Long getExtractedId(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            throw new UnauthorizedAccessException("Authorization header is missing");
        }

        return extractUserId(token);
    }

}
