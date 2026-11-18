package com.example.agrobebederos_backend.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.agrobebederos_backend.dto.LecturaTemperaturaResponse;
import com.example.agrobebederos_backend.entity.LecturaTemperatura;
import com.example.agrobebederos_backend.entity.LineaBebedero;
import com.example.agrobebederos_backend.entity.Usuario;
import com.example.agrobebederos_backend.exception.RecursoNoEncontradoException;
import com.example.agrobebederos_backend.repository.LecturaTemperaturaRepository;
import com.example.agrobebederos_backend.repository.LineaBebederoRepository;

@Service
public class TemperaturaService {

    private final LineaBebederoRepository lineaRepository;
    private final LecturaTemperaturaRepository lecturaRepository;
    private final UsuarioActualService usuarioActualService;
    private final AccesoService accesoService;
    private final TemperaturaEfectivaService temperaturaEfectivaService;
    private final AlertaService alertaService;
    private final Clock clock;

    public TemperaturaService(
            LineaBebederoRepository lineaRepository,
            LecturaTemperaturaRepository lecturaRepository,
            UsuarioActualService usuarioActualService,
            AccesoService accesoService,
            TemperaturaEfectivaService temperaturaEfectivaService,
            AlertaService alertaService,
            Clock clock) {
        this.lineaRepository = lineaRepository;
        this.lecturaRepository = lecturaRepository;
        this.usuarioActualService = usuarioActualService;
        this.accesoService = accesoService;
        this.temperaturaEfectivaService = temperaturaEfectivaService;
        this.alertaService = alertaService;
        this.clock = clock;
    }

    @Transactional
    public LecturaTemperaturaResponse registrar(Long lineaId, BigDecimal valorCelsius) {
        Usuario usuario = usuarioActualService.obtener();
        accesoService.exigirOperario(usuario);
        LineaBebedero linea = buscarLinea(lineaId);
        accesoService.verificarAccesoLinea(usuario, linea);
        BigDecimal valorNormalizado = temperaturaEfectivaService.validarYNormalizar(valorCelsius);

        LecturaTemperatura lectura = new LecturaTemperatura(
                linea,
                valorNormalizado,
                OffsetDateTime.now(clock));
        lectura = lecturaRepository.save(lectura);
        alertaService.sincronizar(linea);
        return convertir(lectura);
    }

    @Transactional(readOnly = true)
    public List<LecturaTemperaturaResponse> listarHistorial(Long lineaId) {
        Usuario usuario = usuarioActualService.obtener();
        LineaBebedero linea = buscarLinea(lineaId);
        accesoService.verificarAccesoLinea(usuario, linea);

        List<LecturaTemperatura> lecturas = lecturaRepository
                .findByLineaIdOrderByRegistradaEnDescIdDesc(lineaId);
        List<LecturaTemperaturaResponse> response = new ArrayList<LecturaTemperaturaResponse>();
        for (int i = 0; i < lecturas.size(); i++) {
            response.add(convertir(lecturas.get(i)));
        }
        return response;
    }

    private LineaBebedero buscarLinea(Long lineaId) {
        return lineaRepository.findById(lineaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La línea de bebedero no existe."));
    }

    private LecturaTemperaturaResponse convertir(LecturaTemperatura lectura) {
        return new LecturaTemperaturaResponse(
                lectura.getId(),
                lectura.getLinea().getId(),
                lectura.getValorCelsius(),
                temperaturaEfectivaService.clasificar(lectura.getValorCelsius()).name(),
                lectura.getRegistradaEn());
    }
}
