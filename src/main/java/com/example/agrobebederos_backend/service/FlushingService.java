package com.example.agrobebederos_backend.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.agrobebederos_backend.dto.ActualizarFlushingRequest;
import com.example.agrobebederos_backend.dto.CrearFlushingRequest;
import com.example.agrobebederos_backend.dto.FlushingResponse;
import com.example.agrobebederos_backend.entity.Galpon;
import com.example.agrobebederos_backend.entity.Granja;
import com.example.agrobebederos_backend.entity.LineaBebedero;
import com.example.agrobebederos_backend.entity.RegistroFlushing;
import com.example.agrobebederos_backend.entity.RolUsuario;
import com.example.agrobebederos_backend.entity.Usuario;
import com.example.agrobebederos_backend.exception.ConflictoException;
import com.example.agrobebederos_backend.exception.RecursoNoEncontradoException;
import com.example.agrobebederos_backend.exception.SolicitudInvalidaException;
import com.example.agrobebederos_backend.repository.LineaBebederoRepository;
import com.example.agrobebederos_backend.repository.RegistroFlushingRepository;

@Service
public class FlushingService {

    private final RegistroFlushingRepository flushingRepository;
    private final LineaBebederoRepository lineaRepository;
    private final UsuarioActualService usuarioActualService;
    private final AccesoService accesoService;
    private final TemperaturaEfectivaService temperaturaEfectivaService;
    private final AlertaService alertaService;
    private final EvidenciaFotograficaService evidenciaService;
    private final Clock clock;

    public FlushingService(
            RegistroFlushingRepository flushingRepository,
            LineaBebederoRepository lineaRepository,
            UsuarioActualService usuarioActualService,
            AccesoService accesoService,
            TemperaturaEfectivaService temperaturaEfectivaService,
            AlertaService alertaService,
            EvidenciaFotograficaService evidenciaService,
            Clock clock) {
        this.flushingRepository = flushingRepository;
        this.lineaRepository = lineaRepository;
        this.usuarioActualService = usuarioActualService;
        this.accesoService = accesoService;
        this.temperaturaEfectivaService = temperaturaEfectivaService;
        this.alertaService = alertaService;
        this.evidenciaService = evidenciaService;
        this.clock = clock;
    }

    @Transactional
    public FlushingResponse crear(Long lineaId, CrearFlushingRequest request) {
        Usuario usuario = usuarioActualService.obtener();
        accesoService.exigirOperario(usuario);
        LineaBebedero linea = buscarLinea(lineaId);
        accesoService.verificarAccesoLinea(usuario, linea);
        BigDecimal temperaturaAntes = temperaturaEfectivaService
                .validarYNormalizar(request.getTemperaturaAntesC());
        BigDecimal temperaturaDespues = temperaturaEfectivaService
                .validarYNormalizar(request.getTemperaturaDespuesC());
        validarOperationId(request.getClientOperationId());

        Optional<RegistroFlushing> existente = flushingRepository
                .findByClientOperationId(request.getClientOperationId());
        if (existente.isPresent()) {
            RegistroFlushing registro = existente.get();
            if (!registro.getOperario().getId().equals(usuario.getId())
                    || !esMismaOperacion(registro, linea, request)) {
                throw new ConflictoException("El identificador de operación ya fue utilizado.");
            }
            return convertir(registro);
        }

        RegistroFlushing flushing = new RegistroFlushing(
                linea,
                usuario,
                OffsetDateTime.now(clock),
                request.getMotivo().trim(),
                limpiarObservacion(request.getObservacion()),
                temperaturaAntes,
                temperaturaDespues,
                request.getClientOperationId());
        flushing = flushingRepository.save(flushing);
        alertaService.sincronizar(linea);
        return convertir(flushing);
    }

    @Transactional(readOnly = true)
    public List<FlushingResponse> listar() {
        Usuario usuario = usuarioActualService.obtener();
        List<RegistroFlushing> registros;
        if (usuario.getRol() == RolUsuario.SUPERVISOR) {
            registros = flushingRepository.findAllByOrderByRegistradoEnDescIdDesc();
        } else {
            if (usuario.getGranja() == null) {
                return new ArrayList<FlushingResponse>();
            }
            registros = flushingRepository.findByLineaGalponGranjaIdOrderByRegistradoEnDescIdDesc(
                    usuario.getGranja().getId());
        }
        return convertirLista(registros);
    }

