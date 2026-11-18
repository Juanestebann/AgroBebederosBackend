package com.example.agrobebederos_backend.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class FlushingResponse {

    private final Long id;
    private final Long lineaId;
    private final String lineaNombre;
    private final Long galponId;
    private final String galponNombre;
    private final Long granjaId;
    private final String granjaNombre;
    private final Long operarioId;
    private final String operarioNombre;
    private final OffsetDateTime registradoEn;
    private final String motivo;
    private final String observacion;
    private final boolean completado;
    private final BigDecimal temperaturaAntesC;
    private final BigDecimal temperaturaDespuesC;
    private final String clientOperationId;
    private final Long version;

    public FlushingResponse(
            Long id,
            Long lineaId,
            String lineaNombre,
            Long galponId,
            String galponNombre,
            Long granjaId,
            String granjaNombre,
            Long operarioId,
            String operarioNombre,
            OffsetDateTime registradoEn,
            String motivo,
            String observacion,
            boolean completado,
            BigDecimal temperaturaAntesC,
            BigDecimal temperaturaDespuesC,
            String clientOperationId,
            Long version) {
        this.id = id;
        this.lineaId = lineaId;
        this.lineaNombre = lineaNombre;
        this.galponId = galponId;
        this.galponNombre = galponNombre;
        this.granjaId = granjaId;
        this.granjaNombre = granjaNombre;
        this.operarioId = operarioId;
        this.operarioNombre = operarioNombre;
        this.registradoEn = registradoEn;
        this.motivo = motivo;
        this.observacion = observacion;
        this.completado = completado;
        this.temperaturaAntesC = temperaturaAntesC;
        this.temperaturaDespuesC = temperaturaDespuesC;
        this.clientOperationId = clientOperationId;
        this.version = version;
    }

    public Long getId() { return id; }
    public Long getLineaId() { return lineaId; }
    public String getLineaNombre() { return lineaNombre; }
    public Long getGalponId() { return galponId; }
    public String getGalponNombre() { return galponNombre; }
    public Long getGranjaId() { return granjaId; }
    public String getGranjaNombre() { return granjaNombre; }
    public Long getOperarioId() { return operarioId; }
    public String getOperarioNombre() { return operarioNombre; }
    public OffsetDateTime getRegistradoEn() { return registradoEn; }
    public String getMotivo() { return motivo; }
    public String getObservacion() { return observacion; }
    public boolean isCompletado() { return completado; }
    public BigDecimal getTemperaturaAntesC() { return temperaturaAntesC; }
    public BigDecimal getTemperaturaDespuesC() { return temperaturaDespuesC; }
    public String getClientOperationId() { return clientOperationId; }
    public Long getVersion() { return version; }
}
