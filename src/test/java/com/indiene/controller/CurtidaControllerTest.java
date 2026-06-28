package com.indiene.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indiene.dto.request.CurtidaCreateRequest;
import com.indiene.model.Curtida;
import com.indiene.service.CurtidaService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CurtidaController.class)
@Import(CurtidaControllerTest.PermissiveSecurity.class)
class CurtidaControllerTest {

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
    private CurtidaService curtidaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static Authentication authFor(UUID id) {
        return new UsernamePasswordAuthenticationToken(id, null, List.of());
    }

    @Test
    void criar_comPayloadValido_retorna201ComLocationECorpo() throws Exception {
        Curtida salvo = Curtida.builder()
                .id(7L).tipo("LIKE").postagemId(1L).usuarioId(AUTOR).build();

        when(curtidaService.criar(any(CurtidaCreateRequest.class), eq(AUTOR))).thenReturn(salvo);

        CurtidaCreateRequest request = new CurtidaCreateRequest(1L, null, "LIKE");

        mockMvc.perform(post("/curtidas")
                        .with(authentication(authFor(AUTOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/curtidas/7")))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.postagemId").value(1))
                .andExpect(jsonPath("$.usuarioId").value(AUTOR.toString()));
    }

    @Test
    void criar_quandoAlvoDuplicado_retorna409() throws Exception {
        when(curtidaService.criar(any(CurtidaCreateRequest.class), eq(AUTOR)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "Publicação já curtida por este usuário"));

        CurtidaCreateRequest request = new CurtidaCreateRequest(1L, null, null);

        mockMvc.perform(post("/curtidas")
                        .with(authentication(authFor(AUTOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void criar_quandoServicoRejeitaAlvo_retorna400() throws Exception {
        when(curtidaService.criar(any(CurtidaCreateRequest.class), eq(AUTOR)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe exatamente um alvo"));

        CurtidaCreateRequest request = new CurtidaCreateRequest(null, null, null);

        mockMvc.perform(post("/curtidas")
                        .with(authentication(authFor(AUTOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void buscarPorId_quandoExiste_retorna200() throws Exception {
        Curtida curtida = Curtida.builder()
                .id(7L).tipo("LIKE").postagemId(1L).usuarioId(AUTOR).build();

        when(curtidaService.buscarPorId(7L)).thenReturn(curtida);

        mockMvc.perform(get("/curtidas/7").with(authentication(authFor(AUTOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void listar_porPostagem_retornaPagina() throws Exception {
        Curtida curtida = Curtida.builder()
                .id(1L).tipo("LIKE").postagemId(1L).usuarioId(AUTOR).build();

        Page<Curtida> page = new PageImpl<>(List.of(curtida));
        when(curtidaService.listarPorPostagem(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/curtidas").param("postagemId", "1").with(authentication(authFor(AUTOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void listar_semAlvo_retorna400() throws Exception {
        mockMvc.perform(get("/curtidas").with(authentication(authFor(AUTOR))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listar_comAmbosAlvos_retorna400() throws Exception {
        mockMvc.perform(get("/curtidas")
                        .param("postagemId", "1")
                        .param("comentarioId", "5")
                        .with(authentication(authFor(AUTOR))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletar_quandoAutor_retorna204() throws Exception {
        mockMvc.perform(delete("/curtidas/7").with(authentication(authFor(AUTOR))))
                .andExpect(status().isNoContent());

        verify(curtidaService).deletar(7L, AUTOR);
    }
}
