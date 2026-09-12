package com.vocal.app.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Slf4j @Component
public class JwtTokenProvider {
    @Value("${jwt.secret}") private String secretString;
    @Value("${jwt.access-token-expiration}") private long accessTokenExpiration;
    @Value("${jwt.refresh-token-expiration}") private long refreshTokenExpiration;
    private SecretKey secretKey;

    @PostConstruct
    public void init() { secretKey = Keys.hmacShaKeyFor(secretString.getBytes(StandardCharsets.UTF_8)); }

    public String createAccessToken(String email) { return buildToken(email, accessTokenExpiration); }
    public String createRefreshToken(String email) { return buildToken(email, refreshTokenExpiration); }

    private String buildToken(String subject, long expiration) {
        Date now = new Date();
        return Jwts.builder().subject(subject).issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(secretKey).compact();
    }

    public String getEmail(String token) { return parseClaims(token).getSubject(); }

    public boolean validateToken(String token) {
        try { parseClaims(token); return true; }
        catch (JwtException | IllegalArgumentException e) { log.warn("Invalid JWT: {}", e.getMessage()); return false; }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token).getPayload();
    }

    public long getAccessTokenExpiration() { return accessTokenExpiration; }
}
