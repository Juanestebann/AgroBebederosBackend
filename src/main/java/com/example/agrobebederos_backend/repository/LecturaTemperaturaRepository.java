package com.example.agrobebederos_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.agrobebederos_backend.entity.LecturaTemperatura;

public interface LecturaTemperaturaRepository extends JpaRepository<LecturaTemperatura, Long> {
    List<LecturaTemperatura> findByLineaIdOrderByRegistradaEnDescIdDesc(Long lineaId);

    Optional<LecturaTemperatura> findFirstByLineaIdOrderByRegistradaEnDescIdDesc(Long lineaId);
}
