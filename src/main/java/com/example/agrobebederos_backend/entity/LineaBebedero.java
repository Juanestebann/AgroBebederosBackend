package com.example.agrobebederos_backend.entity;

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
@Table(name = "lineas_bebedero")
public class LineaBebedero {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "galpon_id", nullable = false)
    private Galpon galpon;

    public LineaBebedero() {
    }

    public LineaBebedero(String nombre, Galpon galpon) {
        this.nombre = nombre;
        this.galpon = galpon;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Galpon getGalpon() {
        return galpon;
    }

    public void setGalpon(Galpon galpon) {
        this.galpon = galpon;
    }
}
