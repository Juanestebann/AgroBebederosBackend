package com.example.agrobebederos_backend.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import com.example.agrobebederos_backend.entity.Galpon;
import com.example.agrobebederos_backend.entity.Granja;
import com.example.agrobebederos_backend.entity.LineaBebedero;
import com.example.agrobebederos_backend.entity.RolUsuario;
import com.example.agrobebederos_backend.entity.Usuario;
import com.example.agrobebederos_backend.entity.RegistroFlushing;
import com.example.agrobebederos_backend.repository.AlertaRepository;
import com.example.agrobebederos_backend.repository.EvidenciaFotograficaRepository;
import com.example.agrobebederos_backend.repository.GalponRepository;
import com.example.agrobebederos_backend.repository.GranjaRepository;
import com.example.agrobebederos_backend.repository.LecturaTemperaturaRepository;
import com.example.agrobebederos_backend.repository.LineaBebederoRepository;
import com.example.agrobebederos_backend.repository.RegistroFlushingRepository;
import com.example.agrobebederos_backend.repository.UsuarioRepository;

@SpringBootTest
@AutoConfigureMockMvc
class ApiIntegrationTest {

    private static final String CLAVE = "ClaveSegura123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private GranjaRepository granjaRepository;

    @Autowired
    private GalponRepository galponRepository;

    @Autowired
    private LineaBebederoRepository lineaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RegistroFlushingRepository flushingRepository;

    @Autowired
    private AlertaRepository alertaRepository;

    @Autowired
    private EvidenciaFotograficaRepository evidenciaRepository;

    @Autowired
    private LecturaTemperaturaRepository lecturaRepository;

    private Granja granjaOperario;
    private Granja otraGranja;
    private LineaBebedero lineaOperario;
    private LineaBebedero lineaSecundariaOperario;
    private LineaBebedero lineaAjena;

    @BeforeEach
    void setUp() {
        evidenciaRepository.deleteAll();
        alertaRepository.deleteAll();
        flushingRepository.deleteAll();
        lecturaRepository.deleteAll();
        usuarioRepository.deleteAll();
        lineaRepository.deleteAll();
        galponRepository.deleteAll();
        granjaRepository.deleteAll();

        granjaOperario = granjaRepository.save(new Granja("El Sauce"));
        otraGranja = granjaRepository.save(new Granja("Santa Elena"));

        Galpon galponOperario = galponRepository.save(new Galpon("Galpón 1", granjaOperario));
        Galpon galponAjeno = galponRepository.save(new Galpon("Galpón 2", otraGranja));
        lineaOperario = lineaRepository.save(new LineaBebedero("Línea A", galponOperario));
        lineaSecundariaOperario = lineaRepository.save(new LineaBebedero("Línea C", galponOperario));
        lineaAjena = lineaRepository.save(new LineaBebedero("Línea B", galponAjeno));

        Usuario operario = new Usuario(
                "Operario Uno",
                "operario@agro.test",
                passwordEncoder.encode(CLAVE),
                RolUsuario.OPERARIO,
                granjaOperario);
        usuarioRepository.save(operario);

        Usuario supervisor = new Usuario(
                "Supervisora",
                "supervisor@agro.test",
                passwordEncoder.encode(CLAVE),
                RolUsuario.SUPERVISOR,
                null);
        usuarioRepository.save(supervisor);

        Usuario otroOperario = new Usuario(
                "Operario Dos",
                "otro@agro.test",
                passwordEncoder.encode(CLAVE),
                RolUsuario.OPERARIO,
                granjaOperario);
        usuarioRepository.save(otroOperario);

        Usuario operarioSinGranja = new Usuario(
                "Operario Sin Granja",
                "sin-granja@agro.test",
                passwordEncoder.encode(CLAVE),
                RolUsuario.OPERARIO,
                null);
        usuarioRepository.save(operarioSinGranja);
    }

