package com.indiene.repository;

import com.indiene.model.Doacao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface DoacaoRepository extends JpaRepository<Doacao, Long> {

    Page<Doacao> findByJogoId(Long jogoId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(d.valor), 0) FROM Doacao d WHERE d.jogoId = :jogoId")
    double somarValorPorJogo(@Param("jogoId") Long jogoId);

    @Query("SELECT COUNT(DISTINCT d.usuarioId) FROM Doacao d WHERE d.jogoId = :jogoId")
    long contarApoiadoresPorJogo(@Param("jogoId") Long jogoId);

    @Query("""
            SELECT d.jogoId AS jogoId,
                   COALESCE(SUM(d.valor), 0) AS total,
                   COUNT(DISTINCT d.usuarioId) AS apoiadores
            FROM Doacao d
            WHERE d.jogoId IN :jogoIds
            GROUP BY d.jogoId
            """)
    List<CampanhaAgregado> agregarPorJogos(@Param("jogoIds") Collection<Long> jogoIds);
}
