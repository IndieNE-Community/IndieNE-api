package com.indiene.service;

import com.indiene.dto.request.PlataformaCreateRequest;
import com.indiene.model.Jogo;
import com.indiene.model.Plataforma;
import com.indiene.repository.JogoRepository;
import com.indiene.repository.PlataformaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlataformaService {

    private final PlataformaRepository plataformaRepository;
    private final JogoRepository jogoRepository;

    @Transactional
    public Plataforma criar(PlataformaCreateRequest request, UUID autorId) {
        Jogo jogo = buscarJogo(request.jogoId());
        garantirDono(jogo, autorId);

        if (plataformaRepository.existsByJogoIdAndPlataforma(request.jogoId(), request.plataforma())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Plataforma já cadastrada para este jogo");
        }

        Plataforma plataforma = Plataforma.builder()
                .plataforma(request.plataforma())
                .jogoId(request.jogoId())
                .build();

        return plataformaRepository.save(plataforma);
    }

    @Transactional(readOnly = true)
    public List<Plataforma> listarPorJogo(Long jogoId) {
        return plataformaRepository.findByJogoId(jogoId);
    }

    @Transactional
    public void deletar(Long id, UUID autorId) {
        Plataforma plataforma = plataformaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plataforma não encontrada"));
        Jogo jogo = buscarJogo(plataforma.getJogoId());
        garantirDono(jogo, autorId);
        plataformaRepository.delete(plataforma);
    }

    private Jogo buscarJogo(Long jogoId) {
        return jogoRepository.findById(jogoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jogo não encontrado"));
    }

    private void garantirDono(Jogo jogo, UUID autorId) {
        if (!jogo.getUsuarioId().equals(autorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operação permitida apenas ao dono do jogo");
        }
    }
}
