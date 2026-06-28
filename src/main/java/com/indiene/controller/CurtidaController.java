package com.indiene.controller;

import com.indiene.dto.request.CurtidaCreateRequest;
import com.indiene.dto.response.CurtidaResponse;
import com.indiene.model.Curtida;
import com.indiene.service.CurtidaService;
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
@RequestMapping("/curtidas")
@RequiredArgsConstructor
@Tag(name = "Curtidas", description = "Curtidas de publicações e comentários")
@SecurityRequirement(name = "bearerAuth")
public class CurtidaController {

    private final CurtidaService curtidaService;

    @Operation(summary = "Criar curtida",
            description = "Curte uma publicação ou um comentário em nome do usuário autenticado. Informe exatamente um alvo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Curtida criada"),
            @ApiResponse(responseCode = "400", description = "Payload inválido (nenhum ou ambos os alvos)"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Alvo não encontrado"),
            @ApiResponse(responseCode = "409", description = "Alvo já curtido por este usuário")
    })
    @PostMapping
    public ResponseEntity<CurtidaResponse> criar(
            @Valid @RequestBody CurtidaCreateRequest request,
            @AuthenticationPrincipal UUID autorId) {
        Curtida curtida = curtidaService.criar(request, autorId);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(curtida.getId())
                .toUri();

        return ResponseEntity.created(location).body(CurtidaResponse.from(curtida));
    }

    @Operation(summary = "Buscar curtida por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Curtida encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Curtida não encontrada")
    })
    @GetMapping("/{id}")
    public CurtidaResponse buscarPorId(@PathVariable Long id) {
        return CurtidaResponse.from(curtidaService.buscarPorId(id));
    }

    @Operation(summary = "Listar curtidas de um alvo",
            description = "Lista paginada de curtidas. Informe exatamente um alvo: postagemId ou comentarioId.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de curtidas"),
            @ApiResponse(responseCode = "400", description = "Nenhum ou ambos os alvos informados")
    })
    @GetMapping
    public Page<CurtidaResponse> listar(
            @RequestParam(required = false) Long postagemId,
            @RequestParam(required = false) Long comentarioId,
            Pageable pageable) {
        if ((postagemId != null) == (comentarioId != null)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Informe exatamente um alvo: postagemId ou comentarioId");
        }
        Page<Curtida> pagina = postagemId != null
                ? curtidaService.listarPorPostagem(postagemId, pageable)
                : curtidaService.listarPorComentario(comentarioId, pageable);
        return pagina.map(CurtidaResponse::from);
    }

    @Operation(summary = "Remover curtida", description = "Apenas o autor pode remover.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Curtida removida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não é o autor"),
            @ApiResponse(responseCode = "404", description = "Curtida não encontrada")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id, @AuthenticationPrincipal UUID autorId) {
        curtidaService.deletar(id, autorId);
    }
}
