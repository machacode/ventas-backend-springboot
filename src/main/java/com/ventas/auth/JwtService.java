package com.ventas.auth;

import com.ventas.usuario.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private Long expiration;

    @Value("${app.jwt.refresh-expiration}")
    private Long refreshExpiration;

    @Value("${app.jwt.issuer}")
    private String issuer;

    // ── Generar Access Token ──────────────────────
    public String generateAccessToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email",  usuario.email);
        claims.put("nombre", usuario.nombre);
        claims.put("roles",  usuario.rol);
        claims.put("type",   "access");

        return buildToken(
                claims,
                usuario.id.toString(),
                expiration
        );
    }

    // ── Generar Refresh Token ─────────────────────
    public String generateRefreshToken(Usuario usuario) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        return buildToken(
                claims,
                usuario.id.toString(),
                refreshExpiration
        );
    }

    // ── Extraer Subject (userId) ──────────────────
    public String extractSubject(String token) {
        return extractClaims(token).getSubject();
    }

    // ── Validar Token ─────────────────────────────
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return !claims.getExpiration()
                    .before(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    // ── Obtener duración access token ─────────────
    public Long getExpiration() {
        return expiration / 1000; // convertir ms → segundos
    }

    // ── Obtener duración refresh token ────────────
    public Long getRefreshExpiration() {
        return refreshExpiration / 1000;
    }

    // ── Helper: construir token ───────────────────
    private String buildToken(
            Map<String, Object> claims,
            String subject,
            long expirationMs) {

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuer(issuer)
                .issuedAt(new Date())
                .expiration(new Date(
                        System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    // ── Helper: extraer claims ────────────────────
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // ── Helper: obtener llave de firma ────────────
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(
                StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}