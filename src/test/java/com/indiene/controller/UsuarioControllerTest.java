package com.indiene.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indiene.dto.request.UsuarioCreateRequest;
import com.indiene.model.TipoUsuario;
import com.indiene.model.Usuario;
import com.indiene.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void criar_comPayloadValido_retorna201ComLocationECorpo() throws Exception {
        UUID id = UUID.fromString("3f9c2b14-7d4e-4f1c-9a3e-2bc8a1f0e9d7");
        Usuario salvo = Usuario.builder()
                .id(id)
                .nome("Fulano")
                .email("fulano@x.com")
                .senha("$2a$hash")
                .tipo(TipoUsuario.DESENVOLVEDOR)
                .build();

        when(usuarioService.criar(any(UsuarioCreateRequest.class))).thenReturn(salvo);

        UsuarioCreateRequest request = new UsuarioCreateRequest(
                "Fulano", "fulano@x.com", "senha1234", TipoUsuario.DESENVOLVEDOR);

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.endsWith("/usuarios/" + id)))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.email").value("fulano@x.com"))
                .andExpect(jsonPath("$.tipo").value("DESENVOLVEDOR"))
                .andExpect(jsonPath("$.senha").doesNotExist());
    }

    @Test
    void criar_comEmailInvalido_retorna400() throws Exception {
        String payload = """
                {"nome":"Fulano","email":"nao-eh-email","senha":"senha1234","tipo":"DESENVOLVEDOR"}
                """;

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criar_comSenhaCurta_retorna400() throws Exception {
        String payload = """
                {"nome":"Fulano","email":"fulano@x.com","senha":"123","tipo":"DESENVOLVEDOR"}
                """;

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criar_comTipoInvalido_retorna400() throws Exception {
        String payload = """
                {"nome":"Fulano","email":"fulano@x.com","senha":"senha1234","tipo":"INVALIDO"}
                """;

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void criar_quandoEmailDuplicado_retorna409() throws Exception {
        when(usuarioService.criar(any(UsuarioCreateRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.CONFLICT, "E-mail já cadastrado"));

        UsuarioCreateRequest request = new UsuarioCreateRequest(
                "Fulano", "fulano@x.com", "senha1234", TipoUsuario.DESENVOLVEDOR);

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }
}
