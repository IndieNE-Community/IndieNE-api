package com.indiene.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

@Schema(description = "Payload de criação de jogo")
public record JogoCreateRequest(
        @Schema(description = "Título do jogo", example = "God Breakers")
        @NotBlank @Size(max = 200) String titulo,

        @Schema(description = "Descrição do jogo")
        @Size(max = 10_000) String descricao,

        @Schema(description = "Meta financeira da campanha (maior que zero; nula quando não há campanha)", example = "100000.00")
        @Positive Double metaFinanceira,

        @Schema(description = "Duração da campanha em dias", example = "86")
        @PositiveOrZero Integer campanha,

        @Schema(description = "Data de início do desenvolvimento", example = "2026-01-15")
        LocalDate dataInicio,

        @Schema(description = "Data prevista de conclusão", example = "2026-12-31")
        LocalDate dataConclusao,

        @Schema(description = "Avaliação/nota do jogo (0-100)", example = "87")
        @Min(0) @Max(100) Integer avaliacao,

        @Schema(description = "Número mínimo de jogadores", example = "1")
        @PositiveOrZero Integer numJogadoresMin,

        @Schema(description = "Número máximo de jogadores", example = "4")
        @PositiveOrZero Integer numJogadoresMax,

        @Schema(description = "Suporta controle?", example = "true")
        Boolean controle,

        @Schema(description = "URL da thumbnail")
        @Size(max = 500) String imgThumb,

        @Schema(description = "Gêneros do jogo", example = "[\"Roguelike\", \"Ação\"]")
        Set<@NotBlank @Size(max = 100) String> generos,

        @Schema(description = "Categorias do jogo", example = "[\"destaque\", \"destaque-hero\"]")
        Set<@NotBlank @Size(max = 100) String> categorias,

        @Schema(description = "Plataformas/sistemas suportados", example = "[\"Windows\", \"Linux\"]")
        Set<@NotBlank @Size(max = 100) String> plataformas
) {
}
