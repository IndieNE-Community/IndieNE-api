package com.indiene.service;

import com.indiene.dto.request.ComentarioCreateRequest;
import com.indiene.dto.request.ComentarioUpdateRequest;
import com.indiene.dto.response.ComentarioResponse;
import com.indiene.model.Comentario;
import com.indiene.repository.ComentarioRepository;
import com.indiene.repository.CurtidaRepository;
import com.indiene.repository.PostagemRepository;
import com.indiene.repository.ReacaoAgregada;
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

import java.time.LocalDateTime;
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
class ComentarioServiceTest {

    private static final UUID AUTOR = UUID.fromString("3f9c2b14-7d4e-4f1c-9a3e-2bc8a1f0e9d7");
    private static final UUID OUTRO = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private ComentarioRepository comentarioRepository;

    @Mock
    private PostagemRepository postagemRepository;

    @Mock
    private CurtidaRepository curtidaRepository;

    @InjectMocks
    private ComentarioService comentarioService;

    @Test
    void criar_persisteComentarioComAutorDoToken() {
        ComentarioCreateRequest request = new ComentarioCreateRequest("muito bom", 1L);
        when(postagemRepository.existsById(1L)).thenReturn(true);
        when(comentarioRepository.save(any(Comentario.class))).thenAnswer(invocation -> {
            Comentario c = invocation.getArgument(0);
            c.setId(7L);
            return c;
        });

        ComentarioResponse resposta = comentarioService.criar(request, AUTOR);

        ArgumentCaptor<Comentario> captor = ArgumentCaptor.forClass(Comentario.class);
        verify(comentarioRepository).save(captor.capture());
        Comentario salvo = captor.getValue();
        assertThat(salvo.getTexto()).isEqualTo("muito bom");
        assertThat(salvo.getPostagemId()).isEqualTo(1L);
        assertThat(salvo.getUsuarioId()).isEqualTo(AUTOR);
        assertThat(salvo.getData()).isNotNull();

        assertThat(resposta.id()).isEqualTo(7L);
        assertThat(resposta.likes()).isZero();
        assertThat(resposta.dislikes()).isZero();
    }

    @Test
    void criar_quandoPostagemNaoExiste_lancaNotFoundENaoSalva() {
        ComentarioCreateRequest request = new ComentarioCreateRequest("texto", 99L);
        when(postagemRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> comentarioService.criar(request, AUTOR))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        verify(comentarioRepository, never()).save(any());
    }

    @Test
    void buscarPorId_quandoNaoExiste_lancaNotFound() {
        when(comentarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> comentarioService.buscarPorId(99L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void buscarPorId_incluiContagemDeReacoes() {
        when(comentarioRepository.findById(1L)).thenReturn(Optional.of(comentario(1L, AUTOR)));
        when(curtidaRepository.countByComentarioIdAndTipoIgnoreCase(1L, "LIKE")).thenReturn(3L);
        when(curtidaRepository.countByComentarioIdAndTipoIgnoreCase(1L, "DISLIKE")).thenReturn(1L);

        ComentarioResponse resposta = comentarioService.buscarPorId(1L);

        assertThat(resposta.likes()).isEqualTo(3L);
        assertThat(resposta.dislikes()).isEqualTo(1L);
    }

    @Test
    void listarPorPostagem_agregaReacoesEmLote() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Comentario> pagina = new PageImpl<>(List.of(comentario(1L, AUTOR), comentario(2L, AUTOR)));
        when(comentarioRepository.findByPostagemId(1L, pageable)).thenReturn(pagina);
        when(curtidaRepository.agregarReacoesPorComentarios(any())).thenReturn(List.of(
                reacao(1L, "LIKE", 5L),
                reacao(1L, "DISLIKE", 2L)));

        Page<ComentarioResponse> resultado = comentarioService.listarPorPostagem(1L, pageable);

        assertThat(resultado.getContent()).hasSize(2);
        assertThat(resultado.getContent().get(0).likes()).isEqualTo(5L);
        assertThat(resultado.getContent().get(0).dislikes()).isEqualTo(2L);
        assertThat(resultado.getContent().get(1).likes()).isZero();
        assertThat(resultado.getContent().get(1).dislikes()).isZero();
    }

    @Test
    void atualizar_quandoAutor_atualizaTexto() {
        Comentario existente = comentario(1L, AUTOR);
        when(comentarioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(comentarioRepository.save(any(Comentario.class))).thenAnswer(i -> i.getArgument(0));
        when(curtidaRepository.countByComentarioIdAndTipoIgnoreCase(1L, "LIKE")).thenReturn(4L);
        when(curtidaRepository.countByComentarioIdAndTipoIgnoreCase(1L, "DISLIKE")).thenReturn(0L);

        ComentarioResponse resposta = comentarioService.atualizar(1L, new ComentarioUpdateRequest("novo texto"), AUTOR);

        assertThat(resposta.texto()).isEqualTo("novo texto");
        assertThat(resposta.likes()).isEqualTo(4L);
    }

    @Test
    void atualizar_quandoNaoAutor_lancaForbiddenENaoSalva() {
        Comentario existente = comentario(1L, AUTOR);
        when(comentarioRepository.findById(1L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> comentarioService.atualizar(1L, new ComentarioUpdateRequest("x"), OUTRO))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(comentarioRepository, never()).save(any());
    }

    @Test
    void deletar_quandoAutor_chamaDelete() {
        Comentario existente = comentario(1L, AUTOR);
        when(comentarioRepository.findById(1L)).thenReturn(Optional.of(existente));

        comentarioService.deletar(1L, AUTOR);

        verify(comentarioRepository).delete(existente);
    }

    @Test
    void deletar_quandoNaoAutor_lancaForbidden() {
        Comentario existente = comentario(1L, AUTOR);
        when(comentarioRepository.findById(1L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> comentarioService.deletar(1L, OUTRO))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(comentarioRepository, never()).delete(any());
    }

    private Comentario comentario(Long id, UUID autor) {
        return Comentario.builder()
                .id(id)
                .texto("texto")
                .data(LocalDateTime.now())
                .postagemId(1L)
                .usuarioId(autor)
                .build();
    }

    private ReacaoAgregada reacao(Long comentarioId, String tipo, long total) {
        return new ReacaoAgregada() {
            public Long getComentarioId() { return comentarioId; }
            public String getTipo() { return tipo; }
            public long getTotal() { return total; }
        };
    }
}
