package com.example.agrobebederos_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.agrobebederos_backend.entity.Galpon;

public interface GalponRepository extends JpaRepository<Galpon, Long> {
    List<Galpon> findByGranjaIdOrderByNombreAsc(Long granjaId);
}
