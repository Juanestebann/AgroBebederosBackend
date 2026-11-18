package com.example.agrobebederos_backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CrearFlushingRequest {

    @NotBlank(message = "El identificador de operación es obligatorio.")
    @Size(max = 36, message = "El identificador de operación no puede superar 36 caracteres.")
    private String clientOperationId;

    @NotBlank(message = "El motivo es obligatorio.")
    @Size(max = 80, message = "El motivo no puede superar 80 caracteres.")
    private String motivo;

    @Size(max = 500, message = "La observación no puede superar 500 caracteres.")
    private String observacion;

    @NotNull(message = "La temperatura anterior es obligatoria.")
    @DecimalMin(value = "0.0", message = "La temperatura anterior debe ser mayor o igual a 0 °C.")
    @DecimalMax(value = "50.0", message = "La temperatura anterior debe ser menor o igual a 50 °C.")
    private BigDecimal temperaturaAntesC;

    @NotNull(message = "La temperatura posterior es obligatoria.")
    @DecimalMin(value = "0.0", message = "La temperatura posterior debe ser mayor o igual a 0 °C.")
    @DecimalMax(value = "50.0", message = "La temperatura posterior debe ser menor o igual a 50 °C.")
    private BigDecimal temperaturaDespuesC;

    public CrearFlushingRequest() {
    }

    public String getClientOperationId() {
        return clientOperationId;
    }

    public void setClientOperationId(String clientOperationId) {
        this.clientOperationId = clientOperationId;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getObservacion() {
        return observacion;
    }

    public void setObservacion(String observacion) {
        this.observacion = observacion;
    }

    public BigDecimal getTemperaturaAntesC() {
        return temperaturaAntesC;
    }

    public void setTemperaturaAntesC(BigDecimal temperaturaAntesC) {
        this.temperaturaAntesC = temperaturaAntesC;
    }

    public BigDecimal getTemperaturaDespuesC() {
        return temperaturaDespuesC;
    }

    public void setTemperaturaDespuesC(BigDecimal temperaturaDespuesC) {
        this.temperaturaDespuesC = temperaturaDespuesC;
    }
}
