package com.example.agrobebederos_backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.agrobebederos_backend.entity.EvidenciaFotografica;

public interface EvidenciaFotograficaRepository extends JpaRepository<EvidenciaFotografica, Long> {
    Optional<EvidenciaFotografica> findByFlushingId(Long flushingId);
}
