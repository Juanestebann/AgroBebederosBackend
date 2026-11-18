package com.example.agrobebederos_backend.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "registros_flushing")
public class RegistroFlushing {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "linea_id", nullable = false)
    private LineaBebedero linea;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operario_id", nullable = false)
    private Usuario operario;

    @Column(name = "registrado_en", nullable = false)
    private OffsetDateTime registradoEn;

    @Column(nullable = false, length = 80)
    private String motivo;

    @Column(length = 500)
    private String observacion;

    @Column(nullable = false)
    private boolean completado;

    @Column(name = "temperatura_antes_c", nullable = false, precision = 5, scale = 2)
    private BigDecimal temperaturaAntesC;

    @Column(name = "temperatura_despues_c", nullable = false, precision = 5, scale = 2)
    private BigDecimal temperaturaDespuesC;

    @Column(name = "client_operation_id", nullable = false, unique = true, length = 36)
    private String clientOperationId;

    @Version
    private Long version;

    public RegistroFlushing() {
    }

    public RegistroFlushing(
            LineaBebedero linea,
            Usuario operario,
            OffsetDateTime registradoEn,
            String motivo,
            String observacion,
            BigDecimal temperaturaAntesC,
            BigDecimal temperaturaDespuesC,
            String clientOperationId) {
        this.linea = linea;
        this.operario = operario;
        this.registradoEn = registradoEn;
        this.motivo = motivo;
        this.observacion = observacion;
        this.completado = true;
        this.temperaturaAntesC = temperaturaAntesC;
        this.temperaturaDespuesC = temperaturaDespuesC;
        this.clientOperationId = clientOperationId;
    }

    public Long getId() {
        return id;
    }

    public LineaBebedero getLinea() {
        return linea;
    }

    public Usuario getOperario() {
        return operario;
    }

    public OffsetDateTime getRegistradoEn() {
        return registradoEn;
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

    public boolean isCompletado() {
        return completado;
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

    public String getClientOperationId() {
        return clientOperationId;
    }

    public Long getVersion() {
        return version;
    }
}
