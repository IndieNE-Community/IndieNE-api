package com.indiene.service;

import com.indiene.dto.request.JogoCreateRequest;
import com.indiene.dto.request.JogoUpdateRequest;
import com.indiene.model.Jogo;
import com.indiene.repository.JogoRepository;
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
public class JogoService {

    private final JogoRepository jogoRepository;

    @Transactional
    public Jogo criar(JogoCreateRequest request, UUID autorId) {
        Jogo jogo = Jogo.builder()
                .titulo(request.titulo())
                .descricao(request.descricao())
                .metaFinanceira(request.metaFinanceira())
                .campanha(request.campanha())
                .dataInicio(request.dataInicio())
                .dataConclusao(request.dataConclusao())
                .numJogadores(request.numJogadores())
                .genero(request.genero())
                .controle(request.controle())
                .imgThumb(request.imgThumb())
                .usuarioId(autorId)
                .build();

        return jogoRepository.save(jogo);
    }

    @Transactional(readOnly = true)
    public Jogo buscarPorId(Long id) {
        return jogoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jogo não encontrado"));
    }

    @Transactional(readOnly = true)
    public Page<Jogo> listar(Pageable pageable) {
        return jogoRepository.findAll(pageable);
    }

    @Transactional
    public Jogo atualizar(Long id, JogoUpdateRequest request, UUID autorId) {
        Jogo jogo = buscarPorId(id);
        garantirDono(jogo, autorId);

        jogo.setTitulo(request.titulo());
        jogo.setDescricao(request.descricao());
        jogo.setMetaFinanceira(request.metaFinanceira());
        jogo.setCampanha(request.campanha());
        jogo.setDataInicio(request.dataInicio());
        jogo.setDataConclusao(request.dataConclusao());
        jogo.setNumJogadores(request.numJogadores());
        jogo.setGenero(request.genero());
        jogo.setControle(request.controle());
        jogo.setImgThumb(request.imgThumb());

        return jogoRepository.save(jogo);
    }

    @Transactional
    public void deletar(Long id, UUID autorId) {
        Jogo jogo = buscarPorId(id);
        garantirDono(jogo, autorId);
        jogoRepository.delete(jogo);
    }

    private void garantirDono(Jogo jogo, UUID autorId) {
        if (!jogo.getUsuarioId().equals(autorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operação permitida apenas ao dono do jogo");
        }
    }
}
