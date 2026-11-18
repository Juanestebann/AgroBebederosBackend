package com.example.agrobebederos_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.agrobebederos_backend.dto.TemperaturaEfectivaResponse;
import com.example.agrobebederos_backend.entity.EstadoTermico;
import com.example.agrobebederos_backend.entity.LecturaTemperatura;
import com.example.agrobebederos_backend.entity.LineaBebedero;
import com.example.agrobebederos_backend.entity.RegistroFlushing;
import com.example.agrobebederos_backend.exception.SolicitudInvalidaException;
import com.example.agrobebederos_backend.repository.LecturaTemperaturaRepository;
import com.example.agrobebederos_backend.repository.RegistroFlushingRepository;

class TemperaturaEfectivaServiceTest {

    private LecturaTemperaturaRepository lecturaRepository;
    private RegistroFlushingRepository flushingRepository;
    private TemperaturaEfectivaService service;

    @BeforeEach
    void setUp() {
        lecturaRepository = mock(LecturaTemperaturaRepository.class);
        flushingRepository = mock(RegistroFlushingRepository.class);
        service = new TemperaturaEfectivaService(lecturaRepository, flushingRepository);
    }

    @Test
    void clasificaLosLimitesTermicos() {
        assertEquals(EstadoTermico.NORMAL, service.clasificar(new BigDecimal("22.99")));
        assertEquals(EstadoTermico.WARNING, service.clasificar(new BigDecimal("23.00")));
        assertEquals(EstadoTermico.WARNING, service.clasificar(new BigDecimal("26.99")));
        assertEquals(EstadoTermico.CRITICAL, service.clasificar(new BigDecimal("27.00")));
    }

    @Test
    void rechazaTemperaturasFueraDelRango() {
        assertThrows(SolicitudInvalidaException.class,
                () -> service.clasificar(new BigDecimal("-0.01")));
        assertThrows(SolicitudInvalidaException.class,
                () -> service.clasificar(new BigDecimal("50.01")));
        assertThrows(SolicitudInvalidaException.class,
                () -> service.clasificar(null));
    }

    @Test
    void rechazaValoresQueNecesitanRedondeo() {
        assertThrows(SolicitudInvalidaException.class,
                () -> service.validarYNormalizar(new BigDecimal("22.999")));
    }

    @Test
    void normalizaSinRedondearLosValoresValidos() {
        assertEquals(new BigDecimal("22.99"),
                service.validarYNormalizar(new BigDecimal("22.99")));
        assertEquals(new BigDecimal("22.99"),
                service.validarYNormalizar(new BigDecimal("22.990")));
        assertEquals(new BigDecimal("23.00"),
                service.validarYNormalizar(new BigDecimal("23")));
        assertEquals(new BigDecimal("26.99"),
                service.validarYNormalizar(new BigDecimal("26.99")));
        assertEquals(new BigDecimal("27.00"),
                service.validarYNormalizar(new BigDecimal("27")));
    }

    @Test
    void usaLaTemperaturaPosteriorDelFlushingMasReciente() {
        LineaBebedero linea = mock(LineaBebedero.class);
        when(linea.getId()).thenReturn(10L);
        OffsetDateTime fecha = OffsetDateTime.parse("2026-11-06T10:00:00-03:00");
        RegistroFlushing flushing = new RegistroFlushing(
                linea, null, fecha, "Limpieza", null,
                new BigDecimal("28.0"), new BigDecimal("21.5"),
                "e9a55eb1-4f9a-46c8-b589-93af06104746");
        when(flushingRepository.findFirstByLineaIdAndCompletadoTrueOrderByRegistradoEnDescIdDesc(10L))
                .thenReturn(Optional.of(flushing));

        TemperaturaEfectivaResponse response = service.calcular(linea);

        assertEquals(new BigDecimal("21.5"), response.getValorCelsius());
        assertEquals("FLUSHING", response.getOrigen());
        assertEquals("NORMAL", response.getEstado());
        assertEquals(fecha, response.getRegistradaEn());
    }

    @Test
    void usaLaUltimaLecturaCuandoNoHayFlushing() {
        LineaBebedero linea = mock(LineaBebedero.class);
        when(linea.getId()).thenReturn(11L);
        OffsetDateTime fecha = OffsetDateTime.parse("2026-11-06T11:00:00-03:00");
        LecturaTemperatura lectura = new LecturaTemperatura(linea, new BigDecimal("24.0"), fecha);
        when(flushingRepository.findFirstByLineaIdAndCompletadoTrueOrderByRegistradoEnDescIdDesc(11L))
                .thenReturn(Optional.empty());
        when(lecturaRepository.findFirstByLineaIdOrderByRegistradaEnDescIdDesc(11L))
                .thenReturn(Optional.of(lectura));

        TemperaturaEfectivaResponse response = service.calcular(linea);

        assertEquals(new BigDecimal("24.0"), response.getValorCelsius());
        assertEquals("LECTURA", response.getOrigen());
        assertEquals("WARNING", response.getEstado());
    }

    @Test
    void retornaNullCuandoNoHayTemperatura() {
        LineaBebedero linea = mock(LineaBebedero.class);
        when(linea.getId()).thenReturn(12L);
        when(flushingRepository.findFirstByLineaIdAndCompletadoTrueOrderByRegistradoEnDescIdDesc(12L))
                .thenReturn(Optional.empty());
        when(lecturaRepository.findFirstByLineaIdOrderByRegistradaEnDescIdDesc(12L))
                .thenReturn(Optional.empty());

        assertNull(service.calcular(linea));
    }
}
