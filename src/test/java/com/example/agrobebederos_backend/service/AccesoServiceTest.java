package com.example.agrobebederos_backend.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.agrobebederos_backend.entity.Galpon;
import com.example.agrobebederos_backend.entity.Granja;
import com.example.agrobebederos_backend.entity.LineaBebedero;
import com.example.agrobebederos_backend.entity.RegistroFlushing;
import com.example.agrobebederos_backend.entity.RolUsuario;
import com.example.agrobebederos_backend.entity.Usuario;
import com.example.agrobebederos_backend.exception.AccesoDenegadoException;

class AccesoServiceTest {

    private AccesoService service;
    private Granja granjaUno;
    private Granja granjaDos;

    @BeforeEach
    void setUp() {
        service = new AccesoService();
        granjaUno = mock(Granja.class);
        granjaDos = mock(Granja.class);
        when(granjaUno.getId()).thenReturn(1L);
        when(granjaDos.getId()).thenReturn(2L);
    }

    @Test
    void supervisorPuedeConsultarCualquierGranja() {
        Usuario supervisor = mock(Usuario.class);
        when(supervisor.getRol()).thenReturn(RolUsuario.SUPERVISOR);

        assertDoesNotThrow(() -> service.verificarAccesoGranja(supervisor, granjaDos));
    }

    @Test
    void operarioSoloPuedeConsultarSuGranja() {
        Usuario operario = mock(Usuario.class);
        when(operario.getRol()).thenReturn(RolUsuario.OPERARIO);
        when(operario.getGranja()).thenReturn(granjaUno);

        assertDoesNotThrow(() -> service.verificarAccesoGranja(operario, granjaUno));
        assertThrows(AccesoDenegadoException.class,
                () -> service.verificarAccesoGranja(operario, granjaDos));
    }

    @Test
    void operarioSinGranjaNoPuedeConsultarUbicaciones() {
        Usuario operario = mock(Usuario.class);
        when(operario.getRol()).thenReturn(RolUsuario.OPERARIO);
        when(operario.getGranja()).thenReturn(null);

        assertThrows(AccesoDenegadoException.class,
                () -> service.verificarAccesoGranja(operario, granjaUno));
    }

    @Test
    void supervisorNoPuedeRealizarMutacionesDeOperario() {
        Usuario supervisor = mock(Usuario.class);
        when(supervisor.getRol()).thenReturn(RolUsuario.SUPERVISOR);

        assertThrows(AccesoDenegadoException.class, () -> service.exigirOperario(supervisor));
    }

    @Test
    void soloElAutorPuedeModificarUnFlushing() {
        Usuario operario = mock(Usuario.class);
        Usuario otro = mock(Usuario.class);
        when(operario.getRol()).thenReturn(RolUsuario.OPERARIO);
        when(operario.getId()).thenReturn(10L);
        when(operario.getGranja()).thenReturn(granjaUno);
        when(otro.getId()).thenReturn(11L);

        Galpon galpon = mock(Galpon.class);
        when(galpon.getGranja()).thenReturn(granjaUno);
        LineaBebedero linea = mock(LineaBebedero.class);
        when(linea.getGalpon()).thenReturn(galpon);
        RegistroFlushing flushing = mock(RegistroFlushing.class);
        when(flushing.getLinea()).thenReturn(linea);
        when(flushing.getOperario()).thenReturn(otro);

        assertThrows(AccesoDenegadoException.class,
                () -> service.verificarPropietario(operario, flushing));
    }

    @Test
    void autorOperarioPuedeModificarSuFlushing() {
        Usuario operario = mock(Usuario.class);
        when(operario.getRol()).thenReturn(RolUsuario.OPERARIO);
        when(operario.getId()).thenReturn(10L);
        when(operario.getGranja()).thenReturn(granjaUno);

        Galpon galpon = mock(Galpon.class);
        when(galpon.getGranja()).thenReturn(granjaUno);
        LineaBebedero linea = mock(LineaBebedero.class);
        when(linea.getGalpon()).thenReturn(galpon);
        RegistroFlushing flushing = mock(RegistroFlushing.class);
        when(flushing.getLinea()).thenReturn(linea);
        when(flushing.getOperario()).thenReturn(operario);

        assertDoesNotThrow(() -> service.verificarPropietario(operario, flushing));
    }
}
