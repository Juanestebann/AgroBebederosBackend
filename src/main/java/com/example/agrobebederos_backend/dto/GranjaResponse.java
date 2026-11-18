package com.example.agrobebederos_backend.dto;

public class GranjaResponse {

    private final Long id;
    private final String nombre;

    public GranjaResponse(Long id, String nombre) {
        this.id = id;
        this.nombre = nombre;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }
}
