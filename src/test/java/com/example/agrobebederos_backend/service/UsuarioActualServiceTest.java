package com.example.agrobebederos_backend.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.agrobebederos_backend.dto.UsuarioResponse;
import com.example.agrobebederos_backend.entity.Granja;
import com.example.agrobebederos_backend.entity.RolUsuario;
import com.example.agrobebederos_backend.entity.Usuario;
import com.example.agrobebederos_backend.exception.RecursoNoEncontradoException;
import com.example.agrobebederos_backend.repository.UsuarioRepository;

class UsuarioActualServiceTest {

    private UsuarioRepository usuarioRepository;
    private UsuarioActualService service;

    @BeforeEach
    void setUp() {
        usuarioRepository = mock(UsuarioRepository.class);
        service = new UsuarioActualService(usuarioRepository);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void limpiarContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rechazaContextoSinAutenticacion() {
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener());

        Authentication noAutenticada = mock(Authentication.class);
        when(noAutenticada.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.getContext().setAuthentication(noAutenticada);
        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener());
    }

    @Test
    void rechazaAutenticacionAnonima() {
        AnonymousAuthenticationToken anonima = new AnonymousAuthenticationToken(
                "clave",
                "anonimo",
                AuthorityUtils.createAuthorityList("ROLE_ANONYMOUS"));
        SecurityContextHolder.getContext().setAuthentication(anonima);

        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener());
    }

    @Test
    void rechazaUsuarioAutenticadoQueYaNoExiste() {
        Authentication autenticacion = mock(Authentication.class);
        when(autenticacion.isAuthenticated()).thenReturn(true);
        when(autenticacion.getName()).thenReturn("ausente@agro.test");
        when(usuarioRepository.findByEmailIgnoreCase("ausente@agro.test"))
                .thenReturn(Optional.empty());
        SecurityContextHolder.getContext().setAuthentication(autenticacion);

        assertThrows(RecursoNoEncontradoException.class, () -> service.obtener());
    }

    @Test
    void convierteOperarioConGranjaYSupervisorSinGranja() {
        Granja granja = mock(Granja.class);
        when(granja.getId()).thenReturn(4L);
        when(granja.getNombre()).thenReturn("El Sauce");

        Usuario operario = mock(Usuario.class);
        when(operario.getId()).thenReturn(10L);
        when(operario.getNombre()).thenReturn("Operario");
        when(operario.getEmail()).thenReturn("operario@agro.test");
        when(operario.getRol()).thenReturn(RolUsuario.OPERARIO);
        when(operario.getGranja()).thenReturn(granja);

        UsuarioResponse responseOperario = service.convertir(operario);
        assertEquals(4L, responseOperario.getGranjaId());
        assertEquals("El Sauce", responseOperario.getGranjaNombre());

        Usuario supervisor = mock(Usuario.class);
        when(supervisor.getRol()).thenReturn(RolUsuario.SUPERVISOR);
        UsuarioResponse responseSupervisor = service.convertir(supervisor);
        assertNull(responseSupervisor.getGranjaId());
        assertNull(responseSupervisor.getGranjaNombre());
    }
}
