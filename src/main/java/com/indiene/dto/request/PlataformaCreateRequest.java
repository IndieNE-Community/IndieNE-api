package com.indiene.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload de criação de plataforma/sistema suportado por um jogo")
public record PlataformaCreateRequest(
        @Schema(description = "ID do jogo dono da plataforma", example = "1")
        @NotNull @Positive Long jogoId,

        @Schema(description = "Nome da plataforma/sistema", example = "Windows")
        @NotBlank @Size(max = 100) String plataforma
) {
}
