package com.example.agrobebederos_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.agrobebederos_backend.entity.Granja;

public interface GranjaRepository extends JpaRepository<Granja, Long> {
    List<Granja> findAllByOrderByNombreAsc();
}
