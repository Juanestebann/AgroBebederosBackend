package com.example.agrobebederos_backend.service;

import org.springframework.stereotype.Service;

import com.example.agrobebederos_backend.entity.Granja;
import com.example.agrobebederos_backend.entity.LineaBebedero;
import com.example.agrobebederos_backend.entity.RegistroFlushing;
import com.example.agrobebederos_backend.entity.RolUsuario;
import com.example.agrobebederos_backend.entity.Usuario;
import com.example.agrobebederos_backend.exception.AccesoDenegadoException;

@Service
public class AccesoService {

    public void exigirOperario(Usuario usuario) {
        if (usuario.getRol() != RolUsuario.OPERARIO) {
            throw new AccesoDenegadoException("Solo un operario puede realizar esta operación.");
        }
    }

    public void verificarAccesoGranja(Usuario usuario, Granja granja) {
        if (usuario.getRol() == RolUsuario.SUPERVISOR) {
            return;
        }
        Granja asignada = usuario.getGranja();
        if (asignada == null || !asignada.getId().equals(granja.getId())) {
            throw new AccesoDenegadoException("No tienes acceso a esta granja.");
        }
    }

    public void verificarAccesoLinea(Usuario usuario, LineaBebedero linea) {
        verificarAccesoGranja(usuario, linea.getGalpon().getGranja());
    }

    public void verificarPropietario(Usuario usuario, RegistroFlushing flushing) {
        exigirOperario(usuario);
        verificarAccesoLinea(usuario, flushing.getLinea());
        if (!flushing.getOperario().getId().equals(usuario.getId())) {
            throw new AccesoDenegadoException("Solo el operario autor puede modificar este flushing.");
        }
    }
}
