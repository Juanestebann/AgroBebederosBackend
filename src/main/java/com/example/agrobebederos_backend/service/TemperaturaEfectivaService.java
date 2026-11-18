package com.example.agrobebederos_backend.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.agrobebederos_backend.dto.TemperaturaEfectivaResponse;
import com.example.agrobebederos_backend.entity.EstadoTermico;
import com.example.agrobebederos_backend.entity.LecturaTemperatura;
import com.example.agrobebederos_backend.entity.LineaBebedero;
import com.example.agrobebederos_backend.entity.RegistroFlushing;
import com.example.agrobebederos_backend.exception.SolicitudInvalidaException;
import com.example.agrobebederos_backend.repository.LecturaTemperaturaRepository;
import com.example.agrobebederos_backend.repository.RegistroFlushingRepository;

@Service
public class TemperaturaEfectivaService {

    public static final BigDecimal TEMPERATURA_ADVERTENCIA = new BigDecimal("23.0");
    public static final BigDecimal TEMPERATURA_CRITICA = new BigDecimal("27.0");
    public static final BigDecimal TEMPERATURA_MINIMA = new BigDecimal("0.0");
    public static final BigDecimal TEMPERATURA_MAXIMA = new BigDecimal("50.0");

    private final LecturaTemperaturaRepository lecturaRepository;
    private final RegistroFlushingRepository flushingRepository;

    public TemperaturaEfectivaService(
            LecturaTemperaturaRepository lecturaRepository,
            RegistroFlushingRepository flushingRepository) {
        this.lecturaRepository = lecturaRepository;
        this.flushingRepository = flushingRepository;
    }

    public EstadoTermico clasificar(BigDecimal temperatura) {
        BigDecimal temperaturaNormalizada = validarYNormalizar(temperatura);
        if (temperaturaNormalizada.compareTo(TEMPERATURA_CRITICA) >= 0) {
            return EstadoTermico.CRITICAL;
        }
        if (temperaturaNormalizada.compareTo(TEMPERATURA_ADVERTENCIA) >= 0) {
            return EstadoTermico.WARNING;
        }
        return EstadoTermico.NORMAL;
    }

    public void validarRango(BigDecimal temperatura) {
        validarYNormalizar(temperatura);
    }

    public BigDecimal validarYNormalizar(BigDecimal temperatura) {
        if (temperatura == null
                || temperatura.compareTo(TEMPERATURA_MINIMA) < 0
                || temperatura.compareTo(TEMPERATURA_MAXIMA) > 0) {
            throw new SolicitudInvalidaException("La temperatura debe estar entre 0 y 50 °C.");
        }
        try {
            return temperatura.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException exception) {
            throw new SolicitudInvalidaException("La temperatura puede tener como máximo dos decimales.");
        }
    }

    @Transactional(readOnly = true)
    public TemperaturaEfectivaResponse calcular(LineaBebedero linea) {
        Optional<RegistroFlushing> flushing = flushingRepository
                .findFirstByLineaIdAndCompletadoTrueOrderByRegistradoEnDescIdDesc(linea.getId());
        if (flushing.isPresent()) {
            RegistroFlushing registro = flushing.get();
            BigDecimal valor = registro.getTemperaturaDespuesC();
            return crearRespuesta(valor, "FLUSHING", registro.getRegistradoEn());
        }

        Optional<LecturaTemperatura> lectura = lecturaRepository
                .findFirstByLineaIdOrderByRegistradaEnDescIdDesc(linea.getId());
        if (lectura.isPresent()) {
            LecturaTemperatura registro = lectura.get();
            return crearRespuesta(registro.getValorCelsius(), "LECTURA", registro.getRegistradaEn());
        }

        return null;
    }

    private TemperaturaEfectivaResponse crearRespuesta(
            BigDecimal valor,
            String origen,
            OffsetDateTime registradaEn) {
        return new TemperaturaEfectivaResponse(
                valor,
                clasificar(valor).name(),
                origen,
                registradaEn);
    }
}
