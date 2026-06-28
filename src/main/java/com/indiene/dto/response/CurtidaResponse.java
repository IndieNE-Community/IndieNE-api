package com.indiene.dto.response;

import com.indiene.model.Curtida;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Curtida retornada pela API")
public record CurtidaResponse(
        @Schema(description = "Identificador da curtida", example = "1")
        Long id,

        @Schema(description = "Tipo/reação", example = "LIKE")
        String tipo,

        @Schema(description = "ID da publicação curtida (nulo se a curtida for de comentário)", example = "1")
        Long postagemId,

        @Schema(description = "ID do comentário curtido (nulo se a curtida for de publicação)", example = "5")
        Long comentarioId,

        @Schema(description = "ID do usuário que curtiu", example = "3f9c2b14-7d4e-4f1c-9a3e-2bc8a1f0e9d7")
        UUID usuarioId
) {

    public static CurtidaResponse from(Curtida curtida) {
        return new CurtidaResponse(
                curtida.getId(),
                curtida.getTipo(),
                curtida.getPostagemId(),
                curtida.getComentarioId(),
                curtida.getUsuarioId()
        );
    }
}
