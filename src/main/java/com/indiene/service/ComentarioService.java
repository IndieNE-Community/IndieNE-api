package com.indiene.service;

import com.indiene.dto.request.ComentarioCreateRequest;
import com.indiene.dto.request.ComentarioUpdateRequest;
import com.indiene.dto.response.ComentarioResponse;
import com.indiene.model.Comentario;
import com.indiene.repository.ComentarioRepository;
import com.indiene.repository.CurtidaRepository;
import com.indiene.repository.PostagemRepository;
import com.indiene.repository.ReacaoAgregada;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ComentarioService {

    private static final String LIKE = "LIKE";
    private static final String DISLIKE = "DISLIKE";

    private final ComentarioRepository comentarioRepository;
    private final PostagemRepository postagemRepository;
    private final CurtidaRepository curtidaRepository;

    @Transactional
    public ComentarioResponse criar(ComentarioCreateRequest request, UUID autorId) {
        if (!postagemRepository.existsById(request.postagemId())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicação não encontrada");
        }

        Comentario comentario = Comentario.builder()
                .texto(request.texto())
                .data(LocalDateTime.now())
                .postagemId(request.postagemId())
                .usuarioId(autorId)
                .build();

        Comentario salvo = comentarioRepository.save(comentario);
        return ComentarioResponse.of(salvo, 0, 0);
    }

    @Transactional(readOnly = true)
    public ComentarioResponse buscarPorId(Long id) {
        return montarComReacoes(obterEntidade(id));
    }

    @Transactional(readOnly = true)
    public Page<ComentarioResponse> listarPorPostagem(Long postagemId, Pageable pageable) {
        Page<Comentario> pagina = comentarioRepository.findByPostagemId(postagemId, pageable);
        List<Comentario> comentarios = pagina.getContent();
        if (comentarios.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> ids = comentarios.stream().map(Comentario::getId).toList();
        Map<Long, long[]> reacoes = new HashMap<>();
        for (ReacaoAgregada agregada : curtidaRepository.agregarReacoesPorComentarios(ids)) {
            long[] par = reacoes.computeIfAbsent(agregada.getComentarioId(), k -> new long[2]);
            if (LIKE.equals(agregada.getTipo())) {
                par[0] = agregada.getTotal();
            } else if (DISLIKE.equals(agregada.getTipo())) {
                par[1] = agregada.getTotal();
            }
        }

        return pagina.map(comentario -> {
            long[] par = reacoes.getOrDefault(comentario.getId(), new long[2]);
            return ComentarioResponse.of(comentario, par[0], par[1]);
        });
    }

    @Transactional
    public ComentarioResponse atualizar(Long id, ComentarioUpdateRequest request, UUID autorId) {
        Comentario comentario = obterEntidade(id);
        garantirAutor(comentario, autorId);

        comentario.setTexto(request.texto());

        return montarComReacoes(comentarioRepository.save(comentario));
    }

    @Transactional
    public void deletar(Long id, UUID autorId) {
        Comentario comentario = obterEntidade(id);
        garantirAutor(comentario, autorId);
        comentarioRepository.delete(comentario);
    }

    private Comentario obterEntidade(Long id) {
        return comentarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentário não encontrado"));
    }

    private ComentarioResponse montarComReacoes(Comentario comentario) {
        long likes = curtidaRepository.countByComentarioIdAndTipoIgnoreCase(comentario.getId(), LIKE);
        long dislikes = curtidaRepository.countByComentarioIdAndTipoIgnoreCase(comentario.getId(), DISLIKE);
        return ComentarioResponse.of(comentario, likes, dislikes);
    }

    private void garantirAutor(Comentario comentario, UUID autorId) {
        if (!comentario.getUsuarioId().equals(autorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operação permitida apenas ao autor do comentário");
        }
    }
}
