package br.com.dio.estoque_cerveja.controller;



import br.com.dio.estoque_cerveja.dto.CervejaEstoqueIncrementadoDTO;
import br.com.dio.estoque_cerveja.dto.CervejaRequestDTO;
import br.com.dio.estoque_cerveja.dto.CervejaResponseDTO;
import br.com.dio.estoque_cerveja.service.CervejaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cervejas")
@RequiredArgsConstructor
@Tag(name = "Cervejas", description = "Endpoints para gerenciamento de estoque de cervejas")
public class CervejaController {

    @Autowired
    private CervejaService service;

    @Operation(summary = "Cadastrar uma nova cerveja")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cerveja criada com sucesso",
                    content = @Content(schema = @Schema(implementation = CervejaResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação ou regra de negócio")
    })
    @PostMapping
    public ResponseEntity<CervejaResponseDTO> criar(@Valid @RequestBody CervejaRequestDTO dto) {
        CervejaResponseDTO novaCerveja = service.criarCerveja(dto);
        return ResponseEntity.created(URI.create("/api/cervejas/" + novaCerveja.id())).body(novaCerveja);
    }

    @Operation(summary = "Buscar cerveja pelo nome")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cerveja encontrada",
                    content = @Content(schema = @Schema(implementation = CervejaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cerveja não encontrada")
    })
    @GetMapping("/nome/{nome}")
    public ResponseEntity<CervejaResponseDTO> buscarPorNome(@PathVariable String nome) {
        return ResponseEntity.ok(service.encontrarPorNome(nome));
    }

    @Operation(summary = "Listar todas as cervejas")
    @ApiResponse(responseCode = "200", description = "Lista de cervejas retornada com sucesso")
    @GetMapping
    public ResponseEntity<List<CervejaResponseDTO>> listarTudo() {
        return ResponseEntity.ok(service.listarTudo());
    }

    @Operation(summary = "Excluir cerveja pelo ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cerveja deletada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Cerveja não encontrada")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletar(@PathVariable Long id) {
        service.deletarPorId(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Incrementar estoque de cerveja")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estoque incrementado com sucesso",
                    content = @Content(schema = @Schema(implementation = CervejaResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Cerveja não encontrada"),
            @ApiResponse(responseCode = "400", description = "Estoque máximo excedido")
    })
    @PatchMapping("/{id}/incrementar")
    public ResponseEntity<CervejaResponseDTO> incrementarEstoque(
            @PathVariable Long id,
            @RequestBody @Valid CervejaEstoqueIncrementadoDTO cervejaEstoqueIncrementadoDTO) {

        CervejaResponseDTO cervejaAtualizada = service.incrementarEstoque(id, cervejaEstoqueIncrementadoDTO.quantidade());
        return ResponseEntity.ok(cervejaAtualizada);
    }
}
