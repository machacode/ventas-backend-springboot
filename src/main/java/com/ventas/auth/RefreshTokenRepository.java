package com.ventas.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenAndRevocadoFalseAndExpiraEnAfter(
            String token, LocalDateTime ahora);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revocado = true " +
            "WHERE r.usuarioId = :usuarioId")
    void revocarTodosPorUsuario(Long usuarioId);
}