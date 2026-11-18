package com.example.agrobebederos_backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.agrobebederos_backend.dto.LecturaTemperaturaRequest;
import com.example.agrobebederos_backend.dto.LecturaTemperaturaResponse;
import com.example.agrobebederos_backend.service.TemperaturaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/lineas/{lineaId}/temperaturas")
public class TemperaturaController {

    private final TemperaturaService temperaturaService;

    public TemperaturaController(TemperaturaService temperaturaService) {
        this.temperaturaService = temperaturaService;
    }

    @PostMapping
    public ResponseEntity<LecturaTemperaturaResponse> registrar(
            @PathVariable Long lineaId,
            @Valid @RequestBody LecturaTemperaturaRequest request) {
        LecturaTemperaturaResponse response = temperaturaService.registrar(lineaId, request.getValorCelsius());
        URI location = URI.create("/api/v1/lineas/" + lineaId + "/temperaturas/" + response.getId());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<LecturaTemperaturaResponse>> listar(@PathVariable Long lineaId) {
        return ResponseEntity.ok(temperaturaService.listarHistorial(lineaId));
    }
}
