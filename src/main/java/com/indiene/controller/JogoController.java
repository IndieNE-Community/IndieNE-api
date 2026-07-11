package com.indiene.controller;

import com.indiene.dto.request.JogoCreateRequest;
import com.indiene.dto.request.JogoUpdateRequest;
import com.indiene.dto.response.JogoResponse;
import com.indiene.service.JogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/jogos")
@RequiredArgsConstructor
@Tag(name = "Jogos", description = "Cadastro de jogos")
@SecurityRequirement(name = "bearerAuth")
public class JogoController {

    private final JogoService jogoService;

    @Operation(summary = "Criar jogo", description = "Cria um jogo cujo dono é o usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Jogo criado"),
            @ApiResponse(responseCode = "400", description = "Payload inválido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PostMapping
    public ResponseEntity<JogoResponse> criar(
            @Valid @RequestBody JogoCreateRequest request,
            @AuthenticationPrincipal UUID autorId) {
        JogoResponse jogo = jogoService.criar(request, autorId);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(jogo.id())
                .toUri();

        return ResponseEntity.created(location).body(jogo);
    }

    @Operation(summary = "Buscar jogo por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Jogo encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado")
    })
    @GetMapping("/{id}")
    public JogoResponse buscarPorId(@PathVariable Long id) {
        return jogoService.buscarPorId(id);
    }

    @Operation(summary = "Listar jogos", description = "Lista paginada de jogos não deletados, com dados de exibição e resumo da campanha.")
    @ApiResponse(responseCode = "200", description = "Página de jogos")
    @GetMapping
    public Page<JogoResponse> listar(Pageable pageable) {
        return jogoService.listar(pageable);
    }

    @Operation(summary = "Atualizar jogo", description = "Apenas o dono pode atualizar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Jogo atualizado"),
            @ApiResponse(responseCode = "400", description = "Payload inválido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não é o dono"),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado")
    })
    @PutMapping("/{id}")
    public JogoResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody JogoUpdateRequest request,
            @AuthenticationPrincipal UUID autorId) {
        return jogoService.atualizar(id, request, autorId);
    }

    @Operation(summary = "Deletar jogo (lógico)", description = "Marca o jogo como deletado. Apenas o dono pode executar.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Jogo deletado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não é o dono"),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id, @AuthenticationPrincipal UUID autorId) {
        jogoService.deletar(id, autorId);
    }
}
