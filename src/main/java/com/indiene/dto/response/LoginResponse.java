package com.indiene.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resposta do login com token JWT")
public record LoginResponse(
        @Schema(description = "Token JWT assinado (Bearer)", example = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiIzZjljMmIxNC03ZDRlLTRmMWMtOWEzZS0yYmM4YTFmMGU5ZDcifQ.assinatura")
        String token,

        @Schema(description = "Tempo de expiração do token em milissegundos", example = "86400000")
        long expiresIn,

        @Schema(description = "Dados do usuário autenticado")
        UsuarioResponse usuario
) {
}
