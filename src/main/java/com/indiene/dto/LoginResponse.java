package com.indiene.dto;

public record LoginResponse(
        String token,
        long expiresIn,
        UsuarioResponse usuario
) {
}
