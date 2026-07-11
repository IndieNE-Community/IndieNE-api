package com.indiene.service;

import com.indiene.dto.request.ImagemCreateRequest;
import com.indiene.model.Imagem;
import com.indiene.model.Jogo;
import com.indiene.model.Postagem;
import com.indiene.repository.ImagemRepository;
import com.indiene.repository.JogoRepository;
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
class ImagemServiceTest {

    private static final UUID DONO = UUID.fromString("3f9c2b14-7d4e-4f1c-9a3e-2bc8a1f0e9d7");
    private static final UUID OUTRO = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock
    private ImagemRepository imagemRepository;

    @Mock
    private JogoRepository jogoRepository;

    @Mock
    private PostagemRepository postagemRepository;

    @InjectMocks
    private ImagemService imagemService;

    @Test
    void criar_imagemDeJogo_comDono_persiste() {
        ImagemCreateRequest request = new ImagemCreateRequest(1L, null, "https://x/y.png");
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(jogo(1L, DONO)));
        when(imagemRepository.save(any(Imagem.class))).thenAnswer(i -> {
            Imagem img = i.getArgument(0);
            img.setId(3L);
            return img;
        });

        Imagem resultado = imagemService.criar(request, DONO);

        ArgumentCaptor<Imagem> captor = ArgumentCaptor.forClass(Imagem.class);
        verify(imagemRepository).save(captor.capture());
        assertThat(captor.getValue().getJogoId()).isEqualTo(1L);
        assertThat(captor.getValue().getPostagemId()).isNull();
        assertThat(captor.getValue().getImagem()).isEqualTo("https://x/y.png");
        assertThat(resultado.getId()).isEqualTo(3L);
    }

    @Test
    void criar_imagemDePostagem_comAutor_persiste() {
        ImagemCreateRequest request = new ImagemCreateRequest(null, 5L, "https://x/y.png");
        when(postagemRepository.findById(5L)).thenReturn(Optional.of(postagem(5L, DONO)));
        when(imagemRepository.save(any(Imagem.class))).thenAnswer(i -> i.getArgument(0));

        Imagem resultado = imagemService.criar(request, DONO);

        assertThat(resultado.getPostagemId()).isEqualTo(5L);
        assertThat(resultado.getJogoId()).isNull();
    }

    @Test
    void criar_semAlvo_lancaBadRequest() {
        ImagemCreateRequest request = new ImagemCreateRequest(null, null, "https://x/y.png");

        assertThatThrownBy(() -> imagemService.criar(request, DONO))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(imagemRepository, never()).save(any());
    }

    @Test
    void criar_comAmbosAlvos_lancaBadRequest() {
        ImagemCreateRequest request = new ImagemCreateRequest(1L, 5L, "https://x/y.png");

        assertThatThrownBy(() -> imagemService.criar(request, DONO))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);

        verify(imagemRepository, never()).save(any());
    }

    @Test
    void criar_quandoJogoNaoExiste_lancaNotFound() {
        ImagemCreateRequest request = new ImagemCreateRequest(99L, null, "https://x/y.png");
        when(jogoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> imagemService.criar(request, DONO))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void criar_quandoNaoDonoDoJogo_lancaForbidden() {
        ImagemCreateRequest request = new ImagemCreateRequest(1L, null, "https://x/y.png");
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(jogo(1L, DONO)));

        assertThatThrownBy(() -> imagemService.criar(request, OUTRO))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(imagemRepository, never()).save(any());
    }

    @Test
    void criar_quandoNaoAutorDaPostagem_lancaForbidden() {
        ImagemCreateRequest request = new ImagemCreateRequest(null, 5L, "https://x/y.png");
        when(postagemRepository.findById(5L)).thenReturn(Optional.of(postagem(5L, DONO)));

        assertThatThrownBy(() -> imagemService.criar(request, OUTRO))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        verify(imagemRepository, never()).save(any());
    }

    @Test
    void deletar_imagemDeJogo_dono_chamaDelete() {
        Imagem existente = Imagem.builder().id(3L).imagem("https://x/y.png").jogoId(1L).build();
        when(imagemRepository.findById(3L)).thenReturn(Optional.of(existente));
        when(jogoRepository.findById(1L)).thenReturn(Optional.of(jogo(1L, DONO)));

        imagemService.deletar(3L, DONO);

        verify(imagemRepository).delete(existente);
    }

    @Test
    void deletar_quandoNaoExiste_lancaNotFound() {
        when(imagemRepository.findById(3L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> imagemService.deletar(3L, DONO))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    private Jogo jogo(Long id, UUID dono) {
        return Jogo.builder().id(id).titulo("Jogo").usuarioId(dono).build();
    }

    private Postagem postagem(Long id, UUID autor) {
        return Postagem.builder().id(id).titulo("Post").jogoId(1L).usuarioId(autor).build();
    }
}
