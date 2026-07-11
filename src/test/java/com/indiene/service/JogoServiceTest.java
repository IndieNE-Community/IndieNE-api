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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JogoServiceTest {

    private static final UUID AUTOR = UUID.fromString("3f9c2b14-7d4e-4f1c-9a3e-2bc8a1f0e9d7");
    private static final UUID OUTRO = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private JogoRepository jogoRepository;

    @Mock
    private PlataformaRepository plataformaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private DoacaoRepository doacaoRepository;

    @InjectMocks
    private JogoService jogoService;

    @Test
    void criar_persisteEEnriqueceResposta() {
        JogoCreateRequest request = new JogoCreateRequest(
                "God Breakers", "Um jogo legal", 100_000.0, 86,
                null, null, 87, 1, 4, true, "https://x/y.png",
                Set.of("Roguelike", "Ação"), Set.of("destaque"), Set.of("Windows", "Linux"));

        when(jogoRepository.save(any(Jogo.class))).thenAnswer(invocation -> {
            Jogo j = invocation.getArgument(0);
            j.setId(7L);
            return j;
        });
        when(usuarioRepository.findById(AUTOR)).thenReturn(Optional.of(usuario(AUTOR, "To The Sky")));
        when(doacaoRepository.somarValorPorJogo(7L)).thenReturn(50_000.0);
        when(doacaoRepository.contarApoiadoresPorJogo(7L)).thenReturn(1L);

        JogoResponse resposta = jogoService.criar(request, AUTOR);

        ArgumentCaptor<Jogo> captor = ArgumentCaptor.forClass(Jogo.class);
        verify(jogoRepository).save(captor.capture());
        Jogo salvo = captor.getValue();
        assertThat(salvo.getUsuarioId()).isEqualTo(AUTOR);
        assertThat(salvo.getGeneros()).containsExactlyInAnyOrder("Roguelike", "Ação");
        assertThat(salvo.getCategorias()).containsExactly("destaque");
        verify(plataformaRepository).deleteByJogoId(7L);

        assertThat(resposta.id()).isEqualTo(7L);
        assertThat(resposta.avaliacao()).isEqualTo(87);
        assertThat(resposta.numJogadoresMin()).isEqualTo(1);
        assertThat(resposta.numJogadoresMax()).isEqualTo(4);
        assertThat(resposta.desenvolvedor()).isEqualTo("To The Sky");
        assertThat(resposta.generos()).containsExactlyInAnyOrder("Roguelike", "Ação");
        assertThat(resposta.categorias()).containsExactly("destaque");
        assertThat(resposta.plataformas()).containsExactlyInAnyOrder("Windows", "Linux");
        assertThat(resposta.totalArrecadado()).isEqualTo(50_000.0);
        assertThat(resposta.apoiadores()).isEqualTo(1L);
        assertThat(resposta.metaPercentual()).isEqualTo(50);
    }

    @Test
    void criar_comColecoesNulas_persisteVazias() {
        JogoCreateRequest request = new JogoCreateRequest(
                "Minimal", null, null, null, null, null, null, null, null, null, null, null, null, null);

        when(jogoRepository.save(any(Jogo.class))).thenAnswer(invocation -> {
            Jogo j = invocation.getArgument(0);
            j.setId(1L);
            return j;
        });

        JogoResponse resposta = jogoService.criar(request, AUTOR);

        assertThat(resposta.titulo()).isEqualTo("Minimal");
        assertThat(resposta.generos()).isEmpty();
        assertThat(resposta.categorias()).isEmpty();
        assertThat(resposta.plataformas()).isEmpty();
        assertThat(resposta.metaPercentual()).isZero();
    }

    @Test
    void buscarPorId_quandoNaoExiste_lancaNotFound() {
        when(jogoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> jogoService.buscarPorId(99L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void buscarPorId_quandoExiste_enriquecComPlataformasEDesenvolvedor() {
        Jogo jogo = Jogo.builder().id(1L).titulo("God Breakers").metaFinanceira(100_000.0).usuarioId(AUTOR).build();
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(jogo));
        when(plataformaRepository.findByJogoId(1L)).thenReturn(List.of(
                Plataforma.builder().id(1L).plataforma("Windows").jogoId(1L).build()));
        when(usuarioRepository.findById(AUTOR)).thenReturn(Optional.of(usuario(AUTOR, "To The Sky")));
        when(doacaoRepository.somarValorPorJogo(1L)).thenReturn(25_000.0);
        when(doacaoRepository.contarApoiadoresPorJogo(1L)).thenReturn(3L);

        JogoResponse resposta = jogoService.buscarPorId(1L);

        assertThat(resposta.desenvolvedor()).isEqualTo("To The Sky");
        assertThat(resposta.plataformas()).containsExactly("Windows");
        assertThat(resposta.totalArrecadado()).isEqualTo(25_000.0);
        assertThat(resposta.apoiadores()).isEqualTo(3L);
        assertThat(resposta.metaPercentual()).isEqualTo(25);
    }

    @Test
    void listar_enriqueceEmLote() {
        Jogo jogo1 = Jogo.builder().id(1L).titulo("a").metaFinanceira(100_000.0).usuarioId(AUTOR).build();
        Jogo jogo2 = Jogo.builder().id(2L).titulo("b").usuarioId(AUTOR).build();
        Pageable pageable = PageRequest.of(0, 10);
        when(jogoRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(jogo1, jogo2)));
        when(usuarioRepository.findAllById(any())).thenReturn(List.of(usuario(AUTOR, "Dev")));
        when(plataformaRepository.findByJogoIdIn(any())).thenReturn(List.of(
                Plataforma.builder().id(1L).plataforma("Windows").jogoId(1L).build()));
        when(doacaoRepository.agregarPorJogos(any())).thenReturn(List.of(agregado(1L, 50_000.0, 2L)));

        Page<JogoResponse> resultado = jogoService.listar(pageable);

        assertThat(resultado.getContent()).hasSize(2);
        JogoResponse r1 = resultado.getContent().get(0);
        assertThat(r1.desenvolvedor()).isEqualTo("Dev");
        assertThat(r1.plataformas()).containsExactly("Windows");
        assertThat(r1.totalArrecadado()).isEqualTo(50_000.0);
        assertThat(r1.apoiadores()).isEqualTo(2L);
        assertThat(r1.metaPercentual()).isEqualTo(50);
        JogoResponse r2 = resultado.getContent().get(1);
        assertThat(r2.plataformas()).isEmpty();
        assertThat(r2.totalArrecadado()).isZero();
        assertThat(r2.metaPercentual()).isZero();
    }

    @Test
    void atualizar_quandoDono_atualizaCamposEColecoes() {
        Jogo existente = Jogo.builder().id(1L).titulo("antigo").usuarioId(AUTOR).build();
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(jogoRepository.save(any(Jogo.class))).thenAnswer(i -> i.getArgument(0));

        JogoUpdateRequest request = new JogoUpdateRequest(
                "novo", "desc", 5000.0, 30, null, null, 91, 2, 8, false, "https://x/z.png",
                Set.of("RPG"), Set.of("rpg"), Set.of("Mac"));

        JogoResponse resposta = jogoService.atualizar(1L, request, AUTOR);

        assertThat(resposta.titulo()).isEqualTo("novo");
        assertThat(resposta.avaliacao()).isEqualTo(91);
        assertThat(resposta.numJogadoresMin()).isEqualTo(2);
        assertThat(resposta.numJogadoresMax()).isEqualTo(8);
        assertThat(resposta.controle()).isFalse();
        assertThat(resposta.generos()).containsExactly("RPG");
        assertThat(resposta.categorias()).containsExactly("rpg");
        assertThat(resposta.plataformas()).containsExactly("Mac");
        verify(plataformaRepository).deleteByJogoId(1L);
    }

    @Test
    void atualizar_quandoNaoDono_lancaForbiddenENaoSalva() {
        Jogo existente = Jogo.builder().id(1L).titulo("antigo").usuarioId(AUTOR).build();
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(existente));

        JogoUpdateRequest request = new JogoUpdateRequest(
                "x", null, null, null, null, null, null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> jogoService.atualizar(1L, request, OUTRO))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(jogoRepository, never()).save(any());
        verify(plataformaRepository, never()).deleteByJogoId(any());
    }

    @Test
    void deletar_quandoDono_chamaDelete() {
        Jogo existente = Jogo.builder().id(1L).titulo("t").usuarioId(AUTOR).build();
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(existente));

        jogoService.deletar(1L, AUTOR);

        verify(jogoRepository).delete(existente);
    }

    @Test
    void deletar_quandoNaoDono_lancaForbidden() {
        Jogo existente = Jogo.builder().id(1L).titulo("t").usuarioId(AUTOR).build();
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> jogoService.deletar(1L, OUTRO))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(jogoRepository, never()).delete(any());
    }

    private Usuario usuario(UUID id, String nome) {
        return Usuario.builder().id(id).nome(nome).build();
    }

    private CampanhaAgregado agregado(Long jogoId, double total, long apoiadores) {
        return new CampanhaAgregado() {
            public Long getJogoId() { return jogoId; }
            public double getTotal() { return total; }
            public long getApoiadores() { return apoiadores; }
        };
    }
}
