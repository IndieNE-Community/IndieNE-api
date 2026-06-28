package com.indiene.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload de criação de publicação")
public record PostagemCreateRequest(
        @Schema(description = "Título da publicação", example = "Devlog #1")
        @NotBlank @Size(max = 200) String titulo,

        @Schema(description = "Conteúdo da publicação", example = "Hoje implementamos o sistema de salvamento.")
        @Size(max = 10_000) String descricao,

        @Schema(description = "ID do jogo associado", example = "42")
        @NotNull @Positive Long jogoId
) {
}