    @Test
    void rechazaUnaSolicitudSinCredenciales() throws Exception {
        mockMvc.perform(get("/api/v1/granjas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void operarioSoloListaSuGranja() throws Exception {
        mockMvc.perform(get("/api/v1/granjas")
                .with(httpBasic("operario@agro.test", CLAVE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(granjaOperario.getId()))
                .andExpect(jsonPath("$[0].nombre").value("El Sauce"));
    }

    @Test
    void supervisorListaTodasLasGranjas() throws Exception {
        mockMvc.perform(get("/api/v1/granjas")
                .with(httpBasic("supervisor@agro.test", CLAVE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void operarioNoPuedeConsultarUnaLineaAjena() throws Exception {
        mockMvc.perform(get("/api/v1/lineas/{lineaId}", lineaAjena.getId())
                .with(httpBasic("operario@agro.test", CLAVE)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void supervisorNoPuedeRegistrarTemperaturas() throws Exception {
        String cuerpo = "{\"valorCelsius\":24.5}";

        mockMvc.perform(post("/api/v1/lineas/{lineaId}/temperaturas", lineaOperario.getId())
                .with(httpBasic("supervisor@agro.test", CLAVE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void validaElRangoDeUnaTemperaturaManual() throws Exception {
        String cuerpo = "{\"valorCelsius\":50.01}";

        mockMvc.perform(post("/api/v1/lineas/{lineaId}/temperaturas", lineaOperario.getId())
                .with(httpBasic("operario@agro.test", CLAVE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.fieldErrors.valorCelsius").exists());
    }

    @Test
    void aplicaLaPrecisionYLosUmbralesTermicos() throws Exception {
        registrarTemperaturaYEsperarEstado("22.99", "NORMAL");
        registrarTemperaturaYEsperarEstado("23", "WARNING");
        registrarTemperaturaYEsperarEstado("26.99", "WARNING");
        registrarTemperaturaYEsperarEstado("27", "CRITICAL");

        mockMvc.perform(post("/api/v1/lineas/{lineaId}/temperaturas", lineaOperario.getId())
                .with(credencialesOperario())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"valorCelsius\":22.999}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("La temperatura puede tener como máximo dos decimales."));
    }

    @Test
    void respondeBadRequestAnteJsonMalFormado() throws Exception {
        mockMvc.perform(post("/api/v1/lineas/{lineaId}/temperaturas", lineaOperario.getId())
                .with(credencialesOperario())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"valorCelsius\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void creaFlushingYActualizaLaTemperaturaEfectiva() throws Exception {
        String operationId = "b687e9ac-f047-42e8-9d84-2f446ba455f2";
        String cuerpo = crearCuerpoFlushing(operationId);

        mockMvc.perform(post("/api/v1/lineas/{lineaId}/flushings", lineaOperario.getId())
                .with(httpBasic("operario@agro.test", CLAVE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.lineaId").value(lineaOperario.getId()))
                .andExpect(jsonPath("$.operarioNombre").value("Operario Uno"))
                .andExpect(jsonPath("$.motivo").value("Limpieza preventiva"))
                .andExpect(jsonPath("$.temperaturaAntesC").value(28.0))
                .andExpect(jsonPath("$.temperaturaDespuesC").value(21.5));

        mockMvc.perform(get("/api/v1/lineas/{lineaId}", lineaOperario.getId())
                .with(httpBasic("operario@agro.test", CLAVE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperaturaEfectiva.valorCelsius").value(21.5))
                .andExpect(jsonPath("$.temperaturaEfectiva.origen").value("FLUSHING"))
                .andExpect(jsonPath("$.temperaturaEfectiva.estado").value("NORMAL"));
    }

    @Test
    void repiteUnFlushingDeFormaIdempotente() throws Exception {
        String operationId = "6f8b9f8c-b77c-45fc-987d-8eb34e005994";
        String cuerpo = crearCuerpoFlushing(operationId);

        mockMvc.perform(post("/api/v1/lineas/{lineaId}/flushings", lineaOperario.getId())
                .with(httpBasic("operario@agro.test", CLAVE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/lineas/{lineaId}/flushings", lineaOperario.getId())
                .with(httpBasic("operario@agro.test", CLAVE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo))
                .andExpect(status().isCreated());

        org.junit.jupiter.api.Assertions.assertEquals(1L, flushingRepository.count());
    }

    @Test
    void rechazaReutilizarUuidConDatosDiferentes() throws Exception {
        String operationId = "bd488984-aa42-4e5f-aefb-aab34084fa95";
        crearFlushing(operationId);
        String cuerpoDiferente = crearCuerpoFlushing(operationId)
                .replace("Limpieza preventiva", "Otro motivo");

        mockMvc.perform(post("/api/v1/lineas/{lineaId}/flushings", lineaOperario.getId())
                .with(credencialesOperario())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpoDiferente))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void rechazaUuidReutilizadoConOtraLineaUOtrosDatos() throws Exception {
        String operationId = "69ff0302-35df-431a-abf5-7e9885368c43";
        String original = crearCuerpoFlushing(operationId);
        crearFlushing(operationId);

        esperarConflictoUuid(lineaSecundariaOperario.getId(), original);
        esperarConflictoUuid(lineaOperario.getId(), original.replace("Sin novedades", "Otra observación"));
        esperarConflictoUuid(lineaOperario.getId(), original.replace("28.0", "29.0"));
        esperarConflictoUuid(lineaOperario.getId(), original.replace("21.5", "21.6"));
    }

    @Test
    void controlaUuidDeOtroOperarioYObservacionNula() throws Exception {
        String operationId = "2d69030b-c7f1-4c51-86ea-f9b825264f27";
        String sinObservacion = crearCuerpoFlushing(operationId)
                .replace("\"observacion\":\"Sin novedades\",", "\"observacion\":null,");

        mockMvc.perform(post("/api/v1/lineas/{lineaId}/flushings", lineaOperario.getId())
                .with(credencialesOperario())
                .contentType(MediaType.APPLICATION_JSON)
                .content(sinObservacion))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/lineas/{lineaId}/flushings", lineaOperario.getId())
                .with(credencialesOperario())
                .contentType(MediaType.APPLICATION_JSON)
                .content(sinObservacion))
                .andExpect(status().isCreated());

        esperarConflictoUuid(lineaOperario.getId(), crearCuerpoFlushing(operationId));

        mockMvc.perform(post("/api/v1/lineas/{lineaId}/flushings", lineaOperario.getId())
                .with(httpBasic("otro@agro.test", CLAVE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(sinObservacion))
                .andExpect(status().isConflict());
    }

    @Test
    void operarioSinGranjaNoObtieneAlcance() throws Exception {
        RequestPostProcessor credenciales = httpBasic("sin-granja@agro.test", CLAVE);

        mockMvc.perform(get("/api/v1/granjas").with(credenciales))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/flushings").with(credenciales))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/v1/alertas").with(credenciales))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void recorreAutenticacionYCatalogoCompleto() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login").with(credencialesOperario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("operario@agro.test"))
                .andExpect(jsonPath("$.rol").value("OPERARIO"))
                .andExpect(jsonPath("$.granjaId").value(granjaOperario.getId()));

        mockMvc.perform(get("/api/v1/me").with(credencialesOperario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Operario Uno"));

        Long galponId = lineaOperario.getGalpon().getId();
        mockMvc.perform(get("/api/v1/granjas/{granjaId}/galpones", granjaOperario.getId())
                .with(credencialesOperario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(galponId));

        mockMvc.perform(get("/api/v1/galpones/{galponId}/lineas", galponId)
                .with(credencialesOperario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(lineaOperario.getId()))
                .andExpect(jsonPath("$[0].temperaturaEfectiva").doesNotExist());

        mockMvc.perform(get("/api/v1/lineas/{lineaId}/temperaturas", lineaOperario.getId())
                .with(credencialesOperario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/v1/flushings").with(credencialesOperario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/v1/alertas").with(credencialesOperario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void registraHistorialYAdministraElCicloDeUnaAlerta() throws Exception {
        mockMvc.perform(post("/api/v1/lineas/{lineaId}/temperaturas", lineaOperario.getId())
                .with(credencialesOperario())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"valorCelsius\":27.0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("CRITICAL"));

        Long alertaId = alertaRepository.findByLineaId(lineaOperario.getId()).orElseThrow().getId();
        mockMvc.perform(get("/api/v1/alertas").with(credencialesOperario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("CRITICAL"));

        mockMvc.perform(get("/api/v1/alertas/{id}", alertaId).with(credencialesOperario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperaturaC").value(27.0));

        mockMvc.perform(post("/api/v1/lineas/{lineaId}/temperaturas", lineaOperario.getId())
                .with(credencialesOperario())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"valorCelsius\":28.0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("CRITICAL"));

        mockMvc.perform(get("/api/v1/alertas/{id}", alertaId).with(credencialesOperario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temperaturaC").value(28.0));

        mockMvc.perform(post("/api/v1/lineas/{lineaId}/temperaturas", lineaOperario.getId())
                .with(credencialesOperario())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"valorCelsius\":22.0}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("NORMAL"));

        mockMvc.perform(get("/api/v1/lineas/{lineaId}/temperaturas", lineaOperario.getId())
                .with(credencialesOperario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].valorCelsius").value(22.0));

        mockMvc.perform(get("/api/v1/alertas").with(credencialesOperario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void supervisorConsultaSeguimientoYAlertasSinPoderEscribir() throws Exception {
        String operationId = "10806440-9e64-4df2-b2e2-b14d92ec2e4a";
        String cuerpo = crearCuerpoFlushing(operationId).replace("21.5", "28.0");
        mockMvc.perform(post("/api/v1/lineas/{lineaId}/flushings", lineaOperario.getId())
                .with(credencialesOperario())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo))
                .andExpect(status().isCreated());

        RequestPostProcessor supervisor = httpBasic("supervisor@agro.test", CLAVE);
        mockMvc.perform(get("/api/v1/flushings").with(supervisor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(get("/api/v1/alertas").with(supervisor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].estado").value("CRITICAL"));

        Long flushingId = flushingRepository.findByClientOperationId(operationId).orElseThrow().getId();
        mockMvc.perform(delete("/api/v1/flushings/{id}", flushingId).with(supervisor))
                .andExpect(status().isForbidden());
    }

    @Test
    void actualizaConsultaYEliminaUnFlushingPropio() throws Exception {
        String operationId = "d7a4ce54-b816-4c8a-b635-0f48821f0bd0";
        crearFlushing(operationId);
        RegistroFlushing flushing = flushingRepository.findByClientOperationId(operationId).orElseThrow();

        mockMvc.perform(get("/api/v1/flushings/{id}", flushing.getId()).with(credencialesOperario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(flushing.getId()));

        mockMvc.perform(get("/api/v1/lineas/{lineaId}/flushings", lineaOperario.getId())
                .with(credencialesOperario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        String actualizacion = "{"
                + "\"motivo\":\"Corrección del registro\","
                + "\"observacion\":null,"
                + "\"temperaturaAntesC\":20.0,"
                + "\"temperaturaDespuesC\":25.0,"
                + "\"version\":" + flushing.getVersion()
                + "}";
        mockMvc.perform(patch("/api/v1/flushings/{id}", flushing.getId())
                .with(credencialesOperario())
                .contentType(MediaType.APPLICATION_JSON)
                .content(actualizacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motivo").value("Corrección del registro"))
                .andExpect(jsonPath("$.temperaturaDespuesC").value(25.0));

        String versionObsoleta = actualizacion.replace(
                "\"version\":" + flushing.getVersion(),
                "\"version\":999");
        mockMvc.perform(patch("/api/v1/flushings/{id}", flushing.getId())
                .with(credencialesOperario())
                .contentType(MediaType.APPLICATION_JSON)
                .content(versionObsoleta))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));

        RegistroFlushing actualizado = flushingRepository.findById(flushing.getId()).orElseThrow();
        String precisionInvalida = actualizacion
                .replace("25.0", "22.999")
                .replace("\"version\":" + flushing.getVersion(),
                        "\"version\":" + actualizado.getVersion());
        mockMvc.perform(patch("/api/v1/flushings/{id}", flushing.getId())
                .with(credencialesOperario())
                .contentType(MediaType.APPLICATION_JSON)
                .content(precisionInvalida))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("La temperatura puede tener como máximo dos decimales."));

        mockMvc.perform(patch("/api/v1/flushings/{id}", flushing.getId())
                .with(httpBasic("otro@agro.test", CLAVE))
                .contentType(MediaType.APPLICATION_JSON)
                .content(actualizacion))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/v1/flushings/{id}", flushing.getId())
                .with(credencialesOperario()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/flushings/{id}", flushing.getId()).with(credencialesOperario()))
                .andExpect(status().isNotFound());
    }

    @Test
    void guardaReemplazaDescargaYEliminaUnaFotografia() throws Exception {
        String operationId = "791cadc8-34cb-4b90-8367-c6fe08d815e7";
        crearFlushing(operationId);
        Long flushingId = flushingRepository.findByClientOperationId(operationId).orElseThrow().getId();

        MockMultipartFile png = new MockMultipartFile(
                "archivo", "primera.png", "image/png",
                new byte[] { (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 1 });
        mockMvc.perform(multipart("/api/v1/flushings/{id}/foto", flushingId)
                .file(png)
                .with(credencialesOperario()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreOriginal").value("primera.png"))
                .andExpect(jsonPath("$.tipoContenido").value("image/png"));

        mockMvc.perform(get("/api/v1/flushings/{id}/foto/metadata", flushingId)
                .with(credencialesOperario()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tamanoBytes").value(9));

        mockMvc.perform(get("/api/v1/flushings/{id}/foto", flushingId)
                .with(credencialesOperario()))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .content().contentType("image/png"));

        MockMultipartFile jpeg = new MockMultipartFile(
                "archivo", "segunda.jpg", "image/jpeg",
                new byte[] { (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, 5, 6, 7 });
        mockMvc.perform(multipart("/api/v1/flushings/{id}/foto", flushingId)
                .file(jpeg)
                .with(credencialesOperario()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombreOriginal").value("segunda.jpg"));

        MockMultipartFile invalido = new MockMultipartFile(
                "archivo", "nota.txt", "text/plain", new byte[] { 8 });
        mockMvc.perform(multipart("/api/v1/flushings/{id}/foto", flushingId)
                .file(invalido)
                .with(credencialesOperario()))
                .andExpect(status().isBadRequest());

        MockMultipartFile firmaFalsa = new MockMultipartFile(
                "archivo", "falsa.png", "image/png", new byte[] { 1, 2, 3, 4 });
        mockMvc.perform(multipart("/api/v1/flushings/{id}/foto", flushingId)
                .file(firmaFalsa)
                .with(credencialesOperario()))
                .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/v1/flushings/{id}/foto", flushingId)
                .with(credencialesOperario()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/flushings/{id}/foto/metadata", flushingId)
                .with(credencialesOperario()))
                .andExpect(status().isNotFound());
    }

    private void crearFlushing(String operationId) throws Exception {
        mockMvc.perform(post("/api/v1/lineas/{lineaId}/flushings", lineaOperario.getId())
                .with(credencialesOperario())
                .contentType(MediaType.APPLICATION_JSON)
                .content(crearCuerpoFlushing(operationId)))
                .andExpect(status().isCreated());
    }

    private void registrarTemperaturaYEsperarEstado(String valor, String estado) throws Exception {
        mockMvc.perform(post("/api/v1/lineas/{lineaId}/temperaturas", lineaOperario.getId())
                .with(credencialesOperario())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"valorCelsius\":" + valor + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value(estado));
    }

    private void esperarConflictoUuid(Long lineaId, String cuerpo) throws Exception {
        mockMvc.perform(post("/api/v1/lineas/{lineaId}/flushings", lineaId)
                .with(credencialesOperario())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo))
                .andExpect(status().isConflict());
    }

    private RequestPostProcessor credencialesOperario() {
        return httpBasic("operario@agro.test", CLAVE);
    }

    private String crearCuerpoFlushing(String operationId) {
        return "{"
                + "\"clientOperationId\":\"" + operationId + "\","
                + "\"motivo\":\"Limpieza preventiva\","
                + "\"observacion\":\"Sin novedades\","
                + "\"temperaturaAntesC\":28.0,"
                + "\"temperaturaDespuesC\":21.5"
                + "}";
    }
}
