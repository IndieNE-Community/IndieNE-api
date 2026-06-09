package com.indiene.dto.request;

import com.indiene.model.TipoUsuario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload de criação de usuário")
public record UsuarioCreateRequest(
        @Schema(description = "Nome do usuário", example = "Fulano da Silva")
        @NotBlank String nome,

        @Schema(description = "E-mail único do usuário", example = "fulano@x.com")
        @NotBlank @Email String email,

        @Schema(description = "Senha em texto plano (hasheada antes de persistir)", example = "minhaSenha123", minLength = 8, maxLength = 100)
        @NotBlank @Size(min = 8, max = 100) String senha,

        @Schema(description = "Tipo do usuário", example = "DESENVOLVEDOR")
        @NotNull TipoUsuario tipo
) {
}
