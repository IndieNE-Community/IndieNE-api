package com.indiene.service;

import com.indiene.dto.request.DoacaoCreateRequest;
import com.indiene.dto.response.CampanhaResumoResponse;
import com.indiene.model.Doacao;
import com.indiene.model.Jogo;
import com.indiene.repository.DoacaoRepository;
import com.indiene.repository.JogoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoacaoServiceTest {

    private static final UUID DOADOR = UUID.fromString("3f9c2b14-7d4e-4f1c-9a3e-2bc8a1f0e9d7");
    private static final UUID OUTRO = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private DoacaoRepository doacaoRepository;

    @Mock
    private JogoRepository jogoRepository;

    @InjectMocks
    private DoacaoService doacaoService;

    @Test
    void criar_comJogoExistente_persisteComDoadorEData() {
        DoacaoCreateRequest request = new DoacaoCreateRequest(1L, 50.0);
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(jogo(1L, 100000.0)));
        when(doacaoRepository.save(any(Doacao.class))).thenAnswer(i -> {
            Doacao d = i.getArgument(0);
            d.setId(7L);
            return d;
        });

        Doacao resultado = doacaoService.criar(request, DOADOR);

        ArgumentCaptor<Doacao> captor = ArgumentCaptor.forClass(Doacao.class);
        verify(doacaoRepository).save(captor.capture());
        assertThat(captor.getValue().getValor()).isEqualTo(50.0);
        assertThat(captor.getValue().getJogoId()).isEqualTo(1L);
        assertThat(captor.getValue().getUsuarioId()).isEqualTo(DOADOR);
        assertThat(captor.getValue().getData()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(7L);
    }

    @Test
    void criar_quandoJogoNaoExiste_lancaNotFound() {
        DoacaoCreateRequest request = new DoacaoCreateRequest(99L, 50.0);
        when(jogoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> doacaoService.criar(request, DOADOR))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        verify(doacaoRepository, never()).save(any());
    }

    @Test
    void resumo_calculaTotalApoiadoresEPercentual() {
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(jogo(1L, 100000.0)));
        when(doacaoRepository.somarValorPorJogo(1L)).thenReturn(50000.0);
        when(doacaoRepository.contarApoiadoresPorJogo(1L)).thenReturn(3L);

        CampanhaResumoResponse resumo = doacaoService.resumoPorJogo(1L);

        assertThat(resumo.jogoId()).isEqualTo(1L);
        assertThat(resumo.metaFinanceira()).isEqualTo(100000.0);
        assertThat(resumo.totalArrecadado()).isEqualTo(50000.0);
        assertThat(resumo.apoiadores()).isEqualTo(3L);
        assertThat(resumo.metaPercentual()).isEqualTo(50);
    }

    @Test
    void resumo_semMeta_percentualZero() {
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(jogo(1L, null)));
        when(doacaoRepository.somarValorPorJogo(1L)).thenReturn(500.0);
        when(doacaoRepository.contarApoiadoresPorJogo(1L)).thenReturn(1L);

        CampanhaResumoResponse resumo = doacaoService.resumoPorJogo(1L);

        assertThat(resumo.metaPercentual()).isZero();
    }

    @Test
    void resumo_acimaDaMeta_percentualLimitadoA100() {
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(jogo(1L, 1000.0)));
        when(doacaoRepository.somarValorPorJogo(1L)).thenReturn(5000.0);
        when(doacaoRepository.contarApoiadoresPorJogo(1L)).thenReturn(10L);

        CampanhaResumoResponse resumo = doacaoService.resumoPorJogo(1L);

        assertThat(resumo.metaPercentual()).isEqualTo(100);
    }

    @Test
    void deletar_quandoDoador_chamaDelete() {
        Doacao existente = Doacao.builder().id(7L).valor(50.0).jogoId(1L).usuarioId(DOADOR).build();
        when(doacaoRepository.findById(7L)).thenReturn(Optional.of(existente));

        doacaoService.deletar(7L, DOADOR);

        verify(doacaoRepository).delete(existente);
    }

    @Test
    void deletar_quandoNaoDoador_lancaForbidden() {
        Doacao existente = Doacao.builder().id(7L).valor(50.0).jogoId(1L).usuarioId(DOADOR).build();
        when(doacaoRepository.findById(7L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> doacaoService.deletar(7L, OUTRO))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(doacaoRepository, never()).delete(any());
    }

    private Jogo jogo(Long id, Double meta) {
        return Jogo.builder().id(id).titulo("Jogo").metaFinanceira(meta).usuarioId(DOADOR).build();
    }
}
