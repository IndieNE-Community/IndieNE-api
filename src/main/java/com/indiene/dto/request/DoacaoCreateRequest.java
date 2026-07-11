package com.indiene.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "Payload de criação de doação para a campanha de um jogo")
public record DoacaoCreateRequest(
        @Schema(description = "ID do jogo apoiado", example = "1")
        @NotNull @Positive Long jogoId,

        @Schema(description = "Valor da doação", example = "50.00")
        @NotNull @Positive Double valor
) {
}
