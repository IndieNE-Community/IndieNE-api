package com.indiene.service;

import com.indiene.dto.request.DoacaoCreateRequest;
import com.indiene.dto.response.CampanhaResumoResponse;
import com.indiene.dto.response.DoacaoResponse;
import com.indiene.model.Doacao;
import com.indiene.model.Jogo;
import com.indiene.model.Usuario;
import com.indiene.repository.DoacaoRepository;
import com.indiene.repository.JogoRepository;
import com.indiene.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DoacaoService {

    private final DoacaoRepository doacaoRepository;
    private final JogoRepository jogoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public Doacao criar(DoacaoCreateRequest request, UUID autorId) {
        buscarJogo(request.jogoId());

        Doacao doacao = Doacao.builder()
                .valor(request.valor())
                .data(LocalDateTime.now())
                .jogoId(request.jogoId())
                .usuarioId(autorId)
                .build();

        return doacaoRepository.save(doacao);
    }

    @Transactional(readOnly = true)
    public Doacao buscarPorId(Long id) {
        return doacaoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doação não encontrada"));
    }

    @Transactional(readOnly = true)
    public Page<DoacaoResponse> listarPorJogo(Long jogoId, Pageable pageable) {
        Page<Doacao> doacoes = doacaoRepository.findByJogoId(jogoId, pageable);

        List<UUID> usuarioIds = doacoes.getContent().stream()
                .map(Doacao::getUsuarioId)
                .distinct()
                .toList();

        Map<UUID, String> nomes = usuarioRepository.findAllById(usuarioIds).stream()
                .collect(Collectors.toMap(Usuario::getId, Usuario::getNome));

        return doacoes.map(d -> DoacaoResponse.from(d, nomes.get(d.getUsuarioId())));
    }

    @Transactional(readOnly = true)
    public CampanhaResumoResponse resumoPorJogo(Long jogoId) {
        Jogo jogo = buscarJogo(jogoId);
        double total = doacaoRepository.somarValorPorJogo(jogoId);
        long apoiadores = doacaoRepository.contarApoiadoresPorJogo(jogoId);
        int percentual = calcularPercentual(total, jogo.getMetaFinanceira());
        return new CampanhaResumoResponse(jogoId, jogo.getMetaFinanceira(), total, apoiadores, percentual);
    }

    @Transactional
    public void deletar(Long id, UUID autorId) {
        Doacao doacao = buscarPorId(id);
        if (!doacao.getUsuarioId().equals(autorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operação permitida apenas ao doador");
        }
        doacaoRepository.delete(doacao);
    }

    private Jogo buscarJogo(Long jogoId) {
        return jogoRepository.findById(jogoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jogo não encontrado"));
    }

    private int calcularPercentual(double total, Double meta) {
        if (meta == null || meta <= 0) {
            return 0;
        }
        return (int) Math.min(100, Math.round(total / meta * 100));
    }
}
