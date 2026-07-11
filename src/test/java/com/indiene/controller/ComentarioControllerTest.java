package com.indiene.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indiene.dto.request.ComentarioCreateRequest;
import com.indiene.dto.request.ComentarioUpdateRequest;
import com.indiene.dto.response.ComentarioResponse;
import com.indiene.service.ComentarioService;
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

@WebMvcTest(ComentarioController.class)
@Import(ComentarioControllerTest.PermissiveSecurity.class)
class ComentarioControllerTest {

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
    private ComentarioService comentarioService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static Authentication authFor(UUID id) {
        return new UsernamePasswordAuthenticationToken(id, null, List.of());
    }

    private ComentarioResponse response(Long id, String texto, long likes, long dislikes) {
        return new ComentarioResponse(id, texto, null, 1L, AUTOR, likes, dislikes);
    }

    @Test
    void criar_comPayloadValido_retorna201ComLocationECorpo() throws Exception {
        when(comentarioService.criar(any(ComentarioCreateRequest.class), eq(AUTOR)))
                .thenReturn(response(7L, "muito bom", 0L, 0L));

        ComentarioCreateRequest request = new ComentarioCreateRequest("muito bom", 1L);

        mockMvc.perform(post("/comentarios")
                        .with(authentication(authFor(AUTOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/comentarios/7")))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.texto").value("muito bom"))
                .andExpect(jsonPath("$.postagemId").value(1))
                .andExpect(jsonPath("$.usuarioId").value(AUTOR.toString()))
                .andExpect(jsonPath("$.likes").value(0));
    }

    @Test
    void criar_comTextoEmBranco_retorna400() throws Exception {
        mockMvc.perform(post("/comentarios")
                        .with(authentication(authFor(AUTOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"\",\"postagemId\":1}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criar_semPostagemId_retorna400() throws Exception {
        mockMvc.perform(post("/comentarios")
                        .with(authentication(authFor(AUTOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"texto\":\"oi\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscarPorId_quandoExiste_retorna200ComContagens() throws Exception {
        when(comentarioService.buscarPorId(7L)).thenReturn(response(7L, "t", 58L, 3L));

        mockMvc.perform(get("/comentarios/7").with(authentication(authFor(AUTOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.likes").value(58))
                .andExpect(jsonPath("$.dislikes").value(3));
    }

    @Test
    void buscarPorId_quandoNaoExiste_retorna404() throws Exception {
        when(comentarioService.buscarPorId(99L))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Comentário não encontrado"));

        mockMvc.perform(get("/comentarios/99").with(authentication(authFor(AUTOR))))
                .andExpect(status().isNotFound());
    }

    @Test
    void listar_retornaPaginaDeResponses() throws Exception {
        Page<ComentarioResponse> page = new PageImpl<>(List.of(response(1L, "a", 5L, 1L)));
        when(comentarioService.listarPorPostagem(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/comentarios").param("postagemId", "1").with(authentication(authFor(AUTOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].likes").value(5))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void listar_semPostagemId_retorna400() throws Exception {
        mockMvc.perform(get("/comentarios").with(authentication(authFor(AUTOR))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void atualizar_quandoAutor_retorna200() throws Exception {
        when(comentarioService.atualizar(eq(7L), any(ComentarioUpdateRequest.class), eq(AUTOR)))
                .thenReturn(response(7L, "novo", 0L, 0L));

        ComentarioUpdateRequest request = new ComentarioUpdateRequest("novo");

        mockMvc.perform(put("/comentarios/7")
                        .with(authentication(authFor(AUTOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.texto").value("novo"));
    }

    @Test
    void atualizar_quandoNaoAutor_retorna403() throws Exception {
        when(comentarioService.atualizar(eq(7L), any(ComentarioUpdateRequest.class), any(UUID.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Operação permitida apenas ao autor do comentário"));

        ComentarioUpdateRequest request = new ComentarioUpdateRequest("novo");

        mockMvc.perform(put("/comentarios/7")
                        .with(authentication(authFor(AUTOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deletar_quandoAutor_retorna204() throws Exception {
        mockMvc.perform(delete("/comentarios/7").with(authentication(authFor(AUTOR))))
                .andExpect(status().isNoContent());

        verify(comentarioService).deletar(7L, AUTOR);
    }
}
