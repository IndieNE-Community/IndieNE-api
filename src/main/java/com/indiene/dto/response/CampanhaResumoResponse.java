package com.indiene.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Resumo agregado da campanha de arrecadação de um jogo")
public record CampanhaResumoResponse(
        @Schema(description = "ID do jogo", example = "1")
        Long jogoId,

        @Schema(description = "Meta financeira da campanha", example = "100000.00")
        Double metaFinanceira,

        @Schema(description = "Total arrecadado em doações", example = "68745.00")
        double totalArrecadado,

        @Schema(description = "Número de apoiadores (doadores distintos)", example = "851")
        long apoiadores,

        @Schema(description = "Percentual da meta atingido (0-100)", example = "68")
        int metaPercentual
) {
}
