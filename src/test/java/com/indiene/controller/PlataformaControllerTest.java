package com.indiene.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indiene.dto.request.PlataformaCreateRequest;
import com.indiene.model.Plataforma;
import com.indiene.service.PlataformaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
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

@WebMvcTest(PlataformaController.class)
@Import(PlataformaControllerTest.PermissiveSecurity.class)
class PlataformaControllerTest {

    private static final UUID DONO = UUID.fromString("3f9c2b14-7d4e-4f1c-9a3e-2bc8a1f0e9d7");

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
    private PlataformaService plataformaService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static Authentication authFor(UUID id) {
        return new UsernamePasswordAuthenticationToken(id, null, List.of());
    }

    @Test
    void criar_comPayloadValido_retorna201ComLocationECorpo() throws Exception {
        Plataforma salvo = Plataforma.builder().id(9L).plataforma("Windows").jogoId(1L).build();
        when(plataformaService.criar(any(PlataformaCreateRequest.class), eq(DONO))).thenReturn(salvo);

        PlataformaCreateRequest request = new PlataformaCreateRequest(1L, "Windows");

        mockMvc.perform(post("/plataformas")
                        .with(authentication(authFor(DONO)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/plataformas/9")))
                .andExpect(jsonPath("$.id").value(9))
                .andExpect(jsonPath("$.plataforma").value("Windows"))
                .andExpect(jsonPath("$.jogoId").value(1));
    }

    @Test
    void criar_quandoNaoDono_retorna403() throws Exception {
        when(plataformaService.criar(any(PlataformaCreateRequest.class), eq(DONO)))
                .thenThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Operação permitida apenas ao dono do jogo"));

        PlataformaCreateRequest request = new PlataformaCreateRequest(1L, "Windows");

        mockMvc.perform(post("/plataformas")
                        .with(authentication(authFor(DONO)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void listar_porJogo_retornaLista() throws Exception {
        Plataforma p = Plataforma.builder().id(1L).plataforma("Windows").jogoId(1L).build();
        when(plataformaService.listarPorJogo(1L)).thenReturn(List.of(p));

        mockMvc.perform(get("/plataformas").param("jogoId", "1").with(authentication(authFor(DONO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].plataforma").value("Windows"));
    }

    @Test
    void deletar_retorna204() throws Exception {
        mockMvc.perform(delete("/plataformas/9").with(authentication(authFor(DONO))))
                .andExpect(status().isNoContent());

        verify(plataformaService).deletar(9L, DONO);
    }
}
