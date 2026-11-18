package com.example.agrobebederos_backend.service;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.agrobebederos_backend.dto.UsuarioResponse;
import com.example.agrobebederos_backend.entity.Granja;
import com.example.agrobebederos_backend.entity.Usuario;
import com.example.agrobebederos_backend.exception.RecursoNoEncontradoException;
import com.example.agrobebederos_backend.repository.UsuarioRepository;

@Service
public class UsuarioActualService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioActualService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public Usuario obtener() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new RecursoNoEncontradoException("No existe un usuario autenticado.");
        }

        return usuarioRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new RecursoNoEncontradoException("El usuario autenticado ya no existe."));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerResponse() {
        return convertir(obtener());
    }

    public UsuarioResponse convertir(Usuario usuario) {
        Granja granja = usuario.getGranja();
        Long granjaId = null;
        String granjaNombre = null;
        if (granja != null) {
            granjaId = granja.getId();
            granjaNombre = granja.getNombre();
        }
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getNombre(),
                usuario.getEmail(),
                usuario.getRol().name(),
                granjaId,
                granjaNombre);
    }
}
