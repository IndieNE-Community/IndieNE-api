package com.indiene.dto.response;

import com.indiene.model.Imagem;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Imagem retornada pela API")
public record ImagemResponse(
        @Schema(description = "Identificador da imagem", example = "1")
        Long id,

        @Schema(description = "URL (ou conteúdo) da imagem", example = "https://cdn.exemplo.com/foto.png")
        String imagem,

        @Schema(description = "ID do jogo associado (nulo se a imagem for de publicação)", example = "1")
        Long jogoId,

        @Schema(description = "ID da publicação associada (nulo se a imagem for de jogo)", example = "5")
        Long postagemId
) {

    public static ImagemResponse from(Imagem imagem) {
        return new ImagemResponse(
                imagem.getId(),
                imagem.getImagem(),
                imagem.getJogoId(),
                imagem.getPostagemId()
        );
    }
}
