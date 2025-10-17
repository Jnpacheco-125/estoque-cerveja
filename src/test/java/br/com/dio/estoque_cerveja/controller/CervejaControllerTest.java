package br.com.dio.estoque_cerveja.controller;


import br.com.dio.estoque_cerveja.dto.CervejaEstoqueIncrementadoDTO;
import br.com.dio.estoque_cerveja.dto.CervejaRequestDTO;
import br.com.dio.estoque_cerveja.dto.CervejaResponseDTO;
import br.com.dio.estoque_cerveja.enums.TipoCerveja;
import br.com.dio.estoque_cerveja.exception.CervejaJaExisteException;
import br.com.dio.estoque_cerveja.exception.CervejaNaoEncontradaException;
import br.com.dio.estoque_cerveja.exception.EstoqueExcedidoException;
import br.com.dio.estoque_cerveja.service.CervejaService;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.http.MediaType;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CervejaController.class)
public class CervejaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CervejaService cervejaService;


    @Test
    void deveRetornar201_QuandoCriarCervejaComSucesso() throws Exception {
        // Arrange
        CervejaRequestDTO requestDTO = new CervejaRequestDTO(
                "Heineken",
                "Heineken",
                100,
                50,
                TipoCerveja.LAGER
        );

        CervejaResponseDTO responseDTO = new CervejaResponseDTO(
                1L,
                "Heineken",
                "Heineken",
                100,
                50,
                TipoCerveja.LAGER
        );

        when(cervejaService.criarCerveja(any(CervejaRequestDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/cervejas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/cervejas/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Heineken"))
                .andExpect(jsonPath("$.marca").value("Heineken"))
                .andExpect(jsonPath("$.maximo").value(100))
                .andExpect(jsonPath("$.quantidade").value(50))
                .andExpect(jsonPath("$.tipo").value("LAGER"));

        verify(cervejaService, times(1)).criarCerveja(any(CervejaRequestDTO.class));
    }

    @Test
    void deveRetornar400_QuandoQuantidadeNegativa() throws Exception {
        // Arrange
        CervejaRequestDTO requestDTO = new CervejaRequestDTO(
                "Heineken", "Heineken", 100, -5, TipoCerveja.LAGER
        );

        // ATENÇÃO: Se houver @Min(0) no DTO, o Service NUNCA é chamado.

        // Act & Assert
        mockMvc.perform(post("/api/cervejas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());
    }



    @Test
    void deveRetornar400_QuandoRequestBodyInvalido() throws Exception {
        // Arrange - JSON mal formado (JSON parse error)
        // O JSON termina de forma incompleta, causando um erro de desserialização.
        String jsonInvalido = "{ \"nome\": \"Heineken\", \"marca\": }";

        // Act & Assert
        mockMvc.perform(post("/api/cervejas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalido))
                // Espera-se 400 Bad Request (HttpMessageNotReadableException)
                .andExpect(status().isBadRequest());

        // Verifica que o Service NÃO foi chamado
        verify(cervejaService, never()).criarCerveja(any(CervejaRequestDTO.class));
    }

    // ===== TESTES DE ERRO - STATUS 415 =====

    @Test
    void deveRetornar415_QuandoContentTypeInvalido() throws Exception {
        // Arrange
        CervejaRequestDTO requestDTO = new CervejaRequestDTO(
                "Heineken",
                "Heineken",
                100,
                50,
                TipoCerveja.LAGER
        );

        // Act & Assert - ContentType incorreto
        mockMvc.perform(post("/api/cervejas")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isUnsupportedMediaType());

        verify(cervejaService, never()).criarCerveja(any(CervejaRequestDTO.class));
    }

    @Test
    void deveRetornar500_QuandoErroInterno() throws Exception {
        // Arrange
        CervejaRequestDTO requestDTO = new CervejaRequestDTO(
                "Heineken",
                "Heineken",
                100,
                50,
                TipoCerveja.LAGER
        );

        when(cervejaService.criarCerveja(any(CervejaRequestDTO.class)))
                .thenThrow(new RuntimeException("Erro interno inesperado"));

        // Act & Assert
        mockMvc.perform(post("/api/cervejas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isInternalServerError());

        verify(cervejaService, times(1)).criarCerveja(any(CervejaRequestDTO.class));
    }

    @Test
    void deveRetornar200_QuandoBuscarPorNomeComSucesso() throws Exception {
        // Arrange
        String nomeCerveja = "Heineken";
        CervejaResponseDTO responseDTO = new CervejaResponseDTO(
                1L,
                "Heineken",
                "Heineken",
                100,
                50,
                TipoCerveja.LAGER
        );

        when(cervejaService.encontrarPorNome(nomeCerveja)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/cervejas/nome/{nome}", nomeCerveja)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Heineken"))
                .andExpect(jsonPath("$.marca").value("Heineken"))
                .andExpect(jsonPath("$.maximo").value(100))
                .andExpect(jsonPath("$.quantidade").value(50))
                .andExpect(jsonPath("$.tipo").value("LAGER"));

        verify(cervejaService, times(1)).encontrarPorNome(nomeCerveja);
    }

    @Test
    void deveRetornar404_QuandoBuscarPorNomeNaoEncontrado() throws Exception {
        // Arrange
        String nomeCerveja = "CervejaInexistente";

        when(cervejaService.encontrarPorNome(nomeCerveja))
                .thenThrow(new CervejaNaoEncontradaException("Cerveja não encontrada com o nome: " + nomeCerveja));

        // Act & Assert
        mockMvc.perform(get("/api/cervejas/nome/{nome}", nomeCerveja)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(cervejaService, times(1)).encontrarPorNome(nomeCerveja);
    }

    // ===== TESTES PARA listarTudo() =====

    @Test
    void deveRetornar200_QuandoListarTodasCervejasComSucesso() throws Exception {
        // Arrange
        List<CervejaResponseDTO> cervejas = Arrays.asList(
                new CervejaResponseDTO(1L, "Heineken", "Heineken", 100, 50, TipoCerveja.LAGER),
                new CervejaResponseDTO(2L, "Skol", "Ambev", 120, 80, TipoCerveja.PILSEN),
                new CervejaResponseDTO(3L, "Colorado", "Colorado", 60, 25, TipoCerveja.IPA)
        );

        when(cervejaService.listarTudo()).thenReturn(cervejas);

        // Act & Assert
        mockMvc.perform(get("/api/cervejas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Heineken"))
                .andExpect(jsonPath("$[0].marca").value("Heineken"))
                .andExpect(jsonPath("$[0].maximo").value(100))
                .andExpect(jsonPath("$[0].quantidade").value(50))
                .andExpect(jsonPath("$[0].tipo").value("LAGER"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].nome").value("Skol"))
                .andExpect(jsonPath("$[1].marca").value("Ambev"))
                .andExpect(jsonPath("$[1].maximo").value(120))
                .andExpect(jsonPath("$[1].quantidade").value(80))
                .andExpect(jsonPath("$[1].tipo").value("PILSEN"))
                .andExpect(jsonPath("$[2].id").value(3))
                .andExpect(jsonPath("$[2].nome").value("Colorado"))
                .andExpect(jsonPath("$[2].marca").value("Colorado"))
                .andExpect(jsonPath("$[2].maximo").value(60))
                .andExpect(jsonPath("$[2].quantidade").value(25))
                .andExpect(jsonPath("$[2].tipo").value("IPA"));

        verify(cervejaService, times(1)).listarTudo();
    }

    @Test
    void deveRetornar200_QuandoListaVazia() throws Exception {
        // Arrange
        when(cervejaService.listarTudo()).thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/cervejas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0))
                .andExpect(content().json("[]"));

        verify(cervejaService, times(1)).listarTudo();
    }

    @Test
    void deveRetornar200_QuandoListarApenasUmaCerveja() throws Exception {
        // Arrange
        List<CervejaResponseDTO> cervejas = List.of(
                new CervejaResponseDTO(1L, "Heineken", "Heineken", 100, 50, TipoCerveja.LAGER)
        );

        when(cervejaService.listarTudo()).thenReturn(cervejas);

        // Act & Assert
        mockMvc.perform(get("/api/cervejas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Heineken"))
                .andExpect(jsonPath("$[0].marca").value("Heineken"))
                .andExpect(jsonPath("$[0].maximo").value(100))
                .andExpect(jsonPath("$[0].quantidade").value(50))
                .andExpect(jsonPath("$[0].tipo").value("LAGER"));

        verify(cervejaService, times(1)).listarTudo();
    }

    @Test
    void deveRetornar204_QuandoDeletarCervejaComSucesso() throws Exception {
        // Arrange
        Long id = 1L;

        doNothing().when(cervejaService).deletarPorId(id);

        // Act & Assert
        mockMvc.perform(delete("/api/cervejas/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
                .andExpect(content().string("")); // Response body vazio

        verify(cervejaService, times(1)).deletarPorId(id);
    }

    @Test
    void deveRetornar404_QuandoDeletarCervejaInexistente() throws Exception {
        // Arrange
        Long idInexistente = 999L;

        doThrow(new CervejaNaoEncontradaException("Cerveja não encontrada com id: " + idInexistente))
                .when(cervejaService).deletarPorId(idInexistente);

        // Act & Assert - ESTRUTURA REAL
        mockMvc.perform(delete("/api/cervejas/{id}", idInexistente)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.mensagem").value("Cerveja não encontrada com id: " + idInexistente))
                .andExpect(jsonPath("$.caminho").value("/api/cervejas/" + idInexistente))
                .andExpect(jsonPath("$.timestamp").exists()); // Verifica que timestamp existe

        verify(cervejaService, times(1)).deletarPorId(idInexistente);
    }

    @Test
    void deveRetornar400_QuandoQuantidadeExcedeMaximo() throws Exception {
        // Arrange
        CervejaRequestDTO requestDTO = new CervejaRequestDTO(
                "Heineken",
                "Heineken",
                100,
                150, // quantidade > máximo
                TipoCerveja.LAGER
        );

        when(cervejaService.criarCerveja(any(CervejaRequestDTO.class)))
                .thenThrow(new IllegalArgumentException("A quantidade inicial não pode exceder o máximo permitido!"));

        // Act & Assert
        mockMvc.perform(post("/api/cervejas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erro").value("Erro de validação"))
                .andExpect(jsonPath("$.mensagem").value("A quantidade inicial não pode exceder o máximo permitido!"))
                .andExpect(jsonPath("$.caminho").value("/api/cervejas"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(cervejaService, times(1)).criarCerveja(any(CervejaRequestDTO.class));
    }

    @Test
    void deveRetornar409_QuandoCervejaJaExiste() throws Exception {
        // Arrange
        CervejaRequestDTO requestDTO = new CervejaRequestDTO(
                "Heineken",
                "Heineken",
                100,
                50,
                TipoCerveja.LAGER
        );

        when(cervejaService.criarCerveja(any(CervejaRequestDTO.class)))
                .thenThrow(new CervejaJaExisteException("Já existe uma cerveja cadastrada com o nome: Heineken"));

        // Act & Assert
        mockMvc.perform(post("/api/cervejas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.erro").value("Conflito de dados"))
                .andExpect(jsonPath("$.mensagem").value("Já existe uma cerveja cadastrada com o nome: Heineken"))
                .andExpect(jsonPath("$.caminho").value("/api/cervejas"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(cervejaService, times(1)).criarCerveja(any(CervejaRequestDTO.class));
    }

    @Test
    void deveRetornar500_QuandoErroInternoAoListar() throws Exception {
        // Arrange
        when(cervejaService.listarTudo())
                .thenThrow(new RuntimeException("Erro de conexão com o banco de dados"));

        // Act & Assert
        mockMvc.perform(get("/api/cervejas")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.erro").value("Erro interno no servidor"))
                .andExpect(jsonPath("$.mensagem").value("Erro de conexão com o banco de dados"))
                .andExpect(jsonPath("$.caminho").value("/api/cervejas"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(cervejaService, times(1)).listarTudo();
    }

    @Test
    void deveRetornar500_QuandoErroInternoAoDeletar() throws Exception {
        // Arrange
        Long id = 1L;

        doThrow(new RuntimeException("Erro de conexão com o banco"))
                .when(cervejaService).deletarPorId(id);

        // Act & Assert
        mockMvc.perform(delete("/api/cervejas/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.erro").value("Erro interno no servidor"))
                .andExpect(jsonPath("$.mensagem").value("Erro de conexão com o banco"))
                .andExpect(jsonPath("$.caminho").value("/api/cervejas/" + id))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(cervejaService, times(1)).deletarPorId(id);
    }

    @Test
    void deveRetornar200_QuandoIncrementarEstoqueComSucesso() throws Exception {
        // Arrange
        Long id = 1L;
        CervejaEstoqueIncrementadoDTO requestDTO = new CervejaEstoqueIncrementadoDTO(10);

        CervejaResponseDTO responseDTO = new CervejaResponseDTO(
                id,
                "Heineken",
                "Heineken",
                100,
                60, // Novo estoque após incremento
                TipoCerveja.LAGER
        );

        when(cervejaService.incrementarEstoque(eq(id), eq(10))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(patch("/api/cervejas/{id}/incrementar", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nome").value("Heineken"))
                .andExpect(jsonPath("$.marca").value("Heineken"))
                .andExpect(jsonPath("$.maximo").value(100))
                .andExpect(jsonPath("$.quantidade").value(60))
                .andExpect(jsonPath("$.tipo").value("LAGER"));

        verify(cervejaService, times(1)).incrementarEstoque(id, 10);
    }

    @Test
    void deveRetornar404_QuandoIncrementarCervejaInexistente() throws Exception {
        // Arrange
        Long idInexistente = 99L;
        CervejaEstoqueIncrementadoDTO requestDTO = new CervejaEstoqueIncrementadoDTO(10);

        when(cervejaService.incrementarEstoque(eq(idInexistente), eq(10)))
                .thenThrow(new CervejaNaoEncontradaException("Cerveja não encontrada com id: " + idInexistente));

        // Act & Assert - CORRIGIDO: caminho inclui "/incrementar"
        mockMvc.perform(patch("/api/cervejas/{id}/incrementar", idInexistente)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.mensagem").value("Cerveja não encontrada com id: " + idInexistente))
                .andExpect(jsonPath("$.caminho").value("/api/cervejas/" + idInexistente + "/incrementar")) // CORRIGIDO
                .andExpect(jsonPath("$.timestamp").exists());

        verify(cervejaService, times(1)).incrementarEstoque(idInexistente, 10);
    }


    @Test
    void deveRetornar400_QuandoIncrementarEstoqueExcedeMaximo() throws Exception {
        // Arrange
        Long id = 1L;
        CervejaEstoqueIncrementadoDTO requestDTO = new CervejaEstoqueIncrementadoDTO(1000);

        when(cervejaService.incrementarEstoque(eq(id), eq(1000)))
                .thenThrow(new EstoqueExcedidoException(id, 1000));

        // Act & Assert - MENSAGEM CORRIGIDA
        mockMvc.perform(patch("/api/cervejas/{id}/incrementar", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erro").value("Erro de validação"))
                .andExpect(jsonPath("$.mensagem").value("Não é possível incrementar 1000 unidades. Estoque máximo excedido para a cerveja com id: 1"))
                .andExpect(jsonPath("$.caminho").value("/api/cervejas/" + id + "/incrementar"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(cervejaService, times(1)).incrementarEstoque(id, 1000);
    }

    @Test
    void deveRetornar400_QuandoQuantidadeParaIncrementarInvalida() throws Exception {
        // Arrange
        Long id = 1L;
        CervejaEstoqueIncrementadoDTO requestDTO = new CervejaEstoqueIncrementadoDTO(0); // inválido, pois @Positive

        // Act & Assert
        mockMvc.perform(patch("/api/cervejas/{id}/incrementar", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isBadRequest());

        verify(cervejaService, never()).incrementarEstoque(anyLong(), anyInt());
    }

    @Test
    void deveRetornar500_QuandoErroInternoAoIncrementar() throws Exception {
        // Arrange
        Long id = 1L;
        CervejaEstoqueIncrementadoDTO requestDTO = new CervejaEstoqueIncrementadoDTO(5);

        when(cervejaService.incrementarEstoque(eq(id), eq(5)))
                .thenThrow(new RuntimeException("Erro inesperado no banco de dados"));

        // Act & Assert
        mockMvc.perform(patch("/api/cervejas/{id}/incrementar", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.erro").value("Erro interno no servidor"))
                .andExpect(jsonPath("$.mensagem").value("Erro inesperado no banco de dados"))
                .andExpect(jsonPath("$.caminho").value("/api/cervejas/" + id + "/incrementar"))
                .andExpect(jsonPath("$.timestamp").exists());

        verify(cervejaService, times(1)).incrementarEstoque(id, 5);
    }

}
