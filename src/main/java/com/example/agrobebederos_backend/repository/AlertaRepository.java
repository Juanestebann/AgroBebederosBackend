package com.example.agrobebederos_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.agrobebederos_backend.entity.Alerta;

public interface AlertaRepository extends JpaRepository<Alerta, Long> {
    List<Alerta> findAllByOrderByGeneradaEnDescIdDesc();

    List<Alerta> findByLineaGalponGranjaIdOrderByGeneradaEnDescIdDesc(Long granjaId);

    Optional<Alerta> findByLineaId(Long lineaId);
}
