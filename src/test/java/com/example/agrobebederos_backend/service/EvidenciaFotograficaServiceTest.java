package com.example.agrobebederos_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.mock.web.MockMultipartFile;
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

class EvidenciaFotograficaServiceTest {

    private static final byte[] PNG_VALIDO = new byte[] {
        (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 1
    };

    @TempDir
    Path directorio;

    private EvidenciaFotograficaRepository evidenciaRepository;
    private RegistroFlushingRepository flushingRepository;
    private UsuarioActualService usuarioActualService;
    private AccesoService accesoService;
    private RegistroFlushing flushing;
    private EvidenciaFotograficaService service;

    @BeforeEach
    void setUp() {
        evidenciaRepository = mock(EvidenciaFotograficaRepository.class);
        flushingRepository = mock(RegistroFlushingRepository.class);
        usuarioActualService = mock(UsuarioActualService.class);
        accesoService = mock(AccesoService.class);
        flushing = mock(RegistroFlushing.class);
        Usuario usuario = mock(Usuario.class);

        when(flushing.getId()).thenReturn(1L);
        when(flushingRepository.findById(1L)).thenReturn(Optional.of(flushing));
        when(usuarioActualService.obtener()).thenReturn(usuario);
        when(evidenciaRepository.findByFlushingId(1L)).thenReturn(Optional.empty());
        when(evidenciaRepository.save(any(EvidenciaFotografica.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Clock clock = Clock.fixed(Instant.parse("2026-11-06T20:00:00Z"), ZoneOffset.UTC);
        service = new EvidenciaFotograficaService(
                evidenciaRepository,
                flushingRepository,
                usuarioActualService,
                accesoService,
                clock,
                directorio.toString());
    }

    @AfterEach
    void limpiarSincronizacion() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    void rechazaArchivosNulosVaciosGrandesOTipoInvalido() {
        assertThrows(SolicitudInvalidaException.class, () -> service.guardar(1L, null));

        MockMultipartFile vacio = new MockMultipartFile(
                "archivo", "vacia.png", "image/png", new byte[0]);
        assertThrows(SolicitudInvalidaException.class, () -> service.guardar(1L, vacio));

        MockMultipartFile grande = new MockMultipartFile(
                "archivo", "grande.png", "image/png", new byte[5 * 1024 * 1024 + 1]);
        assertThrows(SolicitudInvalidaException.class, () -> service.guardar(1L, grande));

        MockMultipartFile texto = new MockMultipartFile(
                "archivo", "nota.txt", "text/plain", new byte[] { 1 });
        assertThrows(SolicitudInvalidaException.class, () -> service.guardar(1L, texto));

        MockMultipartFile firmaFalsa = new MockMultipartFile(
                "archivo", "falsa.png", "image/png", new byte[] { 1, 2, 3, 4 });
        assertThrows(SolicitudInvalidaException.class, () -> service.guardar(1L, firmaFalsa));

        MockMultipartFile jpegFalso = new MockMultipartFile(
                "archivo", "falsa.jpg", "image/jpeg", new byte[] { 1, 2, 3, 4 });
        assertThrows(SolicitudInvalidaException.class, () -> service.guardar(1L, jpegFalso));
    }

    @Test
    void exigeTransaccionAntesDeEscribirElArchivo() throws IOException {
        assertThrows(IllegalStateException.class,
                () -> service.guardar(1L, archivoPng("sin-transaccion.png")));
        assertEquals(0, contarArchivos());
    }

    @Test
    void rechazaUnaFotografiaQueNoPuedeLeerse() throws IOException {
        MultipartFile archivo = mock(MultipartFile.class);
        when(archivo.isEmpty()).thenReturn(false);
        when(archivo.getSize()).thenReturn(10L);
        when(archivo.getContentType()).thenReturn("image/png");
        when(archivo.getInputStream()).thenThrow(new IOException("fallo simulado"));

        assertThrows(SolicitudInvalidaException.class, () -> service.guardar(1L, archivo));
    }

    @Test
    void eliminaElArchivoNuevoCuandoLaPersistenciaRevierte() throws IOException {
        iniciarSincronizacion();
        when(evidenciaRepository.save(any(EvidenciaFotografica.class)))
                .thenThrow(new DataIntegrityViolationException("fallo simulado"));
        MockMultipartFile archivo = archivoPng("nueva.png");

        assertThrows(DataIntegrityViolationException.class, () -> service.guardar(1L, archivo));
        assertEquals(1, contarArchivos());

        completarRollback();
        assertEquals(0, contarArchivos());
    }

    @Test
    void conservaLaFotoAnteriorHastaConfirmarElReemplazo() throws IOException {
        Path anteriorPath = directorio.resolve("anterior.png");
        Files.write(anteriorPath, PNG_VALIDO);
        EvidenciaFotografica anterior = crearEvidencia("anterior.png", "anterior.png");
        when(evidenciaRepository.findByFlushingId(1L)).thenReturn(Optional.of(anterior));
        iniciarSincronizacion();

        EvidenciaResponse response = service.guardar(1L, archivoPng("nueva.png"));

        assertEquals("nueva.png", response.getNombreOriginal());
        assertTrue(Files.exists(anteriorPath));
        assertEquals(2, contarArchivos());

        completarCommit();
        assertFalse(Files.exists(anteriorPath));
        assertEquals(1, contarArchivos());
    }

    @Test
    void conservaLaFotoAnteriorCuandoLaEliminacionRevierte() throws IOException {
        Path anteriorPath = directorio.resolve("anterior.png");
        Files.write(anteriorPath, PNG_VALIDO);
        EvidenciaFotografica anterior = crearEvidencia("anterior.png", "anterior.png");
        when(evidenciaRepository.findByFlushingId(1L)).thenReturn(Optional.of(anterior));
        iniciarSincronizacion();

        service.eliminarInterno(flushing);
        completarRollback();

        assertTrue(Files.exists(anteriorPath));
    }

    @Test
    void usaNombrePredeterminadoCuandoElOriginalEstaVacio() throws IOException {
        iniciarSincronizacion();
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", "", "image/png", PNG_VALIDO);

        EvidenciaResponse response = service.guardar(1L, archivo);
        completarCommit();

        assertEquals("fotografia.png", response.getNombreOriginal());
        assertEquals(1, contarArchivos());
    }

    @Test
    void usaNombrePredeterminadoCuandoElOriginalEsNull() throws IOException {
        iniciarSincronizacion();
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo", null, "image/png", PNG_VALIDO);

        EvidenciaResponse response = service.guardar(1L, archivo);
        completarCommit();

        assertEquals("fotografia.png", response.getNombreOriginal());
    }

    @Test
    void rechazaDescargaCuandoElArchivoFisicoNoExiste() {
        EvidenciaFotografica evidencia = crearEvidencia("ausente.png", "foto.png");
        when(evidenciaRepository.findByFlushingId(1L)).thenReturn(Optional.of(evidencia));

        assertThrows(RecursoNoEncontradoException.class, () -> service.descargar(1L));
    }

    @Test
    void rechazaUnaRutaAlmacenadaFueraDelDirectorio() {
        EvidenciaFotografica evidencia = crearEvidencia("../fuera.png", "foto.png");
        when(evidenciaRepository.findByFlushingId(1L)).thenReturn(Optional.of(evidencia));

        assertThrows(SolicitudInvalidaException.class, () -> service.descargar(1L));
    }

    private MockMultipartFile archivoPng(String nombre) {
        return new MockMultipartFile("archivo", nombre, "image/png", PNG_VALIDO);
    }

    private EvidenciaFotografica crearEvidencia(String almacenado, String original) {
        return new EvidenciaFotografica(
                flushing,
                almacenado,
                original,
                "image/png",
                Long.valueOf(PNG_VALIDO.length),
                OffsetDateTime.parse("2026-11-06T20:00:00Z"));
    }

    private int contarArchivos() throws IOException {
        int cantidad = 0;
        try (DirectoryStream<Path> archivos = Files.newDirectoryStream(directorio)) {
            for (Path archivo : archivos) {
                if (Files.isRegularFile(archivo)) {
                    cantidad++;
                }
            }
        }
        return cantidad;
    }

    private void iniciarSincronizacion() {
        TransactionSynchronizationManager.initSynchronization();
    }

    private void completarCommit() {
        List<TransactionSynchronization> sincronizaciones = copiarSincronizaciones();
        for (int i = 0; i < sincronizaciones.size(); i++) {
            sincronizaciones.get(i).afterCommit();
        }
        for (int i = 0; i < sincronizaciones.size(); i++) {
            sincronizaciones.get(i).afterCompletion(TransactionSynchronization.STATUS_COMMITTED);
        }
        TransactionSynchronizationManager.clearSynchronization();
    }

    private void completarRollback() {
        List<TransactionSynchronization> sincronizaciones = copiarSincronizaciones();
        for (int i = 0; i < sincronizaciones.size(); i++) {
            sincronizaciones.get(i).afterCompletion(TransactionSynchronization.STATUS_ROLLED_BACK);
        }
        TransactionSynchronizationManager.clearSynchronization();
    }

    private List<TransactionSynchronization> copiarSincronizaciones() {
        return new ArrayList<TransactionSynchronization>(
                TransactionSynchronizationManager.getSynchronizations());
    }
}
