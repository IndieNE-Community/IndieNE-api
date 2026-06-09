package com.indiene.controller;

import com.indiene.dto.request.PostagemCreateRequest;
import com.indiene.dto.request.PostagemUpdateRequest;
import com.indiene.dto.response.PostagemResponse;
import com.indiene.model.Postagem;
import com.indiene.service.PostagemService;
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
@RequestMapping("/publicacoes")
@RequiredArgsConstructor
@Tag(name = "Publicações", description = "CRUD de publicações (postagens) vinculadas a um jogo")
@SecurityRequirement(name = "bearerAuth")
public class PostagemController {

    private final PostagemService postagemService;

    @Operation(summary = "Criar publicação", description = "Cria uma publicação cujo autor é o usuário autenticado.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Publicação criada"),
            @ApiResponse(responseCode = "400", description = "Payload inválido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado")
    })
    @PostMapping
    public ResponseEntity<PostagemResponse> criar(
            @Valid @RequestBody PostagemCreateRequest request,
            @AuthenticationPrincipal UUID autorId) {
        Postagem postagem = postagemService.criar(request, autorId);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(postagem.getId())
                .toUri();

        return ResponseEntity.created(location).body(PostagemResponse.from(postagem));
    }

    @Operation(summary = "Buscar publicação por id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publicação encontrada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "404", description = "Publicação não encontrada")
    })
    @GetMapping("/{id}")
    public PostagemResponse buscarPorId(@PathVariable Long id) {
        return PostagemResponse.from(postagemService.buscarPorId(id));
    }

    @Operation(summary = "Listar publicações", description = "Lista paginada de publicações.")
    @ApiResponse(responseCode = "200", description = "Página de publicações")
    @GetMapping
    public Page<PostagemResponse> listar(Pageable pageable) {
        return postagemService.listar(pageable).map(PostagemResponse::from);
    }

    @Operation(summary = "Atualizar publicação", description = "Apenas o autor pode atualizar.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Publicação atualizada"),
            @ApiResponse(responseCode = "400", description = "Payload inválido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não é o autor"),
            @ApiResponse(responseCode = "404", description = "Publicação não encontrada")
    })
    @PutMapping("/{id}")
    public PostagemResponse atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PostagemUpdateRequest request,
            @AuthenticationPrincipal UUID autorId) {
        return PostagemResponse.from(postagemService.atualizar(id, request, autorId));
    }

    @Operation(summary = "Deletar publicação", description = "Apenas o autor pode deletar.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Publicação removida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não é o autor"),
            @ApiResponse(responseCode = "404", description = "Publicação não encontrada")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id, @AuthenticationPrincipal UUID autorId) {
        postagemService.deletar(id, autorId);
    }
}
