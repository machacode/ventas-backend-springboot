package com.ventas.auth;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, unique = true,
            columnDefinition = "TEXT")
    public String token;

    @Column(name = "usuario_id", nullable = false)
    public Long usuarioId;

    @Column(name = "expira_en", nullable = false)
    public LocalDateTime expiraEn;

    @Column(name = "creado_en")
    public LocalDateTime creadoEn = LocalDateTime.now();

    @Column(nullable = false)
    public Boolean revocado = false;
}