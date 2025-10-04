package com.rentmate.service.rental.shared.utility;

import com.rentmate.service.rental.shared.exception.UnauthorizedAccessException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;

public class JwtUtils {
    private static final String SECRET_KEY = "yourSecretKeyHere";

    public static Long extractUserId(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            } else {
                throw new UnauthorizedAccessException("Invalid or missing Bearer token");
            }

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY.getBytes())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("userId", Long.class);
        } catch (JwtException e) {
            throw new UnauthorizedAccessException("Invalid or expired JWT token");
        }
    }
    public static Long getExtractedId(HttpServletRequest request){
        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            throw new UnauthorizedAccessException("Authorization header is missing");
        }

        return JwtUtils.extractUserId(token);
    }
}
