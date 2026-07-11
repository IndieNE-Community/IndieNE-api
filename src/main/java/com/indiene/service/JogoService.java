package com.indiene.service;

import com.indiene.dto.request.JogoCreateRequest;
import com.indiene.dto.request.JogoUpdateRequest;
import com.indiene.dto.response.JogoResponse;
import com.indiene.model.Jogo;
import com.indiene.model.Plataforma;
import com.indiene.model.Usuario;
import com.indiene.repository.CampanhaAgregado;
import com.indiene.repository.DoacaoRepository;
import com.indiene.repository.JogoRepository;
import com.indiene.repository.PlataformaRepository;
import com.indiene.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JogoService {

    private final JogoRepository jogoRepository;
    private final PlataformaRepository plataformaRepository;
    private final UsuarioRepository usuarioRepository;
    private final DoacaoRepository doacaoRepository;

    @Transactional
    public JogoResponse criar(JogoCreateRequest request, UUID autorId) {
        Jogo jogo = Jogo.builder()
                .titulo(request.titulo())
                .descricao(request.descricao())
                .metaFinanceira(request.metaFinanceira())
                .campanha(request.campanha())
                .dataInicio(request.dataInicio())
                .dataConclusao(request.dataConclusao())
                .avaliacao(request.avaliacao())
                .numJogadoresMin(request.numJogadoresMin())
                .numJogadoresMax(request.numJogadoresMax())
                .controle(request.controle())
                .imgThumb(request.imgThumb())
                .usuarioId(autorId)
                .generos(new LinkedHashSet<>(nullSafe(request.generos())))
                .categorias(new LinkedHashSet<>(nullSafe(request.categorias())))
                .build();

        Jogo salvo = jogoRepository.save(jogo);
        List<String> plataformas = sincronizarPlataformas(salvo.getId(), request.plataformas());
        return montarResponse(salvo, plataformas);
    }

    @Transactional(readOnly = true)
    public JogoResponse buscarPorId(Long id) {
        Jogo jogo = obterEntidade(id);
        List<String> plataformas = plataformaRepository.findByJogoId(id).stream()
                .map(Plataforma::getPlataforma)
                .toList();
        return montarResponse(jogo, plataformas);
    }

    @Transactional(readOnly = true)
    public Page<JogoResponse> listar(Pageable pageable) {
        Page<Jogo> pagina = jogoRepository.findAll(pageable);
        List<Jogo> jogos = pagina.getContent();
        if (jogos.isEmpty()) {
            return Page.empty(pageable);
        }

        List<Long> ids = jogos.stream().map(Jogo::getId).toList();
        Set<UUID> autores = jogos.stream().map(Jogo::getUsuarioId).collect(Collectors.toSet());

        Map<UUID, String> nomes = usuarioRepository.findAllById(autores).stream()
                .collect(Collectors.toMap(Usuario::getId, Usuario::getNome));
        Map<Long, List<String>> plataformas = plataformaRepository.findByJogoIdIn(ids).stream()
                .collect(Collectors.groupingBy(Plataforma::getJogoId,
                        Collectors.mapping(Plataforma::getPlataforma, Collectors.toList())));
        Map<Long, CampanhaAgregado> agregados = doacaoRepository.agregarPorJogos(ids).stream()
                .collect(Collectors.toMap(CampanhaAgregado::getJogoId, a -> a));

        return pagina.map(jogo -> {
            CampanhaAgregado agregado = agregados.get(jogo.getId());
            double total = agregado == null ? 0 : agregado.getTotal();
            long apoiadores = agregado == null ? 0 : agregado.getApoiadores();
            int percentual = calcularPercentual(total, jogo.getMetaFinanceira());
            Integer dias = calcularDiasRestantes(jogo.getDataInicio(), jogo.getCampanha());
            return JogoResponse.of(jogo, nomes.get(jogo.getUsuarioId()),
                    plataformas.getOrDefault(jogo.getId(), List.of()), total, apoiadores, percentual, dias);
        });
    }

    @Transactional
    public JogoResponse atualizar(Long id, JogoUpdateRequest request, UUID autorId) {
        Jogo jogo = obterEntidade(id);
        garantirDono(jogo, autorId);

        jogo.setTitulo(request.titulo());
        jogo.setDescricao(request.descricao());
        jogo.setMetaFinanceira(request.metaFinanceira());
        jogo.setCampanha(request.campanha());
        jogo.setDataInicio(request.dataInicio());
        jogo.setDataConclusao(request.dataConclusao());
        jogo.setAvaliacao(request.avaliacao());
        jogo.setNumJogadoresMin(request.numJogadoresMin());
        jogo.setNumJogadoresMax(request.numJogadoresMax());
        jogo.setControle(request.controle());
        jogo.setImgThumb(request.imgThumb());
        jogo.setGeneros(new LinkedHashSet<>(nullSafe(request.generos())));
        jogo.setCategorias(new LinkedHashSet<>(nullSafe(request.categorias())));

        Jogo salvo = jogoRepository.save(jogo);
        List<String> plataformas = sincronizarPlataformas(id, request.plataformas());
        return montarResponse(salvo, plataformas);
    }

    @Transactional
    public void deletar(Long id, UUID autorId) {
        Jogo jogo = obterEntidade(id);
        garantirDono(jogo, autorId);
        jogoRepository.delete(jogo);
    }

    private Jogo obterEntidade(Long id) {
        return jogoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Jogo não encontrado"));
    }

    /** Substitui todas as plataformas do jogo pelas informadas e devolve os nomes na ordem persistida. */
    private List<String> sincronizarPlataformas(Long jogoId, Set<String> plataformas) {
        plataformaRepository.deleteByJogoId(jogoId);
        Set<String> valores = new LinkedHashSet<>(nullSafe(plataformas));
        List<Plataforma> novas = valores.stream()
                .map(nome -> Plataforma.builder().plataforma(nome).jogoId(jogoId).build())
                .toList();
        plataformaRepository.saveAll(novas);
        return valores.stream().toList();
    }

    private JogoResponse montarResponse(Jogo jogo, List<String> plataformas) {
        String desenvolvedor = usuarioRepository.findById(jogo.getUsuarioId())
                .map(Usuario::getNome)
                .orElse(null);
        double total = doacaoRepository.somarValorPorJogo(jogo.getId());
        long apoiadores = doacaoRepository.contarApoiadoresPorJogo(jogo.getId());
        int percentual = calcularPercentual(total, jogo.getMetaFinanceira());
        Integer dias = calcularDiasRestantes(jogo.getDataInicio(), jogo.getCampanha());
        return JogoResponse.of(jogo, desenvolvedor, plataformas, total, apoiadores, percentual, dias);
    }

    private int calcularPercentual(double total, Double meta) {
        if (meta == null || meta <= 0) {
            return 0;
        }
        return (int) Math.min(100, Math.round(total / meta * 100));
    }

    private Integer calcularDiasRestantes(LocalDate dataInicio, Integer duracaoDias) {
        if (dataInicio == null || duracaoDias == null) {
            return null;
        }
        LocalDate fim = dataInicio.plusDays(duracaoDias);
        long dias = ChronoUnit.DAYS.between(LocalDate.now(), fim);
        return (int) Math.max(0, dias);
    }

    private void garantirDono(Jogo jogo, UUID autorId) {
        if (!jogo.getUsuarioId().equals(autorId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Operação permitida apenas ao dono do jogo");
        }
    }

    private static Collection<String> nullSafe(Set<String> valores) {
        return valores == null ? Set.of() : valores;
    }
}
