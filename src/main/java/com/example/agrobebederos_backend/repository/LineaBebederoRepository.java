package com.example.agrobebederos_backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.agrobebederos_backend.entity.LineaBebedero;

public interface LineaBebederoRepository extends JpaRepository<LineaBebedero, Long> {
    List<LineaBebedero> findByGalponIdOrderByNombreAsc(Long galponId);

    List<LineaBebedero> findByGalponGranjaIdOrderByGalponNombreAscNombreAsc(Long granjaId);
}
