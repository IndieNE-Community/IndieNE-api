package com.indiene.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Payload de atualização de jogo")
public record JogoUpdateRequest(
        @NotBlank @Size(max = 200) String titulo,

        @Size(max = 10_000) String descricao,

        @PositiveOrZero Double metaFinanceira,

        Integer campanha,

        LocalDate dataInicio,

        LocalDate dataConclusao,

        @PositiveOrZero Integer numJogadores,

        @Size(max = 100) String genero,

        Boolean controle,

        @Size(max = 500) String imgThumb
) {
}