    @Transactional(readOnly = true)
    public List<FlushingResponse> listarPorLinea(Long lineaId) {
        Usuario usuario = usuarioActualService.obtener();
        LineaBebedero linea = buscarLinea(lineaId);
        accesoService.verificarAccesoLinea(usuario, linea);
        return convertirLista(flushingRepository.findByLineaIdOrderByRegistradoEnDescIdDesc(lineaId));
    }

    @Transactional(readOnly = true)
    public FlushingResponse obtener(Long id) {
        Usuario usuario = usuarioActualService.obtener();
        RegistroFlushing flushing = buscarFlushing(id);
        accesoService.verificarAccesoLinea(usuario, flushing.getLinea());
        return convertir(flushing);
    }

    @Transactional
    public FlushingResponse actualizar(Long id, ActualizarFlushingRequest request) {
        Usuario usuario = usuarioActualService.obtener();
        RegistroFlushing flushing = buscarFlushing(id);
        accesoService.verificarPropietario(usuario, flushing);
        if (!flushing.getVersion().equals(request.getVersion())) {
            throw new ConflictoException("El flushing fue modificado por otra operación.");
        }
        BigDecimal temperaturaAntes = temperaturaEfectivaService
                .validarYNormalizar(request.getTemperaturaAntesC());
        BigDecimal temperaturaDespues = temperaturaEfectivaService
                .validarYNormalizar(request.getTemperaturaDespuesC());

        flushing.setMotivo(request.getMotivo().trim());
        flushing.setObservacion(limpiarObservacion(request.getObservacion()));
        flushing.setTemperaturaAntesC(temperaturaAntes);
        flushing.setTemperaturaDespuesC(temperaturaDespues);
        flushing = flushingRepository.saveAndFlush(flushing);
        alertaService.sincronizar(flushing.getLinea());
        return convertir(flushing);
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = usuarioActualService.obtener();
        RegistroFlushing flushing = buscarFlushing(id);
        accesoService.verificarPropietario(usuario, flushing);
        LineaBebedero linea = flushing.getLinea();
        evidenciaService.eliminarInterno(flushing);
        flushingRepository.delete(flushing);
        flushingRepository.flush();
        alertaService.sincronizar(linea);
    }

    private void validarOperationId(String operationId) {
        try {
            UUID.fromString(operationId);
        } catch (IllegalArgumentException exception) {
            throw new SolicitudInvalidaException("El identificador de operación debe ser un UUID válido.");
        }
    }

    private boolean esMismaOperacion(
            RegistroFlushing registro,
            LineaBebedero linea,
            CrearFlushingRequest request) {
        if (!registro.getLinea().getId().equals(linea.getId())) {
            return false;
        }
        if (!registro.getMotivo().equals(request.getMotivo().trim())) {
            return false;
        }
        String observacion = limpiarObservacion(request.getObservacion());
        if (registro.getObservacion() == null && observacion != null) {
            return false;
        }
        if (registro.getObservacion() != null && !registro.getObservacion().equals(observacion)) {
            return false;
        }
        if (registro.getTemperaturaAntesC().compareTo(request.getTemperaturaAntesC()) != 0) {
            return false;
        }
        return registro.getTemperaturaDespuesC().compareTo(request.getTemperaturaDespuesC()) == 0;
    }

    private String limpiarObservacion(String observacion) {
        if (observacion == null) {
            return null;
        }
        return observacion.trim();
    }

    private LineaBebedero buscarLinea(Long id) {
        return lineaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La línea de bebedero no existe."));
    }

    private RegistroFlushing buscarFlushing(Long id) {
        return flushingRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("El flushing no existe."));
    }

    private List<FlushingResponse> convertirLista(List<RegistroFlushing> registros) {
        List<FlushingResponse> response = new ArrayList<FlushingResponse>();
        for (int i = 0; i < registros.size(); i++) {
            response.add(convertir(registros.get(i)));
        }
        return response;
    }

    private FlushingResponse convertir(RegistroFlushing flushing) {
        LineaBebedero linea = flushing.getLinea();
        Galpon galpon = linea.getGalpon();
        Granja granja = galpon.getGranja();
        Usuario operario = flushing.getOperario();
        return new FlushingResponse(
                flushing.getId(),
                linea.getId(),
                linea.getNombre(),
                galpon.getId(),
                galpon.getNombre(),
                granja.getId(),
                granja.getNombre(),
                operario.getId(),
                operario.getNombre(),
                flushing.getRegistradoEn(),
                flushing.getMotivo(),
                flushing.getObservacion(),
                flushing.isCompletado(),
                flushing.getTemperaturaAntesC(),
                flushing.getTemperaturaDespuesC(),
                flushing.getClientOperationId(),
                flushing.getVersion());
    }
}
