package com.indiene.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload de atualização de publicação")
public record PostagemUpdateRequest(
        @Schema(description = "Título da publicação", example = "Devlog #1 (atualizado)")
        @NotBlank @Size(max = 200) String titulo,

        @Schema(description = "Conteúdo da publicação", example = "Conteúdo revisado.")
        @Size(max = 10_000) String descricao
) {
}
