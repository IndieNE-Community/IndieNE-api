package com.indiene.repository;

import com.indiene.model.Imagem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImagemRepository extends JpaRepository<Imagem, Long> {

    List<Imagem> findByJogoId(Long jogoId);

    List<Imagem> findByPostagemId(Long postagemId);
}
