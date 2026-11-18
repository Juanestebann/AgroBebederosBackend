package com.example.agrobebederos_backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TemperaturaEfectivaResponse {

    private final BigDecimal valorCelsius;
    private final String estado;
    private final String origen;
    private final OffsetDateTime registradaEn;

    public TemperaturaEfectivaResponse(
            BigDecimal valorCelsius,
            String estado,
            String origen,
            OffsetDateTime registradaEn) {
        this.valorCelsius = valorCelsius;
        this.estado = estado;
        this.origen = origen;
        this.registradaEn = registradaEn;
    }

    public BigDecimal getValorCelsius() {
        return valorCelsius;
    }

    public String getEstado() {
        return estado;
    }

    public String getOrigen() {
        return origen;
    }

    public OffsetDateTime getRegistradaEn() {
        return registradaEn;
    }
}
