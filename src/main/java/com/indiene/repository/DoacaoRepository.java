package com.indiene.repository;

import com.indiene.model.Doacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoacaoRepository extends JpaRepository<Doacao, Long> {

    Page<Doacao> findByJogoId(Long jogoId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(d.valor), 0) FROM Doacao d WHERE d.jogoId = :jogoId")
    double somarValorPorJogo(@Param("jogoId") Long jogoId);

    @Query("SELECT COUNT(DISTINCT d.usuarioId) FROM Doacao d WHERE d.jogoId = :jogoId")
    long contarApoiadoresPorJogo(@Param("jogoId") Long jogoId);
}
