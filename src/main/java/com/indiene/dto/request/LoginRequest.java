package com.indiene.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Credenciais de login")
public record LoginRequest(
        @Schema(description = "E-mail do usuário", example = "fulano@x.com")
        @NotBlank @Email String email,

        @Schema(description = "Senha em texto plano", example = "minhaSenha123")
        @NotBlank String senha
) {
}
