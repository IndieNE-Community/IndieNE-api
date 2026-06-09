package com.indiene.dto.response;

import com.indiene.model.TipoUsuario;
import com.indiene.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Usuário retornado pela API")
public record UsuarioResponse(
        @Schema(description = "Identificador único", example = "3f9c2b14-7d4e-4f1c-9a3e-2bc8a1f0e9d7")
        UUID id,

        @Schema(description = "Nome do usuário", example = "Fulano da Silva")
        String nome,

        @Schema(description = "E-mail do usuário", example = "fulano@x.com")
        String email,

        @Schema(description = "Tipo do usuário", example = "DESENVOLVEDOR")
        TipoUsuario tipo
) {

    public static UsuarioResponse from(Usuario usuario) {
        return new UsuarioResponse(usuario.getId(), usuario.getNome(), usuario.getEmail(), usuario.getTipo());
    }
}
