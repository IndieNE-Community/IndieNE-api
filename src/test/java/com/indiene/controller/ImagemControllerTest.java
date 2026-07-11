package com.indiene.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indiene.dto.request.ImagemCreateRequest;
import com.indiene.model.Imagem;
import com.indiene.service.ImagemService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(ImagemController.class)
@Import(ImagemControllerTest.PermissiveSecurity.class)
class ImagemControllerTest {

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
    private ImagemService imagemService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static Authentication authFor(UUID id) {
        return new UsernamePasswordAuthenticationToken(id, null, List.of());
    }

    @Test
    void criar_comPayloadValido_retorna201ComLocationECorpo() throws Exception {
        Imagem salvo = Imagem.builder().id(3L).imagem("https://x/y.png").jogoId(1L).build();
        when(imagemService.criar(any(ImagemCreateRequest.class), eq(DONO))).thenReturn(salvo);

        ImagemCreateRequest request = new ImagemCreateRequest(1L, null, "https://x/y.png");

        mockMvc.perform(post("/imagens")
                        .with(authentication(authFor(DONO)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/imagens/3")))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.jogoId").value(1))
                .andExpect(jsonPath("$.imagem").value("https://x/y.png"));
    }

    @Test
    void listar_porJogo_retornaLista() throws Exception {
        Imagem img = Imagem.builder().id(1L).imagem("https://x/y.png").jogoId(1L).build();
        when(imagemService.listarPorJogo(1L)).thenReturn(List.of(img));

        mockMvc.perform(get("/imagens").param("jogoId", "1").with(authentication(authFor(DONO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].jogoId").value(1));
    }

    @Test
    void listar_semAlvo_retorna400() throws Exception {
        mockMvc.perform(get("/imagens").with(authentication(authFor(DONO))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listar_comAmbosAlvos_retorna400() throws Exception {
        mockMvc.perform(get("/imagens")
                        .param("jogoId", "1")
                        .param("postagemId", "5")
                        .with(authentication(authFor(DONO))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletar_retorna204() throws Exception {
        mockMvc.perform(delete("/imagens/3").with(authentication(authFor(DONO))))
                .andExpect(status().isNoContent());

        verify(imagemService).deletar(3L, DONO);
    }
}
