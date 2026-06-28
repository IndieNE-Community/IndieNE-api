package com.indiene.service;

import com.indiene.dto.request.ComentarioCreateRequest;
import com.indiene.dto.request.ComentarioUpdateRequest;
import com.indiene.model.Comentario;
import com.indiene.repository.ComentarioRepository;
import com.indiene.repository.PostagemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ComentarioService {

    private final ComentarioRepository comentarioRepository;
    private final PostagemRepository postagemRepository;

    @Transactional
    public Comentario criar(ComentarioCreateRequest request, UUID autorId) {
        if (!postagemRepository.existsById(request.postagemId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicação não encontrada");
        }

        Comentario comentario = Comentario.builder()
                .texto(request.texto())
                .data(LocalDateTime.now())
                .postagemId(request.postagemId())
                .usuarioId(autorId)
                .build();

        return comentarioRepository.save(comentario);
    }

    @Transactional(readOnly = true)
    public Comentario buscarPorId(Long id) {
        return comentarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentário não encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<Comentario> listarPorPostagem(Long postagemId, Pageable pageable) {
        return comentarioRepository.findByPostagemId(postagemId, pageable);
    }

    @Transactional
    public Comentario atualizar(Long id, ComentarioUpdateRequest request, UUID autorId) {
        Comentario comentario = buscarPorId(id);
        garantirAutor(comentario, autorId);

        comentario.setTexto(request.texto());

        return comentarioRepository.save(comentario);
    }

    @Transactional
    public void deletar(Long id, UUID autorId) {
        Comentario comentario = buscarPorId(id);
        garantirAutor(comentario, autorId);
        comentarioRepository.delete(comentario);
    }

    private void garantirAutor(Comentario comentario, UUID autorId) {
        if (!comentario.getUsuarioId().equals(autorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operação permitida apenas ao autor do comentário");
        }
    }
}
