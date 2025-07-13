package com.jeannychiu.learningnotesapi.security;

import com.jeannychiu.learningnotesapi.exception.InvalidTokenException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey;
    @Value("${jwt.expiration}")
    private long expirationTime;

    public String generateToken(String email, String role) {
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .subject(email)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();
    }

    public String getEmailFromToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            throw new InvalidTokenException("無效的 JWT token: " + e.getMessage());
        }

    };

    public boolean validateToken(String token, String email) {
        try {
            // 1. 解析 token（如果有錯誤可直接丟 exception）
            Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            String tokenEmail = claims.getSubject();
            Date expiration = claims.getExpiration();

            // 2. 比對 getEmailFromToken(token) 是否等於 email
            return tokenEmail.equals(email) && expiration.after(new Date());
        } catch (Exception e) {
            throw new InvalidTokenException("無效的 JWT token: " + e.getMessage());
        }
    };

    public String getRoleFromToken(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            System.out.println("role from jwt: " + claims.get("role", String.class));

            return claims.get("role", String.class);


        } catch (Exception e) {
            throw new InvalidTokenException("無效的 JWT token: " + e.getMessage());
        }

    }

    public boolean isTokenExpired(String token) {
        try {
            Key key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith((SecretKey) key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            throw new InvalidTokenException("無效的 JWT token: " + e.getMessage());
        }
    }

}
