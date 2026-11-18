package com.example.agrobebederos_backend.service;

import java.nio.file.Path;

public class RecursoFotografico {

    private final Path path;
    private final String nombreOriginal;
    private final String tipoContenido;

    public RecursoFotografico(Path path, String nombreOriginal, String tipoContenido) {
        this.path = path;
        this.nombreOriginal = nombreOriginal;
        this.tipoContenido = tipoContenido;
    }

    public Path getPath() {
        return path;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }
}
