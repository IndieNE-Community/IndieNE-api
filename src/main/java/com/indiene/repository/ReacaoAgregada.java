package com.indiene.repository;

/** Projeção com a contagem de curtidas de um comentário agrupada por tipo (LIKE/DISLIKE). */
public interface ReacaoAgregada {

    Long getComentarioId();

    String getTipo();

    long getTotal();
}
