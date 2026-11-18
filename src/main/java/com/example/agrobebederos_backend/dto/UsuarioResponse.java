package com.example.agrobebederos_backend.dto;

public class UsuarioResponse {

    private final Long id;
    private final String nombre;
    private final String email;
    private final String rol;
    private final Long granjaId;
    private final String granjaNombre;

    public UsuarioResponse(
            Long id,
            String nombre,
            String email,
            String rol,
            Long granjaId,
            String granjaNombre) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.granjaId = granjaId;
        this.granjaNombre = granjaNombre;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }

    public String getRol() {
        return rol;
    }

    public Long getGranjaId() {
        return granjaId;
    }

    public String getGranjaNombre() {
        return granjaNombre;
    }
}
