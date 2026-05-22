package com.togezzer.restapi.auth.service;

import com.togezzer.restapi.auth.dto.JwtPayload;
import com.togezzer.restapi.user.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey secretKey;

    public JwtService(@Value("${togezzer.jwt.secret:}") String secret) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT secret is missing. Set togezzer.jwt.secret or JWT_SECRET.");
        }
        if (secret.length() < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 characters.");
        }
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Genere un JWT HS256 valide 7 jours pour l'utilisateur.
     * @param user utilisateur authentifie
     * @return token JWT
     */
    public String generateToken(UserEntity user) {
        Instant now = Instant.now();
        Date issuedAt = Date.from(now);
        Date expiresAt = Date.from(now.plus(7, ChronoUnit.DAYS));

        return Jwts.builder()
            .setSubject(user.getUuid().toString())
            .claim("id", user.getId())
            .claim("uuid", user.getUuid().toString())
            .claim("email", user.getEmail())
            .claim("username", user.getUsername())
            .setIssuedAt(issuedAt)
            .setExpiration(expiresAt)
            .signWith(secretKey, SignatureAlgorithm.HS256)
            .compact();
    }

    /**
     * Verifie et decode un JWT en payload applicatif.
     * @param token JWT brut sans prefixe Bearer
     * @return payload decode
     * @throws ExpiredJwtException si le token est expire
     */
    public JwtPayload parseToken(String token) {
        Claims claims = Jwts.parserBuilder()
            .setSigningKey(secretKey)
            .build()
            .parseClaimsJws(token)
            .getBody();

        return new JwtPayload(
            claims.get("id", Long.class),
            UUID.fromString(claims.get("uuid", String.class)),
            claims.get("email", String.class),
            claims.get("username", String.class)
        );
    }
}

