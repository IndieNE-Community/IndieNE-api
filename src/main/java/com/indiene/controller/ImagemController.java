package com.indiene.controller;

import com.indiene.dto.request.ImagemCreateRequest;
import com.indiene.dto.response.ImagemResponse;
import com.indiene.model.Imagem;
import com.indiene.service.ImagemService;
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
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/imagens")
@RequiredArgsConstructor
@Tag(name = "Imagens", description = "Imagens de jogos e publicações")
@SecurityRequirement(name = "bearerAuth")
public class ImagemController {

    private final ImagemService imagemService;

    @Operation(summary = "Adicionar imagem",
            description = "Adiciona uma imagem a um jogo ou publicação. Informe exatamente um alvo.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Imagem criada"),
            @ApiResponse(responseCode = "400", description = "Payload inválido (nenhum ou ambos os alvos)"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não é o dono do alvo"),
            @ApiResponse(responseCode = "404", description = "Alvo não encontrado")
    })
    @PostMapping
    public ResponseEntity<ImagemResponse> criar(
            @Valid @RequestBody ImagemCreateRequest request,
            @AuthenticationPrincipal UUID autorId) {
        Imagem imagem = imagemService.criar(request, autorId);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(imagem.getId())
                .toUri();

        return ResponseEntity.created(location).body(ImagemResponse.from(imagem));
    }

    @Operation(summary = "Listar imagens de um alvo",
            description = "Lista as imagens. Informe exatamente um alvo: jogoId ou postagemId.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de imagens"),
            @ApiResponse(responseCode = "400", description = "Nenhum ou ambos os alvos informados")
    })
    @GetMapping
    public List<ImagemResponse> listar(
            @RequestParam(required = false) Long jogoId,
            @RequestParam(required = false) Long postagemId) {
        if ((jogoId != null) == (postagemId != null)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Informe exatamente um alvo: jogoId ou postagemId");
        }
        List<Imagem> imagens = jogoId != null
                ? imagemService.listarPorJogo(jogoId)
                : imagemService.listarPorPostagem(postagemId);
        return imagens.stream().map(ImagemResponse::from).toList();
    }

    @Operation(summary = "Remover imagem", description = "Apenas o dono do jogo/autor da publicação pode remover.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Imagem removida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado"),
            @ApiResponse(responseCode = "403", description = "Usuário não é o dono do alvo"),
            @ApiResponse(responseCode = "404", description = "Imagem não encontrada")
    })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void deletar(@PathVariable Long id, @AuthenticationPrincipal UUID autorId) {
        imagemService.deletar(id, autorId);
    }
}
