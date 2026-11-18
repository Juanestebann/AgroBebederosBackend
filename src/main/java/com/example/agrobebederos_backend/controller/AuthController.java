package com.example.agrobebederos_backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.agrobebederos_backend.dto.UsuarioResponse;
import com.example.agrobebederos_backend.service.UsuarioActualService;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final UsuarioActualService usuarioActualService;

    public AuthController(UsuarioActualService usuarioActualService) {
        this.usuarioActualService = usuarioActualService;
    }

    @PostMapping("/auth/login")
    public ResponseEntity<UsuarioResponse> login() {
        return ResponseEntity.ok(usuarioActualService.obtenerResponse());
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponse> me() {
        return ResponseEntity.ok(usuarioActualService.obtenerResponse());
    }
}
