package com.example.room_manager.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final String SECRET_KEY = "my-secret-key-for-jwt-should-be-long-32bytes";
    private final long EXPIRATION_MS = 1000 * 60 * 60; // 1시간

    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));

    // 토큰 생성
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .setSubject(email)               // email 저장
                .claim("role", role)             // role 저장
                .setIssuedAt(new Date())         // 생성일
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS)) // 만료일
                .signWith(key, SignatureAlgorithm.HS256) // 서명 키
                .compact();
    }

    // 토큰 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    // 토큰에서 이메일 추출
    public String getEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // 토큰에서 role 추출
    public String getRole(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("role", String.class);
    }
}
