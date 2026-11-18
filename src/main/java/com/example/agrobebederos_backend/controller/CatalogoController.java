package com.example.agrobebederos_backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.agrobebederos_backend.dto.GalponResponse;
import com.example.agrobebederos_backend.dto.GranjaResponse;
import com.example.agrobebederos_backend.dto.LineaBebederoResponse;
import com.example.agrobebederos_backend.service.CatalogoService;

@RestController
@RequestMapping("/api/v1")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping("/granjas")
    public ResponseEntity<List<GranjaResponse>> listarGranjas() {
        return ResponseEntity.ok(catalogoService.listarGranjas());
    }

    @GetMapping("/granjas/{granjaId}/galpones")
    public ResponseEntity<List<GalponResponse>> listarGalpones(@PathVariable Long granjaId) {
        return ResponseEntity.ok(catalogoService.listarGalpones(granjaId));
    }

    @GetMapping("/galpones/{galponId}/lineas")
    public ResponseEntity<List<LineaBebederoResponse>> listarLineas(@PathVariable Long galponId) {
        return ResponseEntity.ok(catalogoService.listarLineas(galponId));
    }

    @GetMapping("/lineas/{lineaId}")
    public ResponseEntity<LineaBebederoResponse> obtenerLinea(@PathVariable Long lineaId) {
        return ResponseEntity.ok(catalogoService.obtenerLinea(lineaId));
    }
}
