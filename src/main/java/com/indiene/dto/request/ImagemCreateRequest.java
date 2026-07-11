package com.indiene.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload de criação de imagem. Informe exatamente um alvo: jogoId OU postagemId.")
public record ImagemCreateRequest(
        @Schema(description = "ID do jogo dono da imagem", example = "1")
        @Positive Long jogoId,

        @Schema(description = "ID da publicação dona da imagem", example = "5")
        @Positive Long postagemId,

        @Schema(description = "URL (ou conteúdo) da imagem", example = "https://cdn.exemplo.com/foto.png")
        @NotBlank @Size(max = 2000) String imagem
) {
}
