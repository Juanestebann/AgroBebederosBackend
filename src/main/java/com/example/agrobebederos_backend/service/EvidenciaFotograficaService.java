package com.example.agrobebederos_backend.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.example.agrobebederos_backend.dto.EvidenciaResponse;
import com.example.agrobebederos_backend.entity.EvidenciaFotografica;
import com.example.agrobebederos_backend.entity.RegistroFlushing;
import com.example.agrobebederos_backend.entity.Usuario;
import com.example.agrobebederos_backend.exception.RecursoNoEncontradoException;
import com.example.agrobebederos_backend.exception.SolicitudInvalidaException;
import com.example.agrobebederos_backend.repository.EvidenciaFotograficaRepository;
import com.example.agrobebederos_backend.repository.RegistroFlushingRepository;

@Service
public class EvidenciaFotograficaService {

    private static final long TAMANO_MAXIMO = 5L * 1024L * 1024L;
    private static final Logger LOGGER = LoggerFactory.getLogger(EvidenciaFotograficaService.class);

    private final EvidenciaFotograficaRepository evidenciaRepository;
    private final RegistroFlushingRepository flushingRepository;
    private final UsuarioActualService usuarioActualService;
    private final AccesoService accesoService;
    private final Clock clock;
    private final Path directorio;

    public EvidenciaFotograficaService(
            EvidenciaFotograficaRepository evidenciaRepository,
            RegistroFlushingRepository flushingRepository,
            UsuarioActualService usuarioActualService,
            AccesoService accesoService,
            Clock clock,
            @Value("${app.storage.photos}") String directorio) {
        this.evidenciaRepository = evidenciaRepository;
        this.flushingRepository = flushingRepository;
        this.usuarioActualService = usuarioActualService;
        this.accesoService = accesoService;
        this.clock = clock;
        this.directorio = Path.of(directorio).toAbsolutePath().normalize();
    }

