package com.indiene.service;

import com.indiene.dto.request.JogoCreateRequest;
import com.indiene.dto.request.JogoUpdateRequest;
import com.indiene.model.Jogo;
import com.indiene.repository.JogoRepository;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
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

    @InjectMocks
    private JogoService jogoService;

    @Test
    void criar_persisteJogoComDonoDoToken() {
        JogoCreateRequest request = new JogoCreateRequest(
                "Indie Quest", "Um jogo legal", 10_000.0, 1,
                LocalDate.of(2026, 1, 15), LocalDate.of(2026, 12, 31),
                4, "RPG", true, "https://x/y.png");

        when(jogoRepository.save(any(Jogo.class))).thenAnswer(invocation -> {
            Jogo j = invocation.getArgument(0);
            j.setId(7L);
            return j;
        });

        Jogo resultado = jogoService.criar(request, AUTOR);

        ArgumentCaptor<Jogo> captor = ArgumentCaptor.forClass(Jogo.class);
        verify(jogoRepository).save(captor.capture());
        Jogo salvo = captor.getValue();

        assertThat(salvo.getTitulo()).isEqualTo("Indie Quest");
        assertThat(salvo.getUsuarioId()).isEqualTo(AUTOR);
        assertThat(resultado.getId()).isEqualTo(7L);
    }

    @Test
    void criar_comCamposOpcionaisNulos_persisteSomenteOsObrigatorios() {
        JogoCreateRequest request = new JogoCreateRequest(
                "Minimal", null, null, null, null, null, null, null, null, null);

        when(jogoRepository.save(any(Jogo.class))).thenAnswer(invocation -> {
            Jogo j = invocation.getArgument(0);
            j.setId(1L);
            return j;
        });

        Jogo resultado = jogoService.criar(request, AUTOR);

        assertThat(resultado.getTitulo()).isEqualTo("Minimal");
        assertThat(resultado.getDescricao()).isNull();
        assertThat(resultado.getMetaFinanceira()).isNull();
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
    void listar_repassaPageableEretornaPaginaDoRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Jogo> pagina = new PageImpl<>(List.of(jogo(1L, AUTOR), jogo(2L, AUTOR)));
        when(jogoRepository.findAll(pageable)).thenReturn(pagina);

        Page<Jogo> resultado = jogoService.listar(pageable);

        assertThat(resultado).isSameAs(pagina);
        verify(jogoRepository).findAll(pageable);
    }

    @Test
    void atualizar_quandoDono_atualizaTodosCampos() {
        Jogo existente = jogo(1L, AUTOR);
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(jogoRepository.save(any(Jogo.class))).thenAnswer(i -> i.getArgument(0));

        JogoUpdateRequest request = new JogoUpdateRequest(
                "novo", "desc", 5.0, 2,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 3, 1),
                8, "ACT", false, "https://x/z.png");

        Jogo resultado = jogoService.atualizar(1L, request, AUTOR);

        assertThat(resultado.getTitulo()).isEqualTo("novo");
        assertThat(resultado.getNumJogadores()).isEqualTo(8);
        assertThat(resultado.getControle()).isFalse();
        assertThat(resultado.getUsuarioId()).isEqualTo(AUTOR);
    }

    @Test
    void atualizar_quandoNaoDono_lancaForbiddenENaoSalva() {
        Jogo existente = jogo(1L, AUTOR);
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(existente));

        JogoUpdateRequest request = new JogoUpdateRequest(
                "x", null, null, null, null, null, null, null, null, null);

        assertThatThrownBy(() -> jogoService.atualizar(1L, request, OUTRO))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(jogoRepository, never()).save(any());
    }

    @Test
    void deletar_quandoDono_chamaDeleteNoRepository() {
        Jogo existente = jogo(1L, AUTOR);
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(existente));

        jogoService.deletar(1L, AUTOR);

        verify(jogoRepository).delete(existente);
    }

    @Test
    void deletar_quandoNaoDono_lancaForbidden() {
        Jogo existente = jogo(1L, AUTOR);
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> jogoService.deletar(1L, OUTRO))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(jogoRepository, never()).delete(any());
    }

    private Jogo jogo(Long id, UUID dono) {
        return Jogo.builder()
                .id(id)
                .titulo("titulo")
                .usuarioId(dono)
                .build();
    }
}
