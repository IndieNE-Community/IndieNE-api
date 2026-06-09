package com.indiene.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indiene.dto.request.JogoCreateRequest;
import com.indiene.dto.request.JogoUpdateRequest;
import com.indiene.model.Jogo;
import com.indiene.service.JogoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(JogoController.class)
@Import(JogoControllerTest.PermissiveSecurity.class)
class JogoControllerTest {

    private static final UUID AUTOR = UUID.fromString("3f9c2b14-7d4e-4f1c-9a3e-2bc8a1f0e9d7");

    @TestConfiguration
    @EnableWebSecurity
    static class PermissiveSecurity {
        @Bean
        SecurityFilterChain testSecurity(HttpSecurity http) throws Exception {
            return http
                    .csrf(AbstractHttpConfigurer::disable)
                    .authorizeHttpRequests(a -> a.anyRequest().permitAll())
                    .build();
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JogoService jogoService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static Authentication authFor(UUID id) {
        return new UsernamePasswordAuthenticationToken(id, null, List.of());
    }

    private Jogo jogo(Long id, String titulo) {
        return Jogo.builder()
                .id(id)
                .titulo(titulo)
                .usuarioId(AUTOR)
                .build();
    }

    @Test
    void criar_comPayloadValido_retorna201ComLocationECorpo() throws Exception {
        Jogo salvo = jogo(7L, "Indie Quest");
        when(jogoService.criar(any(JogoCreateRequest.class), eq(AUTOR))).thenReturn(salvo);

        JogoCreateRequest request = new JogoCreateRequest(
                "Indie Quest", null, null, null, null, null, null, null, null, null);

        mockMvc.perform(post("/jogos")
                        .with(authentication(authFor(AUTOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/jogos/7")))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.usuarioId").value(AUTOR.toString()));
    }

    @Test
    void criar_comTituloEmBranco_retorna400() throws Exception {
        String payload = """
                {"titulo":""}
                """;

        mockMvc.perform(post("/jogos")
                        .with(authentication(authFor(AUTOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criar_comMetaFinanceiraNegativa_retorna400() throws Exception {
        String payload = """
                {"titulo":"Indie Quest","metaFinanceira":-1.0}
                """;

        mockMvc.perform(post("/jogos")
                        .with(authentication(authFor(AUTOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscarPorId_quandoExiste_retorna200() throws Exception {
        when(jogoService.buscarPorId(7L)).thenReturn(jogo(7L, "Indie Quest"));

        mockMvc.perform(get("/jogos/7").with(authentication(authFor(AUTOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void buscarPorId_quandoNaoExiste_retorna404() throws Exception {
        when(jogoService.buscarPorId(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Jogo não encontrado"));

        mockMvc.perform(get("/jogos/99").with(authentication(authFor(AUTOR))))
                .andExpect(status().isNotFound());
    }

    @Test
    void listar_retornaPaginaDeResponses() throws Exception {
        Page<Jogo> page = new PageImpl<>(List.of(jogo(1L, "a"), jogo(2L, "b")));
        when(jogoService.listar(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/jogos").with(authentication(authFor(AUTOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void atualizar_quandoDono_retorna200() throws Exception {
        Jogo atualizado = jogo(7L, "novo");
        when(jogoService.atualizar(eq(7L), any(JogoUpdateRequest.class), eq(AUTOR)))
                .thenReturn(atualizado);

        JogoUpdateRequest request = new JogoUpdateRequest(
                "novo", null, null, null, null, null, null, null, null, null);

        mockMvc.perform(put("/jogos/7")
                        .with(authentication(authFor(AUTOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("novo"));
    }

    @Test
    void atualizar_quandoNaoDono_retorna403() throws Exception {
        when(jogoService.atualizar(eq(7L), any(JogoUpdateRequest.class), any(UUID.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Operação permitida apenas ao dono do jogo"));

        JogoUpdateRequest request = new JogoUpdateRequest(
                "novo", null, null, null, null, null, null, null, null, null);

        mockMvc.perform(put("/jogos/7")
                        .with(authentication(authFor(AUTOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deletar_quandoDono_retorna204() throws Exception {
        mockMvc.perform(delete("/jogos/7").with(authentication(authFor(AUTOR))))
                .andExpect(status().isNoContent());

        verify(jogoService).deletar(7L, AUTOR);
    }
}