    @Transactional
    public EvidenciaResponse guardar(Long flushingId, MultipartFile archivo) {
        Usuario usuario = usuarioActualService.obtener();
        RegistroFlushing flushing = buscarFlushing(flushingId);
        accesoService.verificarPropietario(usuario, flushing);
        validarArchivo(archivo);
        verificarSincronizacionActiva();

        String extension = "image/png".equals(archivo.getContentType()) ? ".png" : ".jpg";
        String nombreAlmacenado = UUID.randomUUID().toString() + extension;
        Path destino = resolver(nombreAlmacenado);

        try {
            Files.createDirectories(directorio);
            try (InputStream input = archivo.getInputStream()) {
                Files.copy(input, destino, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new SolicitudInvalidaException("No se pudo guardar la fotografía.");
        }
        programarEliminacionSiRollback(nombreAlmacenado);

        Optional<EvidenciaFotografica> anterior = evidenciaRepository.findByFlushingId(flushingId);
        if (anterior.isPresent()) {
            EvidenciaFotografica evidenciaAnterior = anterior.get();
            evidenciaRepository.delete(evidenciaAnterior);
            evidenciaRepository.flush();
            programarEliminacionTrasCommit(evidenciaAnterior.getNombreAlmacenado());
        }

        String nombreOriginal = archivo.getOriginalFilename();
        if (nombreOriginal == null || nombreOriginal.isBlank()) {
            nombreOriginal = "fotografia" + extension;
        } else {
            nombreOriginal = Path.of(nombreOriginal).getFileName().toString();
        }

        EvidenciaFotografica evidencia = new EvidenciaFotografica(
                flushing,
                nombreAlmacenado,
                nombreOriginal,
                archivo.getContentType(),
                archivo.getSize(),
                OffsetDateTime.now(clock));
        evidencia = evidenciaRepository.save(evidencia);
        return convertir(evidencia);
    }

    @Transactional(readOnly = true)
    public EvidenciaResponse obtenerDatos(Long flushingId) {
        Usuario usuario = usuarioActualService.obtener();
        RegistroFlushing flushing = buscarFlushing(flushingId);
        accesoService.verificarAccesoLinea(usuario, flushing.getLinea());
        EvidenciaFotografica evidencia = buscarEvidencia(flushingId);
        return convertir(evidencia);
    }

    @Transactional(readOnly = true)
    public RecursoFotografico descargar(Long flushingId) {
        Usuario usuario = usuarioActualService.obtener();
        RegistroFlushing flushing = buscarFlushing(flushingId);
        accesoService.verificarAccesoLinea(usuario, flushing.getLinea());
        EvidenciaFotografica evidencia = buscarEvidencia(flushingId);
        Path path = resolver(evidencia.getNombreAlmacenado());
        if (!Files.isRegularFile(path)) {
            throw new RecursoNoEncontradoException("El archivo fotográfico no está disponible.");
        }
        return new RecursoFotografico(path, evidencia.getNombreOriginal(), evidencia.getTipoContenido());
    }

    @Transactional
    public void eliminar(Long flushingId) {
        Usuario usuario = usuarioActualService.obtener();
        RegistroFlushing flushing = buscarFlushing(flushingId);
        accesoService.verificarPropietario(usuario, flushing);
        eliminarInterno(flushing);
    }

    @Transactional
    public void eliminarInterno(RegistroFlushing flushing) {
        verificarSincronizacionActiva();
        Optional<EvidenciaFotografica> evidencia = evidenciaRepository.findByFlushingId(flushing.getId());
        if (evidencia.isPresent()) {
            EvidenciaFotografica existente = evidencia.get();
            evidenciaRepository.delete(existente);
            evidenciaRepository.flush();
            programarEliminacionTrasCommit(existente.getNombreAlmacenado());
        }
    }

    private void validarArchivo(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new SolicitudInvalidaException("La fotografía está vacía.");
        }
        if (archivo.getSize() > TAMANO_MAXIMO) {
            throw new SolicitudInvalidaException("La fotografía supera el máximo de 5 MB.");
        }
        String tipo = archivo.getContentType();
        if (!"image/jpeg".equals(tipo) && !"image/png".equals(tipo)) {
            throw new SolicitudInvalidaException("Solo se permiten fotografías JPEG o PNG.");
        }
        validarFirma(archivo, tipo);
    }

    private void validarFirma(MultipartFile archivo, String tipo) {
        byte[] encabezado = new byte[8];
        int leidos;
        try (InputStream input = archivo.getInputStream()) {
            leidos = input.read(encabezado);
        } catch (IOException exception) {
            throw new SolicitudInvalidaException("No se pudo leer la fotografía.");
        }

        boolean jpegValido = leidos >= 3
                && encabezado[0] == (byte) 0xFF
                && encabezado[1] == (byte) 0xD8
                && encabezado[2] == (byte) 0xFF;
        boolean pngValido = leidos >= 8
                && encabezado[0] == (byte) 0x89
                && encabezado[1] == (byte) 0x50
                && encabezado[2] == (byte) 0x4E
                && encabezado[3] == (byte) 0x47
                && encabezado[4] == (byte) 0x0D
                && encabezado[5] == (byte) 0x0A
                && encabezado[6] == (byte) 0x1A
                && encabezado[7] == (byte) 0x0A;

        if (("image/jpeg".equals(tipo) && !jpegValido)
                || ("image/png".equals(tipo) && !pngValido)) {
            throw new SolicitudInvalidaException("El contenido no corresponde al tipo de fotografía declarado.");
        }
    }

    private RegistroFlushing buscarFlushing(Long id) {
        return flushingRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El flushing no existe."));
    }

    private EvidenciaFotografica buscarEvidencia(Long flushingId) {
        return evidenciaRepository.findByFlushingId(flushingId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El flushing no tiene fotografía."));
    }

    private Path resolver(String nombreAlmacenado) {
        Path path = directorio.resolve(nombreAlmacenado).normalize();
        if (!path.startsWith(directorio)) {
            throw new SolicitudInvalidaException("La ruta de fotografía no es válida.");
        }
        return path;
    }

    private void verificarSincronizacionActiva() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            throw new IllegalStateException("La operación fotográfica requiere una transacción activa.");
        }
    }

    private void programarEliminacionSiRollback(final String nombreAlmacenado) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    eliminarArchivoSeguro(nombreAlmacenado);
                }
            }
        });
    }

    private void programarEliminacionTrasCommit(final String nombreAlmacenado) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eliminarArchivoSeguro(nombreAlmacenado);
            }
        });
    }

    private void eliminarArchivoSeguro(String nombreAlmacenado) {
        try {
            Files.deleteIfExists(resolver(nombreAlmacenado));
        } catch (Exception exception) {
            LOGGER.warn("No se pudo eliminar el archivo fotográfico almacenado: {}", nombreAlmacenado);
        }
    }

    private EvidenciaResponse convertir(EvidenciaFotografica evidencia) {
        return new EvidenciaResponse(
                evidencia.getId(),
                evidencia.getFlushing().getId(),
                evidencia.getNombreOriginal(),
                evidencia.getTipoContenido(),
                evidencia.getTamanoBytes(),
                evidencia.getCreadaEn());
    }
}
