package com.example.agrobebederos_backend.controller;

import java.net.URI;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.agrobebederos_backend.dto.EvidenciaResponse;
import com.example.agrobebederos_backend.service.EvidenciaFotograficaService;
import com.example.agrobebederos_backend.service.RecursoFotografico;

@RestController
@RequestMapping("/api/v1/flushings/{flushingId}/foto")
public class EvidenciaFotograficaController {

    private final EvidenciaFotograficaService evidenciaService;

    public EvidenciaFotograficaController(EvidenciaFotograficaService evidenciaService) {
        this.evidenciaService = evidenciaService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvidenciaResponse> guardar(
            @PathVariable Long flushingId,
            @RequestParam("archivo") MultipartFile archivo) {
        EvidenciaResponse response = evidenciaService.guardar(flushingId, archivo);
        URI location = URI.create("/api/v1/flushings/" + flushingId + "/foto");
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/metadata")
    public ResponseEntity<EvidenciaResponse> metadata(@PathVariable Long flushingId) {
        return ResponseEntity.ok(evidenciaService.obtenerDatos(flushingId));
    }

    @GetMapping
    public ResponseEntity<Resource> descargar(@PathVariable Long flushingId) {
        RecursoFotografico foto = evidenciaService.descargar(flushingId);
        FileSystemResource resource = new FileSystemResource(foto.getPath());
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(foto.getNombreOriginal())
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(foto.getTipoContenido()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(resource);
    }

    @DeleteMapping
    public ResponseEntity<Void> eliminar(@PathVariable Long flushingId) {
        evidenciaService.eliminar(flushingId);
        return ResponseEntity.noContent().build();
    }
}
