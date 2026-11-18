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

@Entity
@Table(name = "lecturas_temperatura")
public class LecturaTemperatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "linea_id", nullable = false)
    private LineaBebedero linea;

    @Column(name = "valor_celsius", nullable = false, precision = 5, scale = 2)
    private BigDecimal valorCelsius;

    @Column(name = "registrada_en", nullable = false)
    private OffsetDateTime registradaEn;

    public LecturaTemperatura() {
    }

    public LecturaTemperatura(LineaBebedero linea, BigDecimal valorCelsius, OffsetDateTime registradaEn) {
        this.linea = linea;
        this.valorCelsius = valorCelsius;
        this.registradaEn = registradaEn;
    }

    public Long getId() {
        return id;
    }

    public LineaBebedero getLinea() {
        return linea;
    }

    public BigDecimal getValorCelsius() {
        return valorCelsius;
    }

    public OffsetDateTime getRegistradaEn() {
        return registradaEn;
    }
}
