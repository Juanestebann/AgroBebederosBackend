package com.example.agrobebederos_backend.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.agrobebederos_backend.dto.GalponResponse;
import com.example.agrobebederos_backend.dto.GranjaResponse;
import com.example.agrobebederos_backend.dto.LineaBebederoResponse;
import com.example.agrobebederos_backend.dto.TemperaturaEfectivaResponse;
import com.example.agrobebederos_backend.entity.Galpon;
import com.example.agrobebederos_backend.entity.Granja;
import com.example.agrobebederos_backend.entity.LineaBebedero;
import com.example.agrobebederos_backend.entity.RolUsuario;
import com.example.agrobebederos_backend.entity.Usuario;
import com.example.agrobebederos_backend.exception.AccesoDenegadoException;
import com.example.agrobebederos_backend.exception.RecursoNoEncontradoException;
import com.example.agrobebederos_backend.repository.GalponRepository;
import com.example.agrobebederos_backend.repository.GranjaRepository;
import com.example.agrobebederos_backend.repository.LineaBebederoRepository;

@Service
public class CatalogoService {

    private final GranjaRepository granjaRepository;
    private final GalponRepository galponRepository;
    private final LineaBebederoRepository lineaRepository;
    private final UsuarioActualService usuarioActualService;
    private final AccesoService accesoService;
    private final TemperaturaEfectivaService temperaturaEfectivaService;

    public CatalogoService(
            GranjaRepository granjaRepository,
            GalponRepository galponRepository,
            LineaBebederoRepository lineaRepository,
            UsuarioActualService usuarioActualService,
            AccesoService accesoService,
            TemperaturaEfectivaService temperaturaEfectivaService) {
        this.granjaRepository = granjaRepository;
        this.galponRepository = galponRepository;
        this.lineaRepository = lineaRepository;
        this.usuarioActualService = usuarioActualService;
        this.accesoService = accesoService;
        this.temperaturaEfectivaService = temperaturaEfectivaService;
    }

    @Transactional(readOnly = true)
    public List<GranjaResponse> listarGranjas() {
        Usuario usuario = usuarioActualService.obtener();
        List<GranjaResponse> response = new ArrayList<GranjaResponse>();
        if (usuario.getRol() == RolUsuario.OPERARIO) {
            Granja granja = usuario.getGranja();
            if (granja == null) {
                throw new AccesoDenegadoException("El operario no tiene una granja asignada.");
            }
            response.add(new GranjaResponse(granja.getId(), granja.getNombre()));
            return response;
        }

        List<Granja> granjas = granjaRepository.findAllByOrderByNombreAsc();
        for (int i = 0; i < granjas.size(); i++) {
            Granja granja = granjas.get(i);
            response.add(new GranjaResponse(granja.getId(), granja.getNombre()));
        }
        return response;
    }

    @Transactional(readOnly = true)
    public List<GalponResponse> listarGalpones(Long granjaId) {
        Usuario usuario = usuarioActualService.obtener();
        Granja granja = granjaRepository.findById(granjaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La granja no existe."));
        accesoService.verificarAccesoGranja(usuario, granja);

        List<Galpon> galpones = galponRepository.findByGranjaIdOrderByNombreAsc(granjaId);
        List<GalponResponse> response = new ArrayList<GalponResponse>();
        for (int i = 0; i < galpones.size(); i++) {
            Galpon galpon = galpones.get(i);
            response.add(new GalponResponse(
                    galpon.getId(),
                    galpon.getNombre(),
                    granja.getId(),
                    granja.getNombre()));
        }
        return response;
    }

    @Transactional(readOnly = true)
    public List<LineaBebederoResponse> listarLineas(Long galponId) {
        Usuario usuario = usuarioActualService.obtener();
        Galpon galpon = galponRepository.findById(galponId)
                .orElseThrow(() -> new RecursoNoEncontradoException("El galpón no existe."));
        accesoService.verificarAccesoGranja(usuario, galpon.getGranja());

        List<LineaBebedero> lineas = lineaRepository.findByGalponIdOrderByNombreAsc(galponId);
        List<LineaBebederoResponse> response = new ArrayList<LineaBebederoResponse>();
        for (int i = 0; i < lineas.size(); i++) {
            response.add(convertirLinea(lineas.get(i)));
        }
        return response;
    }

    @Transactional(readOnly = true)
    public LineaBebederoResponse obtenerLinea(Long lineaId) {
        Usuario usuario = usuarioActualService.obtener();
        LineaBebedero linea = lineaRepository.findById(lineaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("La línea de bebedero no existe."));
        accesoService.verificarAccesoLinea(usuario, linea);
        return convertirLinea(linea);
    }

    private LineaBebederoResponse convertirLinea(LineaBebedero linea) {
        Galpon galpon = linea.getGalpon();
        Granja granja = galpon.getGranja();
        TemperaturaEfectivaResponse temperatura = temperaturaEfectivaService.calcular(linea);
        return new LineaBebederoResponse(
                linea.getId(),
                linea.getNombre(),
                galpon.getId(),
                galpon.getNombre(),
                granja.getId(),
                granja.getNombre(),
                temperatura);
    }
}
