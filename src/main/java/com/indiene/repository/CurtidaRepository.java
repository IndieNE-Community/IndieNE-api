package com.indiene.repository;

import com.indiene.model.Curtida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CurtidaRepository extends JpaRepository<Curtida, Long> {

    Page<Curtida> findByPostagemId(Long postagemId, Pageable pageable);

    Page<Curtida> findByComentarioId(Long comentarioId, Pageable pageable);

    boolean existsByUsuarioIdAndPostagemId(UUID usuarioId, Long postagemId);

    boolean existsByUsuarioIdAndComentarioId(UUID usuarioId, Long comentarioId);

    long countByComentarioIdAndTipoIgnoreCase(Long comentarioId, String tipo);

    @Query("""
            SELECT c.comentarioId AS comentarioId, UPPER(c.tipo) AS tipo, COUNT(c) AS total
            FROM Curtida c
            WHERE c.comentarioId IN :comentarioIds
            GROUP BY c.comentarioId, UPPER(c.tipo)
            """)
    List<ReacaoAgregada> agregarReacoesPorComentarios(@Param("comentarioIds") Collection<Long> comentarioIds);
}
