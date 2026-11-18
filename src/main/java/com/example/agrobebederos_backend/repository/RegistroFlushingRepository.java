package com.example.agrobebederos_backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.agrobebederos_backend.entity.RegistroFlushing;

public interface RegistroFlushingRepository extends JpaRepository<RegistroFlushing, Long> {
    List<RegistroFlushing> findAllByOrderByRegistradoEnDescIdDesc();

    List<RegistroFlushing> findByLineaIdOrderByRegistradoEnDescIdDesc(Long lineaId);

    List<RegistroFlushing> findByLineaGalponGranjaIdOrderByRegistradoEnDescIdDesc(Long granjaId);

    Optional<RegistroFlushing> findFirstByLineaIdAndCompletadoTrueOrderByRegistradoEnDescIdDesc(Long lineaId);

    Optional<RegistroFlushing> findByClientOperationId(String clientOperationId);
}
