package com.indiene.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "jogo")
@SQLDelete(sql = "UPDATE jogo SET deletado = true WHERE id = ?")
@SQLRestriction("deletado = false")
public class Jogo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "meta_financeira")
    private Double metaFinanceira;

    private Integer campanha;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_conclusao")
    private LocalDate dataConclusao;

    @Column(name = "num_jogadores")
    private Integer numJogadores;

    private String genero;

    private Boolean controle;

    @Column(name = "img_thumb")
    private String imgThumb;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;
}
