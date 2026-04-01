package com.ventas.kardex;

import com.ventas.producto.Producto;
import com.ventas.usuario.Usuario;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "kardex")
public class Kardex {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    public Producto producto;

    @Column(nullable = false, length = 10)
    public String tipo;

    @Column(nullable = false)
    public Integer cantidad;

    @Column(name = "stock_anterior", nullable = false)
    public Integer stockAnterior;

    @Column(name = "stock_posterior", nullable = false)
    public Integer stockPosterior;

    @Column(length = 100)
    public String motivo;

    @Column(name = "referencia_id")
    public Long referenciaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    public Usuario usuario;

    @Column(name = "creado_en")
    public LocalDateTime creadoEn = LocalDateTime.now();
}