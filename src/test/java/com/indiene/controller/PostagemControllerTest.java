package com.indiene.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indiene.dto.request.PostagemCreateRequest;
import com.indiene.dto.request.PostagemUpdateRequest;
import com.indiene.model.Postagem;
import com.indiene.service.PostagemService;
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

import java.time.LocalDateTime;
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

@WebMvcTest(PostagemController.class)
@Import(PostagemControllerTest.PermissiveSecurity.class)
class PostagemControllerTest {

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
    private PostagemService postagemService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static Authentication authFor(UUID id) {
        return new UsernamePasswordAuthenticationToken(id, null, List.of());
    }

    @Test
    void criar_comPayloadValido_retorna201ComLocationECorpo() throws Exception {
        Postagem salvo = Postagem.builder()
                .id(7L)
                .titulo("Devlog #1")
                .descricao("conteudo")
                .data(LocalDateTime.now())
                .jogoId(42L)
                .usuarioId(AUTOR)
                .build();

        when(postagemService.criar(any(PostagemCreateRequest.class), eq(AUTOR))).thenReturn(salvo);

        PostagemCreateRequest request = new PostagemCreateRequest("Devlog #1", "conteudo", 42L);

        mockMvc.perform(post("/publicacoes")
                        .with(authentication(authFor(AUTOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/publicacoes/7")))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.titulo").value("Devlog #1"))
                .andExpect(jsonPath("$.jogoId").value(42))
                .andExpect(jsonPath("$.usuarioId").value(AUTOR.toString()));
    }

    @Test
    void criar_comTituloEmBranco_retorna400() throws Exception {
        String payload = """
                {"titulo":"","descricao":"x","jogoId":42}
                """;

        mockMvc.perform(post("/publicacoes")
                        .with(authentication(authFor(AUTOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criar_semJogoId_retorna400() throws Exception {
        String payload = """
                {"titulo":"Devlog #1","descricao":"x"}
                """;

        mockMvc.perform(post("/publicacoes")
                        .with(authentication(authFor(AUTOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscarPorId_quandoExiste_retorna200() throws Exception {
        Postagem postagem = Postagem.builder()
                .id(7L).titulo("t").descricao("d").data(LocalDateTime.now())
                .jogoId(42L).usuarioId(AUTOR).build();

        when(postagemService.buscarPorId(7L)).thenReturn(postagem);

        mockMvc.perform(get("/publicacoes/7").with(authentication(authFor(AUTOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void buscarPorId_quandoNaoExiste_retorna404() throws Exception {
        when(postagemService.buscarPorId(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Publicação não encontrada"));

        mockMvc.perform(get("/publicacoes/99").with(authentication(authFor(AUTOR))))
                .andExpect(status().isNotFound());
    }

    @Test
    void listar_retornaPaginaDeResponses() throws Exception {
        Postagem postagem = Postagem.builder()
                .id(1L).titulo("a").descricao("b").data(LocalDateTime.now())
                .jogoId(42L).usuarioId(AUTOR).build();

        Page<Postagem> page = new PageImpl<>(List.of(postagem));
        when(postagemService.listar(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/publicacoes").with(authentication(authFor(AUTOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void atualizar_quandoAutor_retorna200() throws Exception {
        Postagem atualizado = Postagem.builder()
                .id(7L).titulo("novo").descricao("texto").data(LocalDateTime.now())
                .jogoId(42L).usuarioId(AUTOR).build();

        when(postagemService.atualizar(eq(7L), any(PostagemUpdateRequest.class), eq(AUTOR)))
                .thenReturn(atualizado);

        PostagemUpdateRequest request = new PostagemUpdateRequest("novo", "texto");

        mockMvc.perform(put("/publicacoes/7")
                        .with(authentication(authFor(AUTOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titulo").value("novo"));
    }

    @Test
    void atualizar_quandoNaoAutor_retorna403() throws Exception {
        when(postagemService.atualizar(eq(7L), any(PostagemUpdateRequest.class), any(UUID.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Operação permitida apenas ao autor da publicação"));

        PostagemUpdateRequest request = new PostagemUpdateRequest("novo", "texto");

        mockMvc.perform(put("/publicacoes/7")
                        .with(authentication(authFor(AUTOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deletar_quandoAutor_retorna204() throws Exception {
        mockMvc.perform(delete("/publicacoes/7").with(authentication(authFor(AUTOR))))
                .andExpect(status().isNoContent());

        verify(postagemService).deletar(7L, AUTOR);
    }
}
