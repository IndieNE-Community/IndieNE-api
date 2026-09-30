package com.indiene.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indiene.dto.request.DoacaoCreateRequest;
import com.indiene.dto.response.CampanhaResumoResponse;
import com.indiene.dto.response.DoacaoResponse;
import com.indiene.model.Doacao;
import com.indiene.service.DoacaoService;
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

@WebMvcTest(DoacaoController.class)
@Import(DoacaoControllerTest.PermissiveSecurity.class)
class DoacaoControllerTest {

    private static final UUID DOADOR = UUID.fromString("3f9c2b14-7d4e-4f1c-9a3e-2bc8a1f0e9d7");

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
    private DoacaoService doacaoService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static Authentication authFor(UUID id) {
        return new UsernamePasswordAuthenticationToken(id, null, List.of());
    }

    @Test
    void criar_comPayloadValido_retorna201ComLocationECorpo() throws Exception {
        Doacao salvo = Doacao.builder().id(7L).valor(50.0).jogoId(1L).usuarioId(DOADOR).build();
        when(doacaoService.criar(any(DoacaoCreateRequest.class), eq(DOADOR))).thenReturn(salvo);

        DoacaoCreateRequest request = new DoacaoCreateRequest(1L, 50.0);

        mockMvc.perform(post("/doacoes")
                        .with(authentication(authFor(DOADOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/doacoes/7")))
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.valor").value(50.0))
                .andExpect(jsonPath("$.jogoId").value(1));
    }

    @Test
    void criar_quandoJogoNaoExiste_retorna404() throws Exception {
        when(doacaoService.criar(any(DoacaoCreateRequest.class), eq(DOADOR)))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Jogo não encontrado"));

        DoacaoCreateRequest request = new DoacaoCreateRequest(99L, 50.0);

        mockMvc.perform(post("/doacoes")
                        .with(authentication(authFor(DOADOR)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void buscarPorId_quandoExiste_retorna200() throws Exception {
        Doacao doacao = Doacao.builder().id(7L).valor(50.0).jogoId(1L).usuarioId(DOADOR).build();
        when(doacaoService.buscarPorId(7L)).thenReturn(doacao);

        mockMvc.perform(get("/doacoes/7").with(authentication(authFor(DOADOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void listar_porJogo_retornaPagina() throws Exception {
        DoacaoResponse doacao = new DoacaoResponse(1L, 50.0, null, 1L, DOADOR, "Bruno Lima");
        Page<DoacaoResponse> page = new PageImpl<>(List.of(doacao));
        when(doacaoService.listarPorJogo(eq(1L), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/doacoes").param("jogoId", "1").with(authentication(authFor(DOADOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].usuarioNome").value("Bruno Lima"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void resumo_retornaAgregado() throws Exception {
        when(doacaoService.resumoPorJogo(1L))
                .thenReturn(new CampanhaResumoResponse(1L, 100000.0, 50000.0, 3L, 50));

        mockMvc.perform(get("/doacoes/resumo").param("jogoId", "1").with(authentication(authFor(DOADOR))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jogoId").value(1))
                .andExpect(jsonPath("$.totalArrecadado").value(50000.0))
                .andExpect(jsonPath("$.apoiadores").value(3))
                .andExpect(jsonPath("$.metaPercentual").value(50));
    }

    @Test
    void deletar_retorna204() throws Exception {
        mockMvc.perform(delete("/doacoes/7").with(authentication(authFor(DOADOR))))
                .andExpect(status().isNoContent());

        verify(doacaoService).deletar(7L, DOADOR);
    }
}
