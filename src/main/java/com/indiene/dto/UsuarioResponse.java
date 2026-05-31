package com.indiene.dto;

import com.indiene.model.TipoUsuario;
import com.indiene.model.Usuario;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        TipoUsuario tipo
) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTipo());
    }
}
