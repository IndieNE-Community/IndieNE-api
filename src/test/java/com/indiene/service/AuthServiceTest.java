package com.indiene.service;

import com.indiene.dto.request.LoginRequest;
import com.indiene.dto.response.LoginResponse;
import com.indiene.model.TipoUsuario;
import com.indiene.model.Usuario;
import com.indiene.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
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
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    private Usuario usuarioExistente() {
        return Usuario.builder()
                .id(UUID.fromString("3f9c2b14-7d4e-4f1c-9a3e-2bc8a1f0e9d7"))
                .nome("Fulano")
                .email("fulano@x.com")
                .senha("$2a$hash")
                .tipo(TipoUsuario.DESENVOLVEDOR)
                .build();
    }

    @Test
    void login_quandoCredenciaisValidas_retornaTokenEUsuario() {
        Usuario usuario = usuarioExistente();
        LoginRequest request = new LoginRequest("fulano@x.com", "senha1234");

        when(usuarioRepository.findByEmail("fulano@x.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha1234", "$2a$hash")).thenReturn(true);
        when(jwtService.gerarToken(usuario)).thenReturn("token.jwt.assinado");
        when(jwtService.getExpirationMs()).thenReturn(86400000L);

        LoginResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("token.jwt.assinado");
        assertThat(response.expiresIn()).isEqualTo(86400000L);
        assertThat(response.usuario().id()).isEqualTo(usuario.getId());
        assertThat(response.usuario().email()).isEqualTo("fulano@x.com");
        assertThat(response.usuario().tipo()).isEqualTo(TipoUsuario.DESENVOLVEDOR);
    }

    @Test
    void login_quandoEmailNaoExiste_lancaUnauthorized() {
        LoginRequest request = new LoginRequest("inexistente@x.com", "senha1234");

        when(usuarioRepository.findByEmail("inexistente@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).gerarToken(any());
    }

    @Test
    void login_quandoSenhaInvalida_lancaUnauthorized() {
        Usuario usuario = usuarioExistente();
        LoginRequest request = new LoginRequest("fulano@x.com", "senhaErrada");

        when(usuarioRepository.findByEmail("fulano@x.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senhaErrada", "$2a$hash")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(ResponseStatusException.class)
                .extracting(e -> ((ResponseStatusException) e).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);

        verify(jwtService, never()).gerarToken(any());
    }
}
