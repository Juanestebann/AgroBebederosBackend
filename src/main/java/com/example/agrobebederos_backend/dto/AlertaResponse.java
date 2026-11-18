package com.example.agrobebederos_backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class AlertaResponse {

    private final Long id;
    private final Long lineaId;
    private final String lineaNombre;
    private final Long galponId;
    private final String galponNombre;
    private final Long granjaId;
    private final String granjaNombre;
    private final BigDecimal temperaturaC;
    private final String estado;
    private final OffsetDateTime generadaEn;

    public AlertaResponse(
            Long id,
            Long lineaId,
            String lineaNombre,
            Long galponId,
            String galponNombre,
            Long granjaId,
            String granjaNombre,
            BigDecimal temperaturaC,
            String estado,
            OffsetDateTime generadaEn) {
        this.id = id;
        this.lineaId = lineaId;
        this.lineaNombre = lineaNombre;
        this.galponId = galponId;
        this.galponNombre = galponNombre;
        this.granjaId = granjaId;
        this.granjaNombre = granjaNombre;
        this.temperaturaC = temperaturaC;
        this.estado = estado;
        this.generadaEn = generadaEn;
    }

    public Long getId() { return id; }
    public Long getLineaId() { return lineaId; }
    public String getLineaNombre() { return lineaNombre; }
    public Long getGalponId() { return galponId; }
    public String getGalponNombre() { return galponNombre; }
    public Long getGranjaId() { return granjaId; }
    public String getGranjaNombre() { return granjaNombre; }
    public BigDecimal getTemperaturaC() { return temperaturaC; }
    public String getEstado() { return estado; }
    public OffsetDateTime getGeneradaEn() { return generadaEn; }
}
