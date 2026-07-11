package com.indiene.repository;

import com.indiene.model.Plataforma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlataformaRepository extends JpaRepository<Plataforma, Long> {

    List<Plataforma> findByJogoId(Long jogoId);

    boolean existsByJogoIdAndPlataforma(Long jogoId, String plataforma);
}
