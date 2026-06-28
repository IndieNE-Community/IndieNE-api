package com.indiene.dto.response;

import com.indiene.model.Postagem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Publicação retornada pela API")
public record PostagemResponse(
        @Schema(description = "Identificador da publicação", example = "1")
        Long id,

        @Schema(description = "Título da publicação", example = "Devlog #1")
        String titulo,

        @Schema(description = "Conteúdo da publicação")
        String descricao,

        @Schema(description = "Data/hora de criação")
        LocalDateTime data,

        @Schema(description = "ID do jogo associado", example = "42")
        Long jogoId,

        @Schema(description = "ID do usuário autor", example = "3f9c2b14-7d4e-4f1c-9a3e-2bc8a1f0e9d7")
        UUID usuarioId
) {

    public static PostagemResponse from(Postagem postagem) {
        return new PostagemResponse(
                postagem.getId(),
                postagem.getTitulo(),
                postagem.getDescricao(),
                postagem.getData(),
                postagem.getJogoId(),
                postagem.getUsuarioId()
        );
    }
}
