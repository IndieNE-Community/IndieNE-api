package com.indiene.dto.response;

import com.indiene.model.Comentario;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Comentário retornado pela API")
public record ComentarioResponse(
        @Schema(description = "Identificador do comentário", example = "1")
        Long id,

        @Schema(description = "Texto do comentário")
        String texto,

        @Schema(description = "Data/hora de criação")
        LocalDateTime data,

        @Schema(description = "ID da publicação comentada", example = "1")
        Long postagemId,

        @Schema(description = "ID do usuário autor", example = "3f9c2b14-7d4e-4f1c-9a3e-2bc8a1f0e9d7")
        UUID usuarioId,

        @Schema(description = "Quantidade de curtidas (LIKE)", example = "58")
        long likes,

        @Schema(description = "Quantidade de descurtidas (DISLIKE)", example = "0")
        long dislikes
) {

    public static ComentarioResponse of(Comentario comentario, long likes, long dislikes) {
        return new ComentarioResponse(
                comentario.getId(),
                comentario.getTexto(),
                comentario.getData(),
                comentario.getPostagemId(),
                comentario.getUsuarioId(),
                likes,
                dislikes
        );
    }
}
