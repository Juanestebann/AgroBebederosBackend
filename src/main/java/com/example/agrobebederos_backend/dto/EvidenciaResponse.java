package com.example.agrobebederos_backend.dto;

import java.time.OffsetDateTime;

public class EvidenciaResponse {

    private final Long id;
    private final Long flushingId;
    private final String nombreOriginal;
    private final String tipoContenido;
    private final Long tamanoBytes;
    private final OffsetDateTime creadaEn;

    public EvidenciaResponse(
            Long id,
            Long flushingId,
            String nombreOriginal,
            String tipoContenido,
            Long tamanoBytes,
            OffsetDateTime creadaEn) {
        this.id = id;
        this.flushingId = flushingId;
        this.nombreOriginal = nombreOriginal;
        this.tipoContenido = tipoContenido;
        this.tamanoBytes = tamanoBytes;
        this.creadaEn = creadaEn;
    }

    public Long getId() { return id; }
    public Long getFlushingId() { return flushingId; }
    public String getNombreOriginal() { return nombreOriginal; }
    public String getTipoContenido() { return tipoContenido; }
    public Long getTamanoBytes() { return tamanoBytes; }
    public OffsetDateTime getCreadaEn() { return creadaEn; }
}
