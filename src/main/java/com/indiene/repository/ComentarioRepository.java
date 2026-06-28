package com.indiene.repository;

import com.indiene.model.Comentario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ComentarioRepository extends JpaRepository<Comentario, Long> {

    Page<Comentario> findByPostagemId(Long postagemId, Pageable pageable);
}
