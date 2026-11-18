package com.example.agrobebederos_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.agrobebederos_backend.dto.AlertaResponse;
import com.example.agrobebederos_backend.service.AlertaService;

@RestController
@RequestMapping("/api/v1/alertas")
public class AlertaController {

    private final AlertaService alertaService;

    public AlertaController(AlertaService alertaService) {
        this.alertaService = alertaService;
    }

    @GetMapping
    public ResponseEntity<List<AlertaResponse>> listar() {
        return ResponseEntity.ok(alertaService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AlertaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(alertaService.obtener(id));
    }
}
