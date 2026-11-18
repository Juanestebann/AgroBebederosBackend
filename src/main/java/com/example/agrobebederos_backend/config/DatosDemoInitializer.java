package com.example.agrobebederos_backend.config;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.agrobebederos_backend.entity.Galpon;
import com.example.agrobebederos_backend.entity.Granja;
import com.example.agrobebederos_backend.entity.LecturaTemperatura;
import com.example.agrobebederos_backend.entity.LineaBebedero;
import com.example.agrobebederos_backend.entity.RolUsuario;
import com.example.agrobebederos_backend.entity.Usuario;
import com.example.agrobebederos_backend.repository.GalponRepository;
import com.example.agrobebederos_backend.repository.GranjaRepository;
import com.example.agrobebederos_backend.repository.LecturaTemperaturaRepository;
import com.example.agrobebederos_backend.repository.LineaBebederoRepository;
import com.example.agrobebederos_backend.repository.UsuarioRepository;
import com.example.agrobebederos_backend.service.AlertaService;

@Component
@ConditionalOnProperty(name = "app.demo.enabled", havingValue = "true")
public class DatosDemoInitializer implements CommandLineRunner {

    private final GranjaRepository granjaRepository;
    private final GalponRepository galponRepository;
    private final LineaBebederoRepository lineaRepository;
    private final LecturaTemperaturaRepository lecturaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AlertaService alertaService;
    private final String operarioPassword;
    private final String supervisorPassword;

    public DatosDemoInitializer(
            GranjaRepository granjaRepository,
            GalponRepository galponRepository,
            LineaBebederoRepository lineaRepository,
            LecturaTemperaturaRepository lecturaRepository,
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            AlertaService alertaService,
            @Value("${DEMO_OPERARIO_PASSWORD:}") String operarioPassword,
            @Value("${DEMO_SUPERVISOR_PASSWORD:}") String supervisorPassword) {
        this.granjaRepository = granjaRepository;
        this.galponRepository = galponRepository;
        this.lineaRepository = lineaRepository;
        this.lecturaRepository = lecturaRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.alertaService = alertaService;
        this.operarioPassword = operarioPassword;
        this.supervisorPassword = supervisorPassword;
    }

    @Override
    public void run(String... args) {
        if (granjaRepository.count() == 0) {
            crearCatalogoDemo();
        }
        crearUsuariosDemoSiCorresponde();
    }

    private void crearCatalogoDemo() {
        Granja sauce = granjaRepository.save(new Granja("Granja El Sauce"));
        Granja maitenes = granjaRepository.save(new Granja("Granja Los Maitenes"));
        Granja santaRosa = granjaRepository.save(new Granja("Granja Santa Rosa"));

        List<LineaBebedero> lineas = new ArrayList<LineaBebedero>();
        lineas.add(crearLinea(sauce, "Galpón A", "Línea 1", "19.8", 14, 32));
        lineas.add(crearLinea(sauce, "Galpón A", "Línea 2", "24.3", 14, 30));
        lineas.add(crearLinea(sauce, "Galpón B", "Línea 3", "28.9", 14, 28));
        lineas.add(crearLinea(sauce, "Galpón B", "Línea 4", "20.5", 14, 31));
        lineas.add(crearLinea(sauce, "Galpón C", "Línea 5", "23.1", 14, 29));

        lineas.add(crearLinea(maitenes, "Galpón A", "Línea 1", "21.2", 14, 25));
        lineas.add(crearLinea(maitenes, "Galpón A", "Línea 2", "22.4", 14, 22));
        lineas.add(crearLinea(maitenes, "Galpón B", "Línea 3", "20.8", 14, 19));
        lineas.add(crearLinea(maitenes, "Galpón B", "Línea 4", "24.1", 14, 18));
        lineas.add(crearLinea(maitenes, "Galpón C", "Línea 5", "21.0", 14, 17));
        lineas.add(crearLinea(maitenes, "Galpón C", "Línea 6", "20.2", 14, 16));
        lineas.add(crearLinea(maitenes, "Galpón D", "Línea 7", "21.4", 14, 15));

        lineas.add(crearLinea(santaRosa, "Galpón A", "Línea 1", "20.7", 14, 14));
        lineas.add(crearLinea(santaRosa, "Galpón B", "Línea 2", "21.1", 14, 13));

        for (int i = 0; i < lineas.size(); i++) {
            alertaService.sincronizar(lineas.get(i));
        }
    }

    private LineaBebedero crearLinea(
            Granja granja,
            String nombreGalpon,
            String nombreLinea,
            String temperatura,
            int hora,
            int minuto) {
        Galpon galpon = buscarOCrearGalpon(granja, nombreGalpon);
        LineaBebedero linea = lineaRepository.save(new LineaBebedero(nombreLinea, galpon));
        OffsetDateTime fecha = OffsetDateTime.of(
                2026, 9, 23, hora, minuto, 0, 0, ZoneOffset.of("-03:00"));
        lecturaRepository.save(new LecturaTemperatura(linea, new BigDecimal(temperatura), fecha));
        return linea;
    }

    private Galpon buscarOCrearGalpon(Granja granja, String nombre) {
        List<Galpon> existentes = galponRepository.findByGranjaIdOrderByNombreAsc(granja.getId());
        for (int i = 0; i < existentes.size(); i++) {
            if (existentes.get(i).getNombre().equals(nombre)) {
                return existentes.get(i);
            }
        }
        return galponRepository.save(new Galpon(nombre, granja));
    }

    private void crearUsuariosDemoSiCorresponde() {
        if (!operarioPassword.isBlank()
                && !usuarioRepository.existsByEmailIgnoreCase("operario@ariztia.cl")) {
            Granja sauce = buscarGranjaPorNombre("Granja El Sauce");
            Usuario operario = new Usuario(
                    "Juan Pérez",
                    "operario@ariztia.cl",
                    passwordEncoder.encode(operarioPassword),
                    RolUsuario.OPERARIO,
                    sauce);
            usuarioRepository.save(operario);
        }

        if (!supervisorPassword.isBlank()
                && !usuarioRepository.existsByEmailIgnoreCase("supervisor@ariztia.cl")) {
            Usuario supervisor = new Usuario(
                    "Carlos Morales",
                    "supervisor@ariztia.cl",
                    passwordEncoder.encode(supervisorPassword),
                    RolUsuario.SUPERVISOR,
                    null);
            usuarioRepository.save(supervisor);
        }
    }

    private Granja buscarGranjaPorNombre(String nombre) {
        List<Granja> granjas = granjaRepository.findAllByOrderByNombreAsc();
        for (int i = 0; i < granjas.size(); i++) {
            if (granjas.get(i).getNombre().equals(nombre)) {
                return granjas.get(i);
            }
        }
        throw new IllegalStateException("No existe la granja demo requerida.");
    }
}
