package com.indiene.service;

import com.indiene.dto.request.PostagemCreateRequest;
import com.indiene.dto.request.PostagemUpdateRequest;
import com.indiene.model.Postagem;
import com.indiene.repository.PostagemRepository;
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
class PostagemServiceTest {

    private static final UUID AUTOR = UUID.fromString("3f9c2b14-7d4e-4f1c-9a3e-2bc8a1f0e9d7");
    private static final UUID OUTRO = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private PostagemRepository postagemRepository;

    @InjectMocks
    private PostagemService postagemService;

    @Test
    void criar_persistePostagemComAutorDoToken() {
        PostagemCreateRequest request = new PostagemCreateRequest("Devlog #1", "conteudo", 42L);

        when(postagemRepository.save(any(Postagem.class))).thenAnswer(invocation -> {
            Postagem p = invocation.getArgument(0);
            p.setId(7L);
            return p;
        });

        Postagem resultado = postagemService.criar(request, AUTOR);

        ArgumentCaptor<Postagem> captor = ArgumentCaptor.forClass(Postagem.class);
        verify(postagemRepository).save(captor.capture());
        Postagem salvo = captor.getValue();

        assertThat(salvo.getTitulo()).isEqualTo("Devlog #1");
        assertThat(salvo.getDescricao()).isEqualTo("conteudo");
        assertThat(salvo.getJogoId()).isEqualTo(42L);
        assertThat(salvo.getUsuarioId()).isEqualTo(AUTOR);
        assertThat(salvo.getData()).isNotNull();
        assertThat(resultado.getId()).isEqualTo(7L);
    }

    @Test
    void listar_repassaPageableEretornaPaginaDoRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Postagem> pagina = new PageImpl<>(List.of(postagem(1L, AUTOR), postagem(2L, AUTOR)));
        when(postagemRepository.findAll(pageable)).thenReturn(pagina);

        Page<Postagem> resultado = postagemService.listar(pageable);

        assertThat(resultado).isSameAs(pagina);
        assertThat(resultado.getTotalElements()).isEqualTo(2);
        verify(postagemRepository).findAll(pageable);
    }

    @Test
    void buscarPorId_quandoNaoExiste_lancaNotFound() {
        when(postagemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postagemService.buscarPorId(99L))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void atualizar_quandoAutor_atualizaCampos() {
        Postagem existente = postagem(1L, AUTOR);
        when(postagemRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(postagemRepository.save(any(Postagem.class))).thenAnswer(i -> i.getArgument(0));

        Postagem resultado = postagemService.atualizar(1L, new PostagemUpdateRequest("novo titulo", "novo texto"), AUTOR);

        assertThat(resultado.getTitulo()).isEqualTo("novo titulo");
        assertThat(resultado.getDescricao()).isEqualTo("novo texto");
        assertThat(resultado.getUsuarioId()).isEqualTo(AUTOR);
    }

    @Test
    void atualizar_quandoNaoAutor_lancaForbiddenENaoSalva() {
        Postagem existente = postagem(1L, AUTOR);
        when(postagemRepository.findById(1L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> postagemService.atualizar(1L, new PostagemUpdateRequest("x", "y"), OUTRO))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(postagemRepository, never()).save(any());
    }

    @Test
    void deletar_quandoAutor_chamaDelete() {
        Postagem existente = postagem(1L, AUTOR);
        when(postagemRepository.findById(1L)).thenReturn(Optional.of(existente));

        postagemService.deletar(1L, AUTOR);

        verify(postagemRepository).delete(existente);
    }

    @Test
    void deletar_quandoNaoAutor_lancaForbidden() {
        Postagem existente = postagem(1L, AUTOR);
        when(postagemRepository.findById(1L)).thenReturn(Optional.of(existente));

        assertThatThrownBy(() -> postagemService.deletar(1L, OUTRO))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(postagemRepository, never()).delete(any());
    }

    private Postagem postagem(Long id, UUID autor) {
        return Postagem.builder()
                .id(id)
                .titulo("titulo")
                .descricao("descricao")
                .data(LocalDateTime.now())
                .jogoId(42L)
                .usuarioId(autor)
                .build();
    }
}
