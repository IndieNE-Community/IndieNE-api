package com.indiene.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload de criação de curtida. Informe exatamente um alvo: postagemId OU comentarioId.")
public record CurtidaCreateRequest(
        @Schema(description = "ID da publicação a curtir", example = "1")
        @Positive Long postagemId,

        @Schema(description = "ID do comentário a curtir", example = "5")
        @Positive Long comentarioId,

        @Schema(description = "Tipo/reação opcional", example = "LIKE")
        @Size(max = 50) String tipo
) {
}
