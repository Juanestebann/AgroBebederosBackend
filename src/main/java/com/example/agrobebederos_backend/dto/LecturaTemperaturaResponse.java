package com.example.agrobebederos_backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class LecturaTemperaturaResponse {

    private final Long id;
    private final Long lineaId;
    private final BigDecimal valorCelsius;
    private final String estado;
    private final OffsetDateTime registradaEn;

    public LecturaTemperaturaResponse(
            Long id,
            Long lineaId,
            BigDecimal valorCelsius,
            String estado,
            OffsetDateTime registradaEn) {
        this.id = id;
        this.lineaId = lineaId;
        this.valorCelsius = valorCelsius;
        this.estado = estado;
        this.registradaEn = registradaEn;
    }

    public Long getId() {
        return id;
    }

    public Long getLineaId() {
        return lineaId;
    }

    public BigDecimal getValorCelsius() {
        return valorCelsius;
    }

    public String getEstado() {
        return estado;
    }

    public OffsetDateTime getRegistradaEn() {
        return registradaEn;
    }
}
