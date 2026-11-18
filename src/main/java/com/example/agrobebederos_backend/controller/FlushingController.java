package com.example.agrobebederos_backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.agrobebederos_backend.dto.ActualizarFlushingRequest;
import com.example.agrobebederos_backend.dto.CrearFlushingRequest;
import com.example.agrobebederos_backend.dto.FlushingResponse;
import com.example.agrobebederos_backend.service.FlushingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class FlushingController {

    private final FlushingService flushingService;

    public FlushingController(FlushingService flushingService) {
        this.flushingService = flushingService;
    }

    @PostMapping("/lineas/{lineaId}/flushings")
    public ResponseEntity<FlushingResponse> crear(
            @PathVariable Long lineaId,
            @Valid @RequestBody CrearFlushingRequest request) {
        FlushingResponse response = flushingService.crear(lineaId, request);
        return ResponseEntity.created(URI.create("/api/v1/flushings/" + response.getId())).body(response);
    }

    @GetMapping("/flushings")
    public ResponseEntity<List<FlushingResponse>> listar() {
        return ResponseEntity.ok(flushingService.listar());
    }

    @GetMapping("/lineas/{lineaId}/flushings")
    public ResponseEntity<List<FlushingResponse>> listarPorLinea(@PathVariable Long lineaId) {
        return ResponseEntity.ok(flushingService.listarPorLinea(lineaId));
    }

    @GetMapping("/flushings/{id}")
    public ResponseEntity<FlushingResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(flushingService.obtener(id));
    }

    @PatchMapping("/flushings/{id}")
    public ResponseEntity<FlushingResponse> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ActualizarFlushingRequest request) {
        return ResponseEntity.ok(flushingService.actualizar(id, request));
    }

    @DeleteMapping("/flushings/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        flushingService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
