package com.example.agrobebederos_backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class LecturaTemperaturaRequest {

    @NotNull(message = "La temperatura es obligatoria.")
    @DecimalMin(value = "0.0", inclusive = true, message = "La temperatura debe ser mayor o igual a 0 °C.")
    @DecimalMax(value = "50.0", inclusive = true, message = "La temperatura debe ser menor o igual a 50 °C.")
    private BigDecimal valorCelsius;

    public LecturaTemperaturaRequest() {
    }

    public BigDecimal getValorCelsius() {
        return valorCelsius;
    }

    public void setValorCelsius(BigDecimal valorCelsius) {
        this.valorCelsius = valorCelsius;
    }
}
