package com.indiene.dto.response;

public record LoginResponse(
        String token,
        long expiresIn,
        UsuarioResponse usuario
) {
}
