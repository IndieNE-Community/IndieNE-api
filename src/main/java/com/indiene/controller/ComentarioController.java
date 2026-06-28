package com.indiene.controller;

import com.indiene.dto.request.ComentarioCreateRequest;
import com.indiene.dto.request.ComentarioUpdateRequest;
import com.indiene.dto.response.ComentarioResponse;
import com.indiene.model.Comentario;
import com.indiene.service.ComentarioService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/comentarios")
@RequiredArgsConstructor
@Tag(name = "Comentários", description = "CRUD de comentários vinculados a uma publicação")
@SecurityRequirement(name = "bearerAuth")
public class ComentarioController {

    private final ComentarioService comentarioService;

    @Operation(summary = "Criar comentário", description = "Cria um comentário cujo autor é o usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Comentário criado"),
            @ApiResponse(responseCode = "400", description = "Payload inválido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Publicação não encontrada")
    })
    @PostMapping
    public ResponseEntity<ComentarioResponse> criar(
            @Valid @RequestBody ComentarioCreateRequest request,
            @AuthenticationPrincipal UUID autorId) {
        Comentario comentario = comentarioService.criar(request, autorId);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(comentario.getId())
                .toUri();

        return ResponseEntity.created(location).body(ComentarioResponse.from(comentario));
    }

    @Operation(summary = "Buscar comentário por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comentário encontrado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Comentário não encontrado")
    })
    @GetMapping("/{id}")
    public ComentarioResponse buscarPorId(@PathVariable Long id) {
        return ComentarioResponse.from(comentarioService.buscarPorId(id));
    }

    @Operation(summary = "Listar comentários de uma publicação", description = "Lista paginada de comentários de uma publicação.")
    @ApiResponse(responseCode = "200", description = "Página de comentários")
    @GetMapping
    public Page<ComentarioResponse> listar(@RequestParam Long postagemId, Pageable pageable) {
        return comentarioService.listarPorPostagem(postagemId, pageable).map(ComentarioResponse::from);
    }

    @Operation(summary = "Atualizar comentário", description = "Apenas o autor pode atualizar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comentário atualizado"),
            @ApiResponse(responseCode = "400", description = "Payload inválido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não é o autor"),
            @ApiResponse(responseCode = "404", description = "Comentário não encontrado")
    })
    @PutMapping("/{id}")
    public ComentarioResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody ComentarioUpdateRequest request,
            @AuthenticationPrincipal UUID autorId) {
        return ComentarioResponse.from(comentarioService.atualizar(id, request, autorId));
    }

    @Operation(summary = "Deletar comentário", description = "Apenas o autor pode deletar.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Comentário removido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não é o autor"),
            @ApiResponse(responseCode = "404", description = "Comentário não encontrado")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id, @AuthenticationPrincipal UUID autorId) {
        comentarioService.deletar(id, autorId);
    }
}
