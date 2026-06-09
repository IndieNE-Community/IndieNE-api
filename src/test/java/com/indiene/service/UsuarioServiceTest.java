package com.indiene.service;

import com.indiene.dto.request.UsuarioCreateRequest;
import com.indiene.model.TipoUsuario;
import com.indiene.model.Usuario;
import com.indiene.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void criar_quandoEmailNaoExiste_salvaUsuarioComSenhaHasheada() {
        UsuarioCreateRequest request = new UsuarioCreateRequest(
                "Fulano", "fulano@x.com", "senha1234", TipoUsuario.DESENVOLVEDOR);

        when(usuarioRepository.existsByEmail("fulano@x.com")).thenReturn(false);
        when(passwordEncoder.encode("senha1234")).thenReturn("$2a$hash");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });

        Usuario resultado = usuarioService.criar(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario salvo = captor.getValue();

        assertThat(salvo.getNome()).isEqualTo("Fulano");
        assertThat(salvo.getEmail()).isEqualTo("fulano@x.com");
        assertThat(salvo.getSenha()).isEqualTo("$2a$hash");
        assertThat(salvo.getTipo()).isEqualTo(TipoUsuario.DESENVOLVEDOR);
        assertThat(resultado.getId()).isNotNull();
    }

    @Test
    void criar_quandoEmailJaExiste_lancaConflict() {
        UsuarioCreateRequest request = new UsuarioCreateRequest(
                "Fulano", "fulano@x.com", "senha1234", TipoUsuario.DESENVOLVEDOR);

        when(usuarioRepository.existsByEmail("fulano@x.com")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criar(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}
