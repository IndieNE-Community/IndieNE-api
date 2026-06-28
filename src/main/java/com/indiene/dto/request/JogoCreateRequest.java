package com.indiene.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Payload de criação de jogo")
public record JogoCreateRequest(
        @Schema(description = "Título do jogo", example = "Indie Quest")
        @NotBlank @Size(max = 200) String titulo,

        @Schema(description = "Descrição do jogo")
        @Size(max = 10_000) String descricao,

        @Schema(description = "Meta financeira da campanha", example = "10000.00")
        @PositiveOrZero Double metaFinanceira,

        @Schema(description = "Identificador da campanha associada")
        Integer campanha,

        @Schema(description = "Data de início do desenvolvimento", example = "2026-01-15")
        LocalDate dataInicio,

        @Schema(description = "Data prevista de conclusão", example = "2026-12-31")
        LocalDate dataConclusao,

        @Schema(description = "Número de jogadores suportados", example = "4")
        @PositiveOrZero Integer numJogadores,

        @Schema(description = "Gênero do jogo", example = "RPG")
        @Size(max = 100) String genero,

        @Schema(description = "Suporta controle?", example = "true")
        Boolean controle,

        @Schema(description = "URL da thumbnail")
        @Size(max = 500) String imgThumb
) {
}
