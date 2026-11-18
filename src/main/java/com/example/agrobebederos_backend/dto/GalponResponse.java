package com.example.agrobebederos_backend.dto;

public class GalponResponse {

    private final Long id;
    private final String nombre;
    private final Long granjaId;
    private final String granjaNombre;

    public GalponResponse(Long id, String nombre, Long granjaId, String granjaNombre) {
        this.id = id;
        this.nombre = nombre;
        this.granjaId = granjaId;
        this.granjaNombre = granjaNombre;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Long getGranjaId() {
        return granjaId;
    }

    public String getGranjaNombre() {
        return granjaNombre;
    }
}
