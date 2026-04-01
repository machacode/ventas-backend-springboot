package com.ventas.cliente;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "clientes")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @Column(nullable = false, length = 100)
    public String nombre;

    @Column(length = 150)
    public String email;

    @Column(length = 20)
    public String telefono;

    @Column(length = 255)
    public String direccion;

    @Column(length = 20, unique = true)
    public String documento;

    @Column(nullable = false)
    public Boolean activo = true;

    @Column(name = "creado_en")
    public LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "actualizado_en")
    public LocalDateTime actualizadoEn = LocalDateTime.now();
}