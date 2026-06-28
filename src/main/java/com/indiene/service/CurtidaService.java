package com.indiene.service;

import com.indiene.dto.request.CurtidaCreateRequest;
import com.indiene.model.Curtida;
import com.indiene.repository.ComentarioRepository;
import com.indiene.repository.CurtidaRepository;
import com.indiene.repository.PostagemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CurtidaService {

    private final CurtidaRepository curtidaRepository;
    private final PostagemRepository postagemRepository;
    private final ComentarioRepository comentarioRepository;

    @Transactional
    public Curtida criar(CurtidaCreateRequest request, UUID autorId) {
        boolean curteuPostagem = request.postagemId() != null;
        boolean curteuComentario = request.comentarioId() != null;

        if (curteuPostagem == curteuComentario) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe exatamente um alvo: postagemId ou comentarioId");
        }

        if (curteuPostagem) {
            if (!postagemRepository.existsById(request.postagemId())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicação não encontrada");
            }
            if (curtidaRepository.existsByUsuarioIdAndPostagemId(autorId, request.postagemId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Publicação já curtida por este usuário");
            }
        } else {
            if (!comentarioRepository.existsById(request.comentarioId())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentário não encontrado");
            }
            if (curtidaRepository.existsByUsuarioIdAndComentarioId(autorId, request.comentarioId())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Comentário já curtido por este usuário");
            }
        }

        Curtida curtida = Curtida.builder()
                .tipo(request.tipo())
                .postagemId(request.postagemId())
                .comentarioId(request.comentarioId())
                .usuarioId(autorId)
                .build();

        return curtidaRepository.save(curtida);
    }

    @Transactional(readOnly = true)
    public Curtida buscarPorId(Long id) {
        return curtidaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Curtida não encontrada"));
    }

    @Transactional(readOnly = true)
    public Page<Curtida> listarPorPostagem(Long postagemId, Pageable pageable) {
        return curtidaRepository.findByPostagemId(postagemId, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Curtida> listarPorComentario(Long comentarioId, Pageable pageable) {
        return curtidaRepository.findByComentarioId(comentarioId, pageable);
    }

    @Transactional
    public void deletar(Long id, UUID autorId) {
        Curtida curtida = buscarPorId(id);
        if (!curtida.getUsuarioId().equals(autorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operação permitida apenas ao autor da curtida");
        }
        curtidaRepository.delete(curtida);
    }
}
