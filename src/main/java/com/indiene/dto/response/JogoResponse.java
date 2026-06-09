package com.indiene.dto.response;

import com.indiene.model.Jogo;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.UUID;

@Schema(description = "Jogo retornado pela API")
public record JogoResponse(
        @Schema(description = "Identificador do jogo", example = "1")
        Long id,

        String titulo,
        String descricao,
        Double metaFinanceira,
        Integer campanha,
        LocalDate dataInicio,
        LocalDate dataConclusao,
        Integer numJogadores,
        String genero,
        Boolean controle,
        String imgThumb,

        @Schema(description = "ID do usuário dono do jogo", example = "3f9c2b14-7d4e-4f1c-9a3e-2bc8a1f0e9d7")
        UUID usuarioId
) {

    public static JogoResponse from(Jogo jogo) {
        return new JogoResponse(
                jogo.getId(),
                jogo.getTitulo(),
                jogo.getDescricao(),
                jogo.getMetaFinanceira(),
                jogo.getCampanha(),
                jogo.getDataInicio(),
                jogo.getDataConclusao(),
                jogo.getNumJogadores(),
                jogo.getGenero(),
                jogo.getControle(),
                jogo.getImgThumb(),
                jogo.getUsuarioId()
        );
    }
}
