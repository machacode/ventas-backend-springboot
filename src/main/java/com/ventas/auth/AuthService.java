package com.ventas.auth;

import com.ventas.auth.dto.AuthResponse;
import com.ventas.auth.dto.LoginRequest;
import com.ventas.auth.dto.RegisterRequest;
import com.ventas.usuario.Usuario;
import com.ventas.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordService passwordService;

    @Autowired
    private JwtService jwtService;

    // ── LOGIN ─────────────────────────────────────
    @Transactional  // IGUAL QUE QUARKUS ✅
    public AuthResponse login(LoginRequest request) {

        Usuario usuario = usuarioRepository
                .findByEmail(request.email)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Credenciales incorrectas"));

        if (!passwordService.verify(
                request.password, usuario.password)) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Credenciales incorrectas");
        }

        if (!usuario.activo) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Usuario desactivado");
        }

        return buildResponse(usuario);
    }

    // ── REGISTER ──────────────────────────────────
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        if (usuarioRepository.existsByEmail(request.email)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El email ya esta registrado");
        }

        Usuario nuevo   = new Usuario();
        nuevo.nombre    = request.nombre;
        nuevo.email     = request.email;
        nuevo.password  = passwordService.hash(
                request.password);
        nuevo.rol       = "VENDEDOR";
        nuevo.activo    = true;

        usuarioRepository.save(nuevo);

        return buildResponse(nuevo);
    }

    // ── REFRESH ───────────────────────────────────
    @Transactional
    public AuthResponse refresh(String refreshToken) {

        if (refreshToken == null ||
                refreshToken.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Refresh token requerido");
        }

        RefreshToken stored = refreshTokenRepository
                .findByTokenAndRevocadoFalseAndExpiraEnAfter(
                        refreshToken, LocalDateTime.now())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Refresh token invalido o expirado"));

        Usuario usuario = usuarioRepository
                .findById(stored.usuarioId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.UNAUTHORIZED,
                                "Usuario no valido"));

        if (!usuario.activo) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Usuario desactivado");
        }

        // Revocar token usado (rotación) ← IGUAL QUE QUARKUS ✅
        stored.revocado = true;
        refreshTokenRepository.save(stored);

        return buildResponse(usuario);
    }

    // ── LOGOUT ────────────────────────────────────
    @Transactional
    public void logout(Long usuarioId) {
        // IGUAL QUE QUARKUS ✅
        refreshTokenRepository
                .revocarTodosPorUsuario(usuarioId);
    }

    // ── HELPER: construir respuesta ───────────────
    private AuthResponse buildResponse(Usuario usuario) {

        String accessToken  = jwtService
                .generateAccessToken(usuario);
        String refreshToken = jwtService
                .generateRefreshToken(usuario);

        // Guardar refresh token ← IGUAL QUE QUARKUS ✅
        RefreshToken rt  = new RefreshToken();
        rt.token         = refreshToken;
        rt.usuarioId     = usuario.id;
        rt.expiraEn      = LocalDateTime.now()
                .plusSeconds(
                        jwtService.getRefreshExpiration());
        rt.revocado      = false;
        refreshTokenRepository.save(rt);

        return new AuthResponse(
                accessToken,
                jwtService.getExpiration(),
                usuario.id,
                usuario.nombre,
                usuario.email,
                usuario.rol
        );
    }
}