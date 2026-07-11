package com.indiene.dto.response;

import com.indiene.model.Plataforma;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Plataforma/sistema suportado por um jogo")
public record PlataformaResponse(
        @Schema(description = "Identificador da plataforma", example = "1")
        Long id,

        @Schema(description = "Nome da plataforma/sistema", example = "Windows")
        String plataforma,

        @Schema(description = "ID do jogo associado", example = "1")
        Long jogoId
) {

    public static PlataformaResponse from(Plataforma plataforma) {
        return new PlataformaResponse(
                plataforma.getId(),
                plataforma.getPlataforma(),
                plataforma.getJogoId()
        );
    }
}
