package com.indiene.dto.response;

import com.indiene.model.Jogo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Jogo retornado pela API")
public record JogoResponse(
        @Schema(description = "Identificador do jogo", example = "1")
        Long id,

        @Schema(description = "Título do jogo", example = "Indie Quest")
        String titulo,

        @Schema(description = "Descrição do jogo", example = "Um jogo legal")
        String descricao,

        @Schema(description = "Meta financeira da campanha", example = "10000.00")
        Double metaFinanceira,

        @Schema(description = "Identificador da campanha associada", example = "1")
        Integer campanha,

        @Schema(description = "Data de início do desenvolvimento", example = "2026-01-15")
        LocalDate dataInicio,

        @Schema(description = "Data prevista de conclusão", example = "2026-12-31")
        LocalDate dataConclusao,

        @Schema(description = "Número de jogadores suportados", example = "4")
        Integer numJogadores,

        @Schema(description = "Gênero do jogo", example = "RPG")
        String genero,

        @Schema(description = "Suporta controle?", example = "true")
        Boolean controle,

        @Schema(description = "URL da thumbnail", example = "https://x/y.png")
        String imgThumb,

        @Schema(description = "ID do usuário dono do jogo", example = "3f9c2b14-7d4e-4f1c-9a3e-2bc8a1f0e9d7")
        UUID usuarioId
) {

    public static JogoResponse from(Jogo jogo) {
        return new JogoResponse(
                jogo.getId(),
                jogo.getTitulo(),
                jogo.getDescricao(),
                jogo.getMetaFinanceira(),
                jogo.getCampanha(),
                jogo.getDataInicio(),
                jogo.getDataConclusao(),
                jogo.getNumJogadores(),
                jogo.getGenero(),
                jogo.getControle(),
                jogo.getImgThumb(),
                jogo.getUsuarioId()
        );
    }
}
