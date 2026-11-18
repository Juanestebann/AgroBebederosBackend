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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "alertas")
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "linea_id", nullable = false, unique = true)
    private LineaBebedero linea;

    @Column(name = "temperatura_c", nullable = false, precision = 5, scale = 2)
    private BigDecimal temperaturaC;

    @Column(name = "generada_en", nullable = false)
    private OffsetDateTime generadaEn;

    public Alerta() {
    }

    public Alerta(LineaBebedero linea, BigDecimal temperaturaC, OffsetDateTime generadaEn) {
        this.linea = linea;
        this.temperaturaC = temperaturaC;
        this.generadaEn = generadaEn;
    }

    public Long getId() {
        return id;
    }

    public LineaBebedero getLinea() {
        return linea;
    }

    public BigDecimal getTemperaturaC() {
        return temperaturaC;
    }

    public void setTemperaturaC(BigDecimal temperaturaC) {
        this.temperaturaC = temperaturaC;
    }

    public OffsetDateTime getGeneradaEn() {
        return generadaEn;
    }

    public void setGeneradaEn(OffsetDateTime generadaEn) {
        this.generadaEn = generadaEn;
    }
}
