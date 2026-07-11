package com.indiene.service;

import com.indiene.dto.request.ImagemCreateRequest;
import com.indiene.model.Imagem;
import com.indiene.model.Jogo;
import com.indiene.model.Postagem;
import com.indiene.repository.ImagemRepository;
import com.indiene.repository.JogoRepository;
import com.indiene.repository.PostagemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImagemService {

    private final ImagemRepository imagemRepository;
    private final JogoRepository jogoRepository;
    private final PostagemRepository postagemRepository;

    @Transactional
    public Imagem criar(ImagemCreateRequest request, UUID autorId) {
        boolean deJogo = request.jogoId() != null;
        boolean dePostagem = request.postagemId() != null;

        if (deJogo == dePostagem) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe exatamente um alvo: jogoId ou postagemId");
        }

        if (deJogo) {
            garantirDonoDoJogo(request.jogoId(), autorId);
        } else {
            garantirAutorDaPostagem(request.postagemId(), autorId);
        }

        Imagem imagem = Imagem.builder()
                .imagem(request.imagem())
                .jogoId(request.jogoId())
                .postagemId(request.postagemId())
                .build();

        return imagemRepository.save(imagem);
    }

    @Transactional(readOnly = true)
    public List<Imagem> listarPorJogo(Long jogoId) {
        return imagemRepository.findByJogoId(jogoId);
    }

    @Transactional(readOnly = true)
    public List<Imagem> listarPorPostagem(Long postagemId) {
        return imagemRepository.findByPostagemId(postagemId);
    }

    @Transactional
    public void deletar(Long id, UUID autorId) {
        Imagem imagem = imagemRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Imagem não encontrada"));

        if (imagem.getJogoId() != null) {
            garantirDonoDoJogo(imagem.getJogoId(), autorId);
        } else {
            garantirAutorDaPostagem(imagem.getPostagemId(), autorId);
        }

        imagemRepository.delete(imagem);
    }

    private void garantirDonoDoJogo(Long jogoId, UUID autorId) {
        Jogo jogo = jogoRepository.findById(jogoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jogo não encontrado"));
        if (!jogo.getUsuarioId().equals(autorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operação permitida apenas ao dono do jogo");
        }
    }

    private void garantirAutorDaPostagem(Long postagemId, UUID autorId) {
        Postagem postagem = postagemRepository.findById(postagemId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicação não encontrada"));
        if (!postagem.getUsuarioId().equals(autorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operação permitida apenas ao autor da publicação");
        }
    }
}
