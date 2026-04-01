package com.ventas.kardex;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface KardexRepository
        extends JpaRepository<Kardex, Long> {

    Page<Kardex> findByProductoIdOrderByCreadoEnDesc(
            Long productoId, Pageable pageable);

    Page<Kardex> findByProductoIdAndCreadoEnBetweenOrderByCreadoEnDesc(
            Long productoId,
            LocalDateTime desde,
            LocalDateTime hasta,
            Pageable pageable);

    Optional<Kardex> findFirstByProductoIdOrderByCreadoEnDesc(Long productoId);

    long countByProductoIdAndCreadoEnBetween(
            Long productoId,
            LocalDateTime desde,
            LocalDateTime hasta);
}