package com.indiene.dto.response;

import com.indiene.model.Doacao;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Doação retornada pela API")
public record DoacaoResponse(
        @Schema(description = "Identificador da doação", example = "1")
        Long id,

        @Schema(description = "Valor da doação", example = "50.00")
        Double valor,

        @Schema(description = "Data/hora da doação")
        LocalDateTime data,

        @Schema(description = "ID do jogo apoiado", example = "1")
        Long jogoId,

        @Schema(description = "ID do usuário doador", example = "3f9c2b14-7d4e-4f1c-9a3e-2bc8a1f0e9d7")
        UUID usuarioId,

        @Schema(description = "Nome do usuário doador (apoiador)", example = "Ana Beatriz")
        String usuarioNome
) {

    public static DoacaoResponse from(Doacao doacao) {
        return from(doacao, null);
    }

    public static DoacaoResponse from(Doacao doacao, String usuarioNome) {
        return new DoacaoResponse(
                doacao.getId(),
                doacao.getValor(),
                doacao.getData(),
                doacao.getJogoId(),
                doacao.getUsuarioId(),
                usuarioNome
        );
    }
}
