package com.indiene.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload de atualização de comentário")
public record ComentarioUpdateRequest(
        @Schema(description = "Texto do comentário", example = "Comentário revisado.")
        @NotBlank @Size(max = 5_000) String texto
) {
}
