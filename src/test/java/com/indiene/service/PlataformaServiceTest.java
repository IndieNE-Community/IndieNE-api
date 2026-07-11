package com.indiene.service;

import com.indiene.dto.request.PlataformaCreateRequest;
import com.indiene.model.Jogo;
import com.indiene.model.Plataforma;
import com.indiene.repository.JogoRepository;
import com.indiene.repository.PlataformaRepository;
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
class PlataformaServiceTest {

    private static final UUID DONO = UUID.fromString("3f9c2b14-7d4e-4f1c-9a3e-2bc8a1f0e9d7");
    private static final UUID OUTRO = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private PlataformaRepository plataformaRepository;

    @Mock
    private JogoRepository jogoRepository;

    @InjectMocks
    private PlataformaService plataformaService;

    @Test
    void criar_comDono_persiste() {
        PlataformaCreateRequest request = new PlataformaCreateRequest(1L, "Windows");
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(jogo(1L, DONO)));
        when(plataformaRepository.existsByJogoIdAndPlataforma(1L, "Windows")).thenReturn(false);
        when(plataformaRepository.save(any(Plataforma.class))).thenAnswer(invocation -> {
            Plataforma p = invocation.getArgument(0);
            p.setId(9L);
            return p;
        });

        Plataforma resultado = plataformaService.criar(request, DONO);

        ArgumentCaptor<Plataforma> captor = ArgumentCaptor.forClass(Plataforma.class);
        verify(plataformaRepository).save(captor.capture());
        assertThat(captor.getValue().getPlataforma()).isEqualTo("Windows");
        assertThat(captor.getValue().getJogoId()).isEqualTo(1L);
        assertThat(resultado.getId()).isEqualTo(9L);
    }

    @Test
    void criar_quandoJogoNaoExiste_lancaNotFound() {
        PlataformaCreateRequest request = new PlataformaCreateRequest(99L, "Windows");
        when(jogoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> plataformaService.criar(request, DONO))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        verify(plataformaRepository, never()).save(any());
    }

    @Test
    void criar_quandoNaoDono_lancaForbidden() {
        PlataformaCreateRequest request = new PlataformaCreateRequest(1L, "Windows");
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(jogo(1L, DONO)));

        assertThatThrownBy(() -> plataformaService.criar(request, OUTRO))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(plataformaRepository, never()).save(any());
    }

    @Test
    void criar_quandoDuplicada_lancaConflict() {
        PlataformaCreateRequest request = new PlataformaCreateRequest(1L, "Windows");
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(jogo(1L, DONO)));
        when(plataformaRepository.existsByJogoIdAndPlataforma(1L, "Windows")).thenReturn(true);

        assertThatThrownBy(() -> plataformaService.criar(request, DONO))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(plataformaRepository, never()).save(any());
    }

    @Test
    void deletar_quandoDono_chamaDelete() {
        Plataforma existente = Plataforma.builder().id(5L).plataforma("Linux").jogoId(1L).build();
        when(plataformaRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(jogo(1L, DONO)));

        plataformaService.deletar(5L, DONO);

        verify(plataformaRepository).delete(existente);
    }

    @Test
    void deletar_quandoNaoDono_lancaForbidden() {
        Plataforma existente = Plataforma.builder().id(5L).plataforma("Linux").jogoId(1L).build();
        when(plataformaRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(jogo(1L, DONO)));

        assertThatThrownBy(() -> plataformaService.deletar(5L, OUTRO))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(plataformaRepository, never()).delete(any());
    }

    @Test
    void deletar_quandoNaoExiste_lancaNotFound() {
        when(plataformaRepository.findById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> plataformaService.deletar(5L, DONO))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Jogo jogo(Long id, UUID dono) {
        return Jogo.builder().id(id).titulo("Jogo").usuarioId(dono).build();
    }
}
