package com.indiene.model;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Jogo {
    private Long id;
    private String titulo;
    private String descricao;
    private Double metaFinanceira;
    private Integer campanha;
    private LocalDate dataInicio;
    private LocalDate dataConclusao;
    private Integer numJogadores;
    private String genero;
    private Boolean controle;
    private String imgThumb;
    private Long usuarioId;
}
