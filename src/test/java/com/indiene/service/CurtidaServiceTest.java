package com.indiene.service;

import com.indiene.dto.request.CurtidaCreateRequest;
import com.indiene.model.Curtida;
import com.indiene.repository.ComentarioRepository;
import com.indiene.repository.CurtidaRepository;
import com.indiene.repository.PostagemRepository;
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
class CurtidaServiceTest {

    private static final UUID AUTOR = UUID.fromString("3f9c2b14-7d4e-4f1c-9a3e-2bc8a1f0e9d7");
    private static final UUID OUTRO = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private CurtidaRepository curtidaRepository;

    @Mock
    private PostagemRepository postagemRepository;

    @Mock
    private ComentarioRepository comentarioRepository;

    @InjectMocks
    private CurtidaService curtidaService;

    @Test
    void criar_curtidaDePostagem_persisteComAutorDoToken() {
        CurtidaCreateRequest request = new CurtidaCreateRequest(1L, null, "LIKE");
        when(postagemRepository.existsById(1L)).thenReturn(true);
        when(curtidaRepository.existsByUsuarioIdAndPostagemId(AUTOR, 1L)).thenReturn(false);
        when(curtidaRepository.save(any(Curtida.class))).thenAnswer(invocation -> {
            Curtida c = invocation.getArgument(0);
            c.setId(7L);
            return c;
        });

        Curtida resultado = curtidaService.criar(request, AUTOR);

        ArgumentCaptor<Curtida> captor = ArgumentCaptor.forClass(Curtida.class);
        verify(curtidaRepository).save(captor.capture());
        Curtida salvo = captor.getValue();

        assertThat(salvo.getPostagemId()).isEqualTo(1L);
        assertThat(salvo.getComentarioId()).isNull();
        assertThat(salvo.getTipo()).isEqualTo("LIKE");
        assertThat(salvo.getUsuarioId()).isEqualTo(AUTOR);
        assertThat(resultado.getId()).isEqualTo(7L);
    }

    @Test
    void criar_curtidaDeComentario_persiste() {
        CurtidaCreateRequest request = new CurtidaCreateRequest(null, 5L, null);
        when(comentarioRepository.existsById(5L)).thenReturn(true);
        when(curtidaRepository.existsByUsuarioIdAndComentarioId(AUTOR, 5L)).thenReturn(false);
        when(curtidaRepository.save(any(Curtida.class))).thenAnswer(i -> i.getArgument(0));

        Curtida resultado = curtidaService.criar(request, AUTOR);

        assertThat(resultado.getComentarioId()).isEqualTo(5L);
        assertThat(resultado.getPostagemId()).isNull();
        assertThat(resultado.getUsuarioId()).isEqualTo(AUTOR);
    }

    @Test
    void criar_semNenhumAlvo_lancaBadRequest() {
        CurtidaCreateRequest request = new CurtidaCreateRequest(null, null, null);

        assertThatThrownBy(() -> curtidaService.criar(request, AUTOR))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(curtidaRepository, never()).save(any());
    }

    @Test
    void criar_comAmbosAlvos_lancaBadRequest() {
        CurtidaCreateRequest request = new CurtidaCreateRequest(1L, 5L, null);

        assertThatThrownBy(() -> curtidaService.criar(request, AUTOR))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(curtidaRepository, never()).save(any());
    }

    @Test
    void criar_quandoPostagemNaoExiste_lancaNotFound() {
        CurtidaCreateRequest request = new CurtidaCreateRequest(99L, null, null);
        when(postagemRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> curtidaService.criar(request, AUTOR))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        verify(curtidaRepository, never()).save(any());
    }

    @Test
    void criar_quandoJaCurtiuPostagem_lancaConflict() {
        CurtidaCreateRequest request = new CurtidaCreateRequest(1L, null, null);
        when(postagemRepository.existsById(1L)).thenReturn(true);
        when(curtidaRepository.existsByUsuarioIdAndPostagemId(AUTOR, 1L)).thenReturn(true);

        assertThatThrownBy(() -> curtidaService.criar(request, AUTOR))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(curtidaRepository, never()).save(any());
    }

    @Test
    void deletar_quandoAutor_chamaDelete() {
        Curtida existente = curtida(1L, AUTOR);
        when(curtidaRepository.findById(1L)).thenReturn(Optional.of(existente));

        curtidaService.deletar(1L, AUTOR);

        verify(curtidaRepository).delete(existente);
    }

    @Test
    void deletar_quandoNaoAutor_lancaForbidden() {
        Curtida existente = curtida(1L, AUTOR);
        when(curtidaRepository.findById(1L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> curtidaService.deletar(1L, OUTRO))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(curtidaRepository, never()).delete(any());
    }

    private Curtida curtida(Long id, UUID autor) {
        return Curtida.builder()
                .id(id)
                .tipo("LIKE")
                .postagemId(1L)
                .usuarioId(autor)
                .build();
    }
}
