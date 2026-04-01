package com.ventas.cliente;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long>
{
    Page<Cliente> findByActivoTrue(Pageable pageable);

    Page<Cliente> findByActivoTrueAndNombreContainingIgnoreCase(
            String nombre, Pageable pageable);

    Optional<Cliente> findByDocumentoAndActivoTrue(String documento);

    boolean existsByDocumentoAndIdNot(String documento, Long id);
}