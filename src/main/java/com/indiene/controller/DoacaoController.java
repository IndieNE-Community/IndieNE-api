package com.indiene.controller;

import com.indiene.dto.request.DoacaoCreateRequest;
import com.indiene.dto.response.CampanhaResumoResponse;
import com.indiene.dto.response.DoacaoResponse;
import com.indiene.model.Doacao;
import com.indiene.service.DoacaoService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/doacoes")
@RequiredArgsConstructor
@Tag(name = "Doações", description = "Doações para as campanhas dos jogos")
@SecurityRequirement(name = "bearerAuth")
public class DoacaoController {

    private final DoacaoService doacaoService;

    @Operation(summary = "Criar doação", description = "Registra uma doação do usuário autenticado para a campanha de um jogo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Doação registrada"),
            @ApiResponse(responseCode = "400", description = "Payload inválido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado")
    })
    @PostMapping
    public ResponseEntity<DoacaoResponse> criar(
            @Valid @RequestBody DoacaoCreateRequest request,
            @AuthenticationPrincipal UUID autorId) {
        Doacao doacao = doacaoService.criar(request, autorId);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(doacao.getId())
                .toUri();

        return ResponseEntity.created(location).body(DoacaoResponse.from(doacao));
    }

    @Operation(summary = "Buscar doação por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Doação encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Doação não encontrada")
    })
    @GetMapping("/{id}")
    public DoacaoResponse buscarPorId(@PathVariable Long id) {
        return DoacaoResponse.from(doacaoService.buscarPorId(id));
    }

    @Operation(summary = "Listar doações de um jogo", description = "Lista paginada de doações de um jogo.")
    @ApiResponse(responseCode = "200", description = "Página de doações")
    @GetMapping
    public Page<DoacaoResponse> listar(@RequestParam Long jogoId, Pageable pageable) {
        return doacaoService.listarPorJogo(jogoId, pageable).map(DoacaoResponse::from);
    }

    @Operation(summary = "Resumo da campanha de um jogo",
            description = "Retorna total arrecadado, número de apoiadores e percentual da meta atingido.")
    @ApiResponse(responseCode = "200", description = "Resumo da campanha")
    @GetMapping("/resumo")
    public CampanhaResumoResponse resumo(@RequestParam Long jogoId) {
        return doacaoService.resumoPorJogo(jogoId);
    }

    @Operation(summary = "Remover doação", description = "Apenas o doador pode remover.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Doação removida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não é o doador"),
            @ApiResponse(responseCode = "404", description = "Doação não encontrada")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id, @AuthenticationPrincipal UUID autorId) {
        doacaoService.deletar(id, autorId);
    }
}
