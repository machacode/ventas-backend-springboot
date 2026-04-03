package com.ventas.auth;

import com.ventas.auth.dto.AuthResponse;
import com.ventas.auth.dto.LoginRequest;
import com.ventas.auth.dto.RegisterRequest;
import com.ventas.usuario.Usuario;
import com.ventas.usuario.UsuarioRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired AuthService     authService;
    @Autowired JwtService      jwtService;
    @Autowired UsuarioRepository usuarioRepository;

    // ── POST /api/auth/register ───────────────────
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);
        return ResponseEntity.status(201).body(response);
    }

    // ── POST /api/auth/login ──────────────────────
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);

        // Cookie httpOnly para refresh token ← IGUAL QUE QUARKUS ✅
        ResponseCookie cookie = ResponseCookie
                .from("refreshToken", response.accessToken)
                .httpOnly(true)
                .secure(false)      // true en producción
                .path("/api/auth")
                .maxAge(Duration.ofSeconds(
                        jwtService.getRefreshExpiration()))
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        cookie.toString())
                .body(response);
    }

    // ── POST /api/auth/refresh ────────────────────
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @CookieValue(name = "refreshToken",
                    required = false)
            String refreshToken) {

        AuthResponse response = authService
                .refresh(refreshToken);

        ResponseCookie cookie = ResponseCookie
                .from("refreshToken", response.accessToken)
                .httpOnly(true)
                .secure(false)
                .path("/api/auth")
                .maxAge(Duration.ofSeconds(
                        jwtService.getRefreshExpiration()))
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        cookie.toString())
                .body(response);
    }

    // ── POST /api/auth/logout ─────────────────────
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @AuthenticationPrincipal
            UserDetails userDetails) {

        Long usuarioId = Long.parseLong(
                userDetails.getUsername());
        authService.logout(usuarioId);

        // Borrar cookie ← IGUAL QUE QUARKUS ✅
        ResponseCookie deleteCookie = ResponseCookie
                .from("refreshToken", "")
                .httpOnly(true)
                .path("/api/auth")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        deleteCookie.toString())
                .body("{\"mensaje\":\"Sesion cerrada\"}");
    }

    // ── GET /api/auth/me ──────────────────────────
    @GetMapping("/me")
    public ResponseEntity<Usuario> me(
            @AuthenticationPrincipal
            UserDetails userDetails) {

        Long usuarioId = Long.parseLong(
                userDetails.getUsername());

        Usuario usuario = usuarioRepository
                .findById(usuarioId)
                .orElseThrow();

        usuario.password = null; // nunca exponer ✅
        return ResponseEntity.ok(usuario);
    }
}