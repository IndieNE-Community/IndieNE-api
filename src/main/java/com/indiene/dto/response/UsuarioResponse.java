package com.indiene.dto.response;

import com.indiene.model.TipoUsuario;
import com.indiene.model.Usuario;

import java.util.UUID;

public record UsuarioResponse(
        UUID id,
        String nome,
        String email,
        TipoUsuario tipo
) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTipo());
    }
}
