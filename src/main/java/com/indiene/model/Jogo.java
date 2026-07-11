package com.indiene.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;
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

    /** Duração da campanha em dias (os dias restantes são derivados de dataInicio + campanha). */
    private Integer campanha;

    @Column(name = "data_inicio")
    private LocalDate dataInicio;

    @Column(name = "data_conclusao")
    private LocalDate dataConclusao;

    /** Avaliação/nota do jogo (0-100). */
    private Integer avaliacao;

    @Column(name = "num_jogadores_min")
    private Integer numJogadoresMin;

    @Column(name = "num_jogadores_max")
    private Integer numJogadoresMax;

    private Boolean controle;

    @Column(name = "img_thumb")
    private String imgThumb;

    @Column(name = "usuario_id", nullable = false)
    private UUID usuarioId;

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "jogo_generos", joinColumns = @JoinColumn(name = "jogo_id"))
    @Column(name = "genero")
    @BatchSize(size = 100)
    @Builder.Default
    private Set<String> generos = new LinkedHashSet<>();

    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "jogo_categorias", joinColumns = @JoinColumn(name = "jogo_id"))
    @Column(name = "categoria")
    @BatchSize(size = 100)
    @Builder.Default
    private Set<String> categorias = new LinkedHashSet<>();
}
