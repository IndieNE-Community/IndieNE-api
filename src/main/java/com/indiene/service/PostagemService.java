package com.indiene.service;

import com.indiene.dto.request.PostagemCreateRequest;
import com.indiene.dto.request.PostagemUpdateRequest;
import com.indiene.model.Postagem;
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
public class PostagemService {

    private final PostagemRepository postagemRepository;

    @Transactional
    public Postagem criar(PostagemCreateRequest request, UUID autorId) {
        Postagem postagem = Postagem.builder()
                .titulo(request.titulo())
                .descricao(request.descricao())
                .data(LocalDateTime.now())
                .jogoId(request.jogoId())
                .usuarioId(autorId)
                .build();

        return postagemRepository.save(postagem);
    }

    @Transactional(readOnly = true)
    public Postagem buscarPorId(Long id) {
        return postagemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicação não encontrada"));
    }

    @Transactional(readOnly = true)
    public Page<Postagem> listar(Pageable pageable) {
        return postagemRepository.findAll(pageable);
    }

    @Transactional
    public Postagem atualizar(Long id, PostagemUpdateRequest request, UUID autorId) {
        Postagem postagem = buscarPorId(id);
        garantirAutor(postagem, autorId);

        postagem.setTitulo(request.titulo());
        postagem.setDescricao(request.descricao());

        return postagemRepository.save(postagem);
    }

    @Transactional
    public void deletar(Long id, UUID autorId) {
        Postagem postagem = buscarPorId(id);
        garantirAutor(postagem, autorId);
        postagemRepository.delete(postagem);
    }

    private void garantirAutor(Postagem postagem, UUID autorId) {
        if (!postagem.getUsuarioId().equals(autorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operação permitida apenas ao autor da publicação");
        }
    }
}
