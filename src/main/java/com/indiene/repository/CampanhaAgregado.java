package com.indiene.repository;

/** Projeção com os totais de doação agregados por jogo (usada na listagem do catálogo). */
public interface CampanhaAgregado {

    Long getJogoId();

    double getTotal();

    long getApoiadores();
}
