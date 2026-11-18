package com.example.agrobebederos_backend.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.agrobebederos_backend.dto.AlertaResponse;
import com.example.agrobebederos_backend.dto.TemperaturaEfectivaResponse;
import com.example.agrobebederos_backend.entity.Alerta;
import com.example.agrobebederos_backend.entity.EstadoTermico;
import com.example.agrobebederos_backend.entity.Galpon;
import com.example.agrobebederos_backend.entity.Granja;
import com.example.agrobebederos_backend.entity.LineaBebedero;
import com.example.agrobebederos_backend.entity.RolUsuario;
import com.example.agrobebederos_backend.entity.Usuario;
import com.example.agrobebederos_backend.exception.RecursoNoEncontradoException;
import com.example.agrobebederos_backend.repository.AlertaRepository;

@Service
public class AlertaService {

    private final AlertaRepository alertaRepository;
    private final TemperaturaEfectivaService temperaturaEfectivaService;
    private final UsuarioActualService usuarioActualService;
    private final AccesoService accesoService;

    public AlertaService(
            AlertaRepository alertaRepository,
            TemperaturaEfectivaService temperaturaEfectivaService,
            UsuarioActualService usuarioActualService,
            AccesoService accesoService) {
        this.alertaRepository = alertaRepository;
        this.temperaturaEfectivaService = temperaturaEfectivaService;
        this.usuarioActualService = usuarioActualService;
        this.accesoService = accesoService;
    }

    @Transactional
    public void sincronizar(LineaBebedero linea) {
        TemperaturaEfectivaResponse efectiva = temperaturaEfectivaService.calcular(linea);
        Optional<Alerta> existente = alertaRepository.findByLineaId(linea.getId());

        if (efectiva == null || EstadoTermico.NORMAL.name().equals(efectiva.getEstado())) {
            if (existente.isPresent()) {
                alertaRepository.delete(existente.get());
            }
            return;
        }

        Alerta alerta;
        if (existente.isPresent()) {
            alerta = existente.get();
            alerta.setTemperaturaC(efectiva.getValorCelsius());
            alerta.setGeneradaEn(efectiva.getRegistradaEn());
        } else {
            alerta = new Alerta(linea, efectiva.getValorCelsius(), efectiva.getRegistradaEn());
        }
        alertaRepository.save(alerta);
    }

    @Transactional(readOnly = true)
    public List<AlertaResponse> listar() {
        Usuario usuario = usuarioActualService.obtener();
        List<Alerta> alertas;
        if (usuario.getRol() == RolUsuario.SUPERVISOR) {
            alertas = alertaRepository.findAllByOrderByGeneradaEnDescIdDesc();
        } else {
            if (usuario.getGranja() == null) {
                return new ArrayList<AlertaResponse>();
            }
            alertas = alertaRepository.findByLineaGalponGranjaIdOrderByGeneradaEnDescIdDesc(
                    usuario.getGranja().getId());
        }

        List<AlertaResponse> response = new ArrayList<AlertaResponse>();
        for (int i = 0; i < alertas.size(); i++) {
            response.add(convertir(alertas.get(i)));
        }
        return response;
    }

    @Transactional(readOnly = true)
    public AlertaResponse obtener(Long id) {
        Usuario usuario = usuarioActualService.obtener();
        Alerta alerta = alertaRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("La alerta no existe."));
        accesoService.verificarAccesoLinea(usuario, alerta.getLinea());
        return convertir(alerta);
    }

    private AlertaResponse convertir(Alerta alerta) {
        LineaBebedero linea = alerta.getLinea();
        Galpon galpon = linea.getGalpon();
        Granja granja = galpon.getGranja();
        return new AlertaResponse(
                alerta.getId(),
                linea.getId(),
                linea.getNombre(),
                galpon.getId(),
                galpon.getNombre(),
                granja.getId(),
                granja.getNombre(),
                alerta.getTemperaturaC(),
                temperaturaEfectivaService.clasificar(alerta.getTemperaturaC()).name(),
                alerta.getGeneradaEn());
    }
}
