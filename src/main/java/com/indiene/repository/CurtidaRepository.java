package com.indiene.repository;

import com.indiene.model.Curtida;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CurtidaRepository extends JpaRepository<Curtida, Long> {

    Page<Curtida> findByPostagemId(Long postagemId, Pageable pageable);

    Page<Curtida> findByComentarioId(Long comentarioId, Pageable pageable);

    boolean existsByUsuarioIdAndPostagemId(UUID usuarioId, Long postagemId);

    boolean existsByUsuarioIdAndComentarioId(UUID usuarioId, Long comentarioId);
}
