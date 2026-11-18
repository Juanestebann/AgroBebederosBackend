package com.example.agrobebederos_backend.entity;

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
@Table(name = "evidencias_fotograficas")
public class EvidenciaFotografica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flushing_id", nullable = false, unique = true)
    private RegistroFlushing flushing;

    @Column(name = "nombre_almacenado", nullable = false, unique = true)
    private String nombreAlmacenado;

    @Column(name = "nombre_original", nullable = false)
    private String nombreOriginal;

    @Column(name = "tipo_contenido", nullable = false, length = 100)
    private String tipoContenido;

    @Column(name = "tamano_bytes", nullable = false)
    private Long tamanoBytes;

    @Column(name = "creada_en", nullable = false)
    private OffsetDateTime creadaEn;

    public EvidenciaFotografica() {
    }

    public EvidenciaFotografica(
            RegistroFlushing flushing,
            String nombreAlmacenado,
            String nombreOriginal,
            String tipoContenido,
            Long tamanoBytes,
            OffsetDateTime creadaEn) {
        this.flushing = flushing;
        this.nombreAlmacenado = nombreAlmacenado;
        this.nombreOriginal = nombreOriginal;
        this.tipoContenido = tipoContenido;
        this.tamanoBytes = tamanoBytes;
        this.creadaEn = creadaEn;
    }

    public Long getId() {
        return id;
    }

    public RegistroFlushing getFlushing() {
        return flushing;
    }

    public String getNombreAlmacenado() {
        return nombreAlmacenado;
    }

    public String getNombreOriginal() {
        return nombreOriginal;
    }

    public String getTipoContenido() {
        return tipoContenido;
    }

    public Long getTamanoBytes() {
        return tamanoBytes;
    }

    public OffsetDateTime getCreadaEn() {
        return creadaEn;
    }
}
