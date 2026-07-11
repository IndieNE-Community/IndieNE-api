package com.indiene.dto.response;

import com.indiene.model.Jogo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Schema(description = "Jogo retornado pela API, com dados de exibição e resumo da campanha")
public record JogoResponse(
        @Schema(description = "Identificador do jogo", example = "1")
        Long id,

        @Schema(description = "Título do jogo", example = "God Breakers")
        String titulo,

        @Schema(description = "Descrição do jogo")
        String descricao,

        @Schema(description = "Meta financeira da campanha", example = "100000.00")
        Double metaFinanceira,

        @Schema(description = "Duração da campanha em dias", example = "86")
        Integer campanha,

        @Schema(description = "Data de início do desenvolvimento", example = "2026-01-15")
        LocalDate dataInicio,

        @Schema(description = "Data prevista de conclusão", example = "2026-12-31")
        LocalDate dataConclusao,

        @Schema(description = "Avaliação/nota do jogo (0-100)", example = "87")
        Integer avaliacao,

        @Schema(description = "Número mínimo de jogadores", example = "1")
        Integer numJogadoresMin,

        @Schema(description = "Número máximo de jogadores", example = "4")
        Integer numJogadoresMax,

        @Schema(description = "Suporta controle?", example = "true")
        Boolean controle,

        @Schema(description = "URL da thumbnail")
        String imgThumb,

        @Schema(description = "ID do usuário dono do jogo", example = "3f9c2b14-7d4e-4f1c-9a3e-2bc8a1f0e9d7")
        UUID usuarioId,

        @Schema(description = "Nome do desenvolvedor (dono do jogo)", example = "To The Sky")
        String desenvolvedor,

        @Schema(description = "Gêneros do jogo", example = "[\"Roguelike\", \"Ação\"]")
        List<String> generos,

        @Schema(description = "Categorias do jogo", example = "[\"destaque\", \"destaque-hero\"]")
        List<String> categorias,

        @Schema(description = "Plataformas/sistemas suportados", example = "[\"Windows\", \"Linux\"]")
        List<String> plataformas,

        @Schema(description = "Total arrecadado em doações", example = "68745.00")
        double totalArrecadado,

        @Schema(description = "Número de apoiadores (doadores distintos)", example = "851")
        long apoiadores,

        @Schema(description = "Percentual da meta atingido (0-100)", example = "68")
        int metaPercentual,

        @Schema(description = "Dias restantes de campanha (nulo se sem data de início/duração)", example = "42")
        Integer diasRestantes
) {

    public static JogoResponse of(
            Jogo jogo,
            String desenvolvedor,
            List<String> plataformas,
            double totalArrecadado,
            long apoiadores,
            int metaPercentual,
            Integer diasRestantes) {
        return new JogoResponse(
                jogo.getId(),
                jogo.getTitulo(),
                jogo.getDescricao(),
                jogo.getMetaFinanceira(),
                jogo.getCampanha(),
                jogo.getDataInicio(),
                jogo.getDataConclusao(),
                jogo.getAvaliacao(),
                jogo.getNumJogadoresMin(),
                jogo.getNumJogadoresMax(),
                jogo.getControle(),
                jogo.getImgThumb(),
                jogo.getUsuarioId(),
                desenvolvedor,
                jogo.getGeneros() == null ? List.of() : new ArrayList<>(jogo.getGeneros()),
                jogo.getCategorias() == null ? List.of() : new ArrayList<>(jogo.getCategorias()),
                plataformas == null ? List.of() : plataformas,
                totalArrecadado,
                apoiadores,
                metaPercentual,
                diasRestantes
        );
    }
}
