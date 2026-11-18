package com.example.agrobebederos_backend.dto;

public class LineaBebederoResponse {

    private final Long id;
    private final String nombre;
    private final Long galponId;
    private final String galponNombre;
    private final Long granjaId;
    private final String granjaNombre;
    private final TemperaturaEfectivaResponse temperaturaEfectiva;

    public LineaBebederoResponse(
            Long id,
            String nombre,
            Long galponId,
            String galponNombre,
            Long granjaId,
            String granjaNombre,
            TemperaturaEfectivaResponse temperaturaEfectiva) {
        this.id = id;
        this.nombre = nombre;
        this.galponId = galponId;
        this.galponNombre = galponNombre;
        this.granjaId = granjaId;
        this.granjaNombre = granjaNombre;
        this.temperaturaEfectiva = temperaturaEfectiva;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Long getGalponId() {
        return galponId;
    }

    public String getGalponNombre() {
        return galponNombre;
    }

    public Long getGranjaId() {
        return granjaId;
    }

    public String getGranjaNombre() {
        return granjaNombre;
    }

    public TemperaturaEfectivaResponse getTemperaturaEfectiva() {
        return temperaturaEfectiva;
    }
}
