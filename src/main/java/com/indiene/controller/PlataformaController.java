package com.indiene.controller;

import com.indiene.dto.request.PlataformaCreateRequest;
import com.indiene.dto.response.PlataformaResponse;
import com.indiene.model.Plataforma;
import com.indiene.service.PlataformaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/plataformas")
@RequiredArgsConstructor
@Tag(name = "Plataformas", description = "Plataformas/sistemas suportados por um jogo")
@SecurityRequirement(name = "bearerAuth")
public class PlataformaController {

    private final PlataformaService plataformaService;

    @Operation(summary = "Adicionar plataforma", description = "Adiciona uma plataforma a um jogo. Apenas o dono do jogo pode executar.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Plataforma criada"),
            @ApiResponse(responseCode = "400", description = "Payload inválido"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não é o dono do jogo"),
            @ApiResponse(responseCode = "404", description = "Jogo não encontrado"),
            @ApiResponse(responseCode = "409", description = "Plataforma já cadastrada para o jogo")
    })
    @PostMapping
    public ResponseEntity<PlataformaResponse> criar(
            @Valid @RequestBody PlataformaCreateRequest request,
            @AuthenticationPrincipal UUID autorId) {
        Plataforma plataforma = plataformaService.criar(request, autorId);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(plataforma.getId())
                .toUri();

        return ResponseEntity.created(location).body(PlataformaResponse.from(plataforma));
    }

    @Operation(summary = "Listar plataformas de um jogo")
    @ApiResponse(responseCode = "200", description = "Lista de plataformas")
    @GetMapping
    public List<PlataformaResponse> listar(@RequestParam Long jogoId) {
        return plataformaService.listarPorJogo(jogoId).stream()
                .map(PlataformaResponse::from)
                .toList();
    }

    @Operation(summary = "Remover plataforma", description = "Apenas o dono do jogo pode remover.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Plataforma removida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não é o dono do jogo"),
            @ApiResponse(responseCode = "404", description = "Plataforma não encontrada")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id, @AuthenticationPrincipal UUID autorId) {
        plataformaService.deletar(id, autorId);
    }
}
