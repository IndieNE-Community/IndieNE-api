package com.indiene.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Payload de criação de comentário")
public record ComentarioCreateRequest(
        @Schema(description = "Texto do comentário", example = "Ficou muito bom esse devlog!")
        @NotBlank @Size(max = 5_000) String texto,

        @Schema(description = "ID da publicação comentada", example = "1")
        @NotNull @Positive Long postagemId
) {
}
