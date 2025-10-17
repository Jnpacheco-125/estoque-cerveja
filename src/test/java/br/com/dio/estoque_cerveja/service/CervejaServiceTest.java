package br.com.dio.estoque_cerveja.service;


import br.com.dio.estoque_cerveja.dto.CervejaRequestDTO;
import br.com.dio.estoque_cerveja.dto.CervejaResponseDTO;
import br.com.dio.estoque_cerveja.entity.Cerveja;
import br.com.dio.estoque_cerveja.enums.TipoCerveja;
import br.com.dio.estoque_cerveja.exception.CervejaJaExisteException;
import br.com.dio.estoque_cerveja.exception.CervejaNaoEncontradaException;
import br.com.dio.estoque_cerveja.exception.EstoqueExcedidoException;
import br.com.dio.estoque_cerveja.repository.CervejaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CervejaServiceTest {

    @Mock
    private CervejaRepository repository;

    @InjectMocks
    private CervejaService cervejaService;

    private CervejaRequestDTO criarRequestDTOValido() {
        return new CervejaRequestDTO(
                "Heineken",
                "Heineken",
                100,
                50,
                TipoCerveja.LAGER
        );
    }

    private Cerveja criarCervejaValida() {
        return Cerveja.builder()
                .id(1L)
                .nome("Heineken")
                .marca("Heineken")
                .maximo(100)
                .quantidade(50)
                .tipo(TipoCerveja.LAGER)
                .build();
    }

    private Cerveja criarCervejaExistente() {
        return Cerveja.builder()
                .id(2L)
                .nome("Heineken")
                .marca("Heineken")
                .maximo(80)
                .quantidade(30)
                .tipo(TipoCerveja.LAGER)
                .build();
    }

    private Cerveja criarCerveja(Long id, String nome, String marca, Integer maximo, Integer quantidade, TipoCerveja tipo) {
        return Cerveja.builder()
                .id(id)
                .nome(nome)
                .marca(marca)
                .maximo(maximo)
                .quantidade(quantidade)
                .tipo(tipo)
                .build();
    }

    private List<Cerveja> criarListaCervejas() {
        return Arrays.asList(
                criarCerveja(1L, "Heineken", "Heineken", 100, 50, TipoCerveja.LAGER),
                criarCerveja(2L, "Skol", "Ambev", 120, 80, TipoCerveja.PILSEN),
                criarCerveja(3L, "Colorado", "Colorado", 60, 25, TipoCerveja.IPA)
        );
    }

    @Test
    void deveCriarCervejaComSucesso_QuandoDadosValidos() {
        // Arrange
        CervejaRequestDTO requestDTO = criarRequestDTOValido();
        Cerveja cervejaSalva = criarCervejaValida();

        when(repository.findByNome(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Cerveja.class))).thenReturn(cervejaSalva);

        // Act
        CervejaResponseDTO resultado = cervejaService.criarCerveja(requestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("Heineken", resultado.nome());
        assertEquals("Heineken", resultado.marca());
        assertEquals(100, resultado.maximo());
        assertEquals(50, resultado.quantidade());
        assertEquals(TipoCerveja.LAGER, resultado.tipo());

        verify(repository, times(1)).findByNome("Heineken");
        verify(repository, times(1)).save(any(Cerveja.class));
    }

    @Test
    void deveCriarCervejaComQuantidadeZero_QuandoDadosValidos() {
        // Arrange
        CervejaRequestDTO requestDTO = new CervejaRequestDTO(
                "Skol",
                "Ambev",
                100,
                0,  // Quantidade zero permitida
                TipoCerveja.PILSEN
        );

        Cerveja cervejaSalva = Cerveja.builder()
                .id(1L)
                .nome("Skol")
                .marca("Ambev")
                .maximo(100)
                .quantidade(0)
                .tipo(TipoCerveja.PILSEN)
                .build();

        when(repository.findByNome(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Cerveja.class))).thenReturn(cervejaSalva);

        // Act
        CervejaResponseDTO resultado = cervejaService.criarCerveja(requestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(0, resultado.quantidade());
        verify(repository, times(1)).findByNome("Skol");
        verify(repository, times(1)).save(any(Cerveja.class));
    }

    @Test
    void deveCriarCervejaComQuantidadeIgualMaximo() {
        // Arrange
        CervejaRequestDTO requestDTO = new CervejaRequestDTO(
                "Stella Artois",
                "Ambev",
                100,
                100,  // Quantidade = máximo
                TipoCerveja.PILSEN
        );

        Cerveja cervejaSalva = Cerveja.builder()
                .id(1L)
                .nome("Stella Artois")
                .marca("Ambev")
                .maximo(100)
                .quantidade(100)
                .tipo(TipoCerveja.PILSEN)
                .build();

        when(repository.findByNome(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Cerveja.class))).thenReturn(cervejaSalva);

        // Act
        CervejaResponseDTO resultado = cervejaService.criarCerveja(requestDTO);

        // Assert
        assertNotNull(resultado);
        assertEquals(100, resultado.quantidade());
        assertEquals(100, resultado.maximo());
        verify(repository, times(1)).save(any(Cerveja.class));
    }

    @Test
    void deveLancarCervejaJaExisteException_QuandoCervejaComMesmoNomeJaExiste() {
        // Arrange
        CervejaRequestDTO requestDTO = criarRequestDTOValido();
        Cerveja cervejaExistente = criarCervejaExistente();

        when(repository.findByNome(anyString())).thenReturn(Optional.of(cervejaExistente));

        // Act & Assert
        CervejaJaExisteException exception = assertThrows(
                CervejaJaExisteException.class,
                () -> cervejaService.criarCerveja(requestDTO)
        );

        assertEquals("Já existe uma cerveja cadastrada com o nome: Heineken", exception.getMessage());
        verify(repository, times(1)).findByNome("Heineken");
        verify(repository, never()).save(any(Cerveja.class));
    }

    @Test
    void deveLancarIllegalArgumentException_QuandoQuantidadeExcedeMaximo() {
        // Arrange
        CervejaRequestDTO requestDTO = new CervejaRequestDTO(
                "Heineken",
                "Heineken",
                100,
                150,
                TipoCerveja.LAGER
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cervejaService.criarCerveja(requestDTO)
        );

        // Verifica a mensagem da exceção
        assertEquals("A quantidade inicial não pode exceder o máximo permitido!", exception.getMessage());

        // Verifica que NÃO houve nenhuma interação com o repository
        verifyNoInteractions(repository);
    }

    @Test
    void deveLancarIllegalArgumentException_QuandoQuantidadeNegativa() {
        // Arrange
        CervejaRequestDTO requestDTO = new CervejaRequestDTO(
                "Heineken",
                "Heineken",
                100,
                -5,   // quantidade negativa
                TipoCerveja.LAGER
        );

        // NÃO mockamos o repository porque a validação deve acontecer ANTES de qualquer chamada ao repository
        // quando você reordenar as validações no service

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cervejaService.criarCerveja(requestDTO)
        );

        // A mensagem exata depende da sua implementação
        assertTrue(exception.getMessage().contains("quantidade") ||
                exception.getMessage().contains("negativa") ||
                exception.getMessage().contains("máximo"));

        // Verifica que NÃO chegou a chamar o repository ou mapper
        verify(repository, never()).findByNome(anyString());
        verify(repository, never()).save(any(Cerveja.class));
    }

    @Test
    void deveVerificarNomeAntesDeSalvar() {
        // Arrange
        CervejaRequestDTO requestDTO = criarRequestDTOValido();
        Cerveja cervejaSalva = criarCervejaValida();

        when(repository.findByNome(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Cerveja.class))).thenReturn(cervejaSalva);

        // Act
        cervejaService.criarCerveja(requestDTO);

        // Assert - Verifica a ordem das chamadas
        var inOrder = inOrder(repository);
        inOrder.verify(repository).findByNome("Heineken");
        inOrder.verify(repository).save(any(Cerveja.class));
    }

    @Test
    void deveUsarMapperParaConversao() {
        // Arrange
        CervejaRequestDTO requestDTO = criarRequestDTOValido();
        Cerveja cervejaSalva = criarCervejaValida();

        when(repository.findByNome(anyString())).thenReturn(Optional.empty());
        when(repository.save(any(Cerveja.class))).thenReturn(cervejaSalva);

        // Act
        CervejaResponseDTO resultado = cervejaService.criarCerveja(requestDTO);

        // Assert - Verifica se o resultado tem os dados corretos
        assertEquals(requestDTO.nome(), resultado.nome());
        assertEquals(requestDTO.marca(), resultado.marca());
        assertEquals(requestDTO.maximo(), resultado.maximo());
        assertEquals(requestDTO.quantidade(), resultado.quantidade());
        assertEquals(requestDTO.tipo(), resultado.tipo());
    }

    @Test
    void devePermitirCervejasComNomesDiferentes() {
        // Arrange
        CervejaRequestDTO requestDTO1 = new CervejaRequestDTO(
                "Heineken",
                "Heineken",
                100,
                50,
                TipoCerveja.LAGER
        );

        CervejaRequestDTO requestDTO2 = new CervejaRequestDTO(
                "Heineken Silver",  // Nome diferente
                "Heineken",
                80,
                40,
                TipoCerveja.LAGER
        );

        Cerveja cervejaSalva1 = Cerveja.builder().id(1L).build();
        Cerveja cervejaSalva2 = Cerveja.builder().id(2L).build();

        when(repository.findByNome("Heineken")).thenReturn(Optional.empty());
        when(repository.findByNome("Heineken Silver")).thenReturn(Optional.empty());
        when(repository.save(any(Cerveja.class))).thenReturn(cervejaSalva1, cervejaSalva2);

        // Act & Assert - Ambas devem ser criadas com sucesso
        assertDoesNotThrow(() -> {
            cervejaService.criarCerveja(requestDTO1);
            cervejaService.criarCerveja(requestDTO2);
        });

        verify(repository, times(2)).findByNome(anyString());
        verify(repository, times(2)).save(any(Cerveja.class));
    }

    //TESTES PARA encontrarPorNome()
    @Test
    void deveEncontrarCervejaPorNome_QuandoCervejaExiste() {
        // Arrange
        String nome = "Heineken";
        Cerveja cerveja = criarCerveja(1L, nome, "Heineken", 100, 50, TipoCerveja.LAGER);

        when(repository.findByNome(nome)).thenReturn(Optional.of(cerveja));

        // Act
        CervejaResponseDTO resultado = cervejaService.encontrarPorNome(nome);

        // Assert
        assertNotNull(resultado);
        assertEquals(1L, resultado.id());
        assertEquals("Heineken", resultado.nome());
        assertEquals("Heineken", resultado.marca());
        assertEquals(100, resultado.maximo());
        assertEquals(50, resultado.quantidade());
        assertEquals(TipoCerveja.LAGER, resultado.tipo());

        verify(repository, times(1)).findByNome(nome);
    }

    @Test
    void deveLancarExcecao_QuandoEncontrarPorNomeComCervejaInexistente() {
        // Arrange
        String nome = "CervejaInexistente";
        when(repository.findByNome(nome)).thenReturn(Optional.empty());

        // Act & Assert
        CervejaNaoEncontradaException exception = assertThrows(
                CervejaNaoEncontradaException.class,
                () -> cervejaService.encontrarPorNome(nome)
        );

        assertEquals("Cerveja não encontrada com o nome: " + nome, exception.getMessage());
        verify(repository, times(1)).findByNome(nome);
    }

    @Test
    void deveEncontrarCervejaPorNome_ComCaseSensitive() {
        // Arrange
        String nomeBusca = "heineken"; // lowercase
        String nomeSalvo = "Heineken"; // uppercase
        Cerveja cerveja = criarCerveja(1L, nomeSalvo, "Heineken", 100, 50, TipoCerveja.LAGER);

        when(repository.findByNome(nomeBusca)).thenReturn(Optional.of(cerveja));

        // Act
        CervejaResponseDTO resultado = cervejaService.encontrarPorNome(nomeBusca);

        // Assert
        assertNotNull(resultado);
        assertEquals("Heineken", resultado.nome());
        verify(repository, times(1)).findByNome(nomeBusca);
    }

    @Test
    void deveLancarExcecao_QuandoEncontrarPorNomeComNomeNulo() {
        // Arrange
        String nomeNulo = null;

        // Act & Assert
        assertThrows(
                Exception.class, // Pode ser NullPointerException ou CervejaNaoEncontradaException
                () -> cervejaService.encontrarPorNome(nomeNulo)
        );

        // O comportamento depende de como o repository lida com null
    }

    @Test
    void deveLancarExcecao_QuandoEncontrarPorNomeComNomeVazio() {
        // Arrange
        String nomeVazio = "";
        when(repository.findByNome(nomeVazio)).thenReturn(Optional.empty());

        // Act & Assert
        CervejaNaoEncontradaException exception = assertThrows(
                CervejaNaoEncontradaException.class,
                () -> cervejaService.encontrarPorNome(nomeVazio)
        );

        assertEquals("Cerveja não encontrada com o nome: " + nomeVazio, exception.getMessage());
        verify(repository, times(1)).findByNome(nomeVazio);
    }

    // ===== TESTES PARA listarTudo() =====

    @Test
    void deveListarTodasCervejas_QuandoExistiremCervejas() {
        // Arrange
        List<Cerveja> cervejas = criarListaCervejas();
        when(repository.findAll()).thenReturn(cervejas);

        // Act
        List<CervejaResponseDTO> resultado = cervejaService.listarTudo();

        // Assert
        assertNotNull(resultado);
        assertEquals(3, resultado.size());

        // Verifica primeira cerveja
        assertEquals(1L, resultado.get(0).id());
        assertEquals("Heineken", resultado.get(0).nome());

        // Verifica segunda cerveja
        assertEquals(2L, resultado.get(1).id());
        assertEquals("Skol", resultado.get(1).nome());

        // Verifica terceira cerveja
        assertEquals(3L, resultado.get(2).id());
        assertEquals("Colorado", resultado.get(2).nome());

        verify(repository, times(1)).findAll();
    }

    @Test
    void deveRetornarListaVazia_QuandoNaoExistiremCervejas() {
        // Arrange
        when(repository.findAll()).thenReturn(List.of());

        // Act
        List<CervejaResponseDTO> resultado = cervejaService.listarTudo();

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(repository, times(1)).findAll();
    }

    @Test
    void deveConverterTodasEntidadesParaDTO_QuandoListarTudo() {
        // Arrange
        List<Cerveja> cervejas = Arrays.asList(
                criarCerveja(1L, "Cerveja1", "Marca1", 100, 50, TipoCerveja.LAGER),
                criarCerveja(2L, "Cerveja2", "Marca2", 80, 30, TipoCerveja.PILSEN)
        );

        when(repository.findAll()).thenReturn(cervejas);

        // Act
        List<CervejaResponseDTO> resultado = cervejaService.listarTudo();

        // Assert
        assertEquals(2, resultado.size());

        // Verifica se todos os elementos foram convertidos corretamente
        for (int i = 0; i < cervejas.size(); i++) {
            assertEquals(cervejas.get(i).getId(), resultado.get(i).id());
            assertEquals(cervejas.get(i).getNome(), resultado.get(i).nome());
            assertEquals(cervejas.get(i).getMarca(), resultado.get(i).marca());
            assertEquals(cervejas.get(i).getMaximo(), resultado.get(i).maximo());
            assertEquals(cervejas.get(i).getQuantidade(), resultado.get(i).quantidade());
            assertEquals(cervejas.get(i).getTipo(), resultado.get(i).tipo());
        }

        verify(repository, times(1)).findAll();
    }

    // ===== TESTES PARA deletarPorId() =====

    @Test
    void deveDeletarCervejaPorId_QuandoCervejaExiste() {
        // Arrange
        Long id = 1L;
        when(repository.existsById(id)).thenReturn(true);
        doNothing().when(repository).deleteById(id);

        // Act & Assert
        assertDoesNotThrow(() -> cervejaService.deletarPorId(id));

        verify(repository, times(1)).existsById(id);
        verify(repository, times(1)).deleteById(id);
    }

    @Test
    void deveLancarExcecao_QuandoDeletarPorIdComCervejaInexistente() {
        // Arrange
        Long id = 999L;
        when(repository.existsById(id)).thenReturn(false);

        // Act & Assert
        CervejaNaoEncontradaException exception = assertThrows(
                CervejaNaoEncontradaException.class,
                () -> cervejaService.deletarPorId(id)
        );

        assertEquals("Cerveja não encontrada com id: " + id, exception.getMessage());
        verify(repository, times(1)).existsById(id);
        verify(repository, never()).deleteById(id);
    }
    @Test
    void deveLancarExcecao_QuandoDeletarPorIdComIdNulo() {
        // Arrange
        Long idNulo = null;

        // Act & Assert
        assertThrows(
                Exception.class, // Pode ser NullPointerException ou CervejaNaoEncontradaException
                () -> cervejaService.deletarPorId(idNulo)
        );

        // O comportamento depende de como o repository lida com null
    }

    @Test
    void deveDeletarCerveja_QuandoExisteByIdRetornaTrue() {
        // Arrange
        Long id = 5L;
        when(repository.existsById(id)).thenReturn(true);
        doNothing().when(repository).deleteById(id);

        // Act
        cervejaService.deletarPorId(id);

        // Assert
        verify(repository, times(1)).existsById(id);
        verify(repository, times(1)).deleteById(id);

        // Verifica a ordem das chamadas
        var inOrder = inOrder(repository);
        inOrder.verify(repository).existsById(id);
        inOrder.verify(repository).deleteById(id);
    }

    @Test
    void deveNaoChamarDelete_QuandoExistsByIdRetornaFalse() {
        // Arrange
        Long id = 999L;
        when(repository.existsById(id)).thenReturn(false);

        // Act & Assert
        assertThrows(
                CervejaNaoEncontradaException.class,
                () -> cervejaService.deletarPorId(id)
        );

        verify(repository, times(1)).existsById(id);
        verify(repository, never()).deleteById(anyLong());
    }

    // ===== TESTES DE INTEGRAÇÃO ENTRE MÉTODOS =====

    @Test
    void deveEncontrarCervejaQueFoiCriada() {
        // Arrange - Setup para criação
        Cerveja cervejaSalva = criarCerveja(1L, "NovaCerveja", "NovaMarca", 100, 50, TipoCerveja.IPA);
        when(repository.findByNome("NovaCerveja")).thenReturn(Optional.of(cervejaSalva));

        // Act - Busca pela cerveja
        CervejaResponseDTO resultado = cervejaService.encontrarPorNome("NovaCerveja");

        // Assert
        assertNotNull(resultado);
        assertEquals("NovaCerveja", resultado.nome());
        assertEquals("NovaMarca", resultado.marca());
    }

    @Test
    void deveListarCervejaAposCriacao() {
        // Arrange
        Cerveja cerveja1 = criarCerveja(1L, "Cerveja1", "Marca1", 100, 50, TipoCerveja.LAGER);
        Cerveja cerveja2 = criarCerveja(2L, "Cerveja2", "Marca2", 80, 30, TipoCerveja.PILSEN);

        when(repository.findAll()).thenReturn(Arrays.asList(cerveja1, cerveja2));

        // Act
        List<CervejaResponseDTO> resultado = cervejaService.listarTudo();

        // Assert
        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().anyMatch(c -> c.nome().equals("Cerveja1")));
        assertTrue(resultado.stream().anyMatch(c -> c.nome().equals("Cerveja2")));
    }

    @Test
    void deveRemoverCervejaDaListaAposDelecao() {
        // Arrange - Setup inicial com 3 cervejas
        List<Cerveja> cervejasIniciais = criarListaCervejas();
        when(repository.findAll()).thenReturn(cervejasIniciais);

        // Act & Assert - Verifica lista inicial
        List<CervejaResponseDTO> listaInicial = cervejaService.listarTudo();
        assertEquals(3, listaInicial.size());

        // Arrange - Setup para deleção
        Long idParaDeletar = 2L; // Skol
        when(repository.existsById(idParaDeletar)).thenReturn(true);
        doNothing().when(repository).deleteById(idParaDeletar);

        // Act - Deleta uma cerveja
        cervejaService.deletarPorId(idParaDeletar);

        // Assert - Verifica que delete foi chamado
        verify(repository, times(1)).deleteById(idParaDeletar);
    }

    // ===== TESTES PARA incrementarEstoque() no CervejaServiceTest =====

    @Test
    void deveIncrementarEstoque_QuandoQuantidadeValida() {
        // Arrange
        Long id = 1L;
        Integer quantidadeParaIncrementar = 10;

        Cerveja cerveja = Cerveja.builder()
                .id(id)
                .nome("Heineken")
                .marca("Heineken")
                .maximo(100)
                .quantidade(50)
                .tipo(TipoCerveja.LAGER)
                .build();

        Cerveja cervejaAtualizada = Cerveja.builder()
                .id(id)
                .nome("Heineken")
                .marca("Heineken")
                .maximo(100)
                .quantidade(60) // 50 + 10
                .tipo(TipoCerveja.LAGER)
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(cerveja));
        when(repository.save(any(Cerveja.class))).thenReturn(cervejaAtualizada);

        // Act
        CervejaResponseDTO resultado = cervejaService.incrementarEstoque(id, quantidadeParaIncrementar);

        // Assert
        assertNotNull(resultado);
        assertEquals(60, resultado.quantidade());
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(any(Cerveja.class));
    }

    @Test
    void deveLancarExcecao_QuandoCervejaNaoEncontrada() {
        // Arrange
        Long id = 999L;
        Integer quantidadeParaIncrementar = 10;

        when(repository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(CervejaNaoEncontradaException.class,
                () -> cervejaService.incrementarEstoque(id, quantidadeParaIncrementar));

        verify(repository, times(1)).findById(id);
        verify(repository, never()).save(any(Cerveja.class));
    }

    @Test
    void deveLancarExcecao_QuandoEstoqueExcedido() {
        // Arrange
        Long id = 1L;
        Integer quantidadeParaIncrementar = 60; // Vai exceder o máximo

        Cerveja cerveja = Cerveja.builder()
                .id(id)
                .nome("Heineken")
                .marca("Heineken")
                .maximo(100)
                .quantidade(50)
                .tipo(TipoCerveja.LAGER)
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(cerveja));

        // Act & Assert
        EstoqueExcedidoException exception = assertThrows(EstoqueExcedidoException.class,
                () -> cervejaService.incrementarEstoque(id, quantidadeParaIncrementar));

        assertTrue(exception.getMessage().contains("Não é possível incrementar"));
        assertTrue(exception.getMessage().contains("60"));
        assertTrue(exception.getMessage().contains("1"));

        verify(repository, times(1)).findById(id);
        verify(repository, never()).save(any(Cerveja.class));
    }

    @Test
    void deveIncrementarEstoque_QuandoQuantidadeIgualMaximo() {
        // Arrange
        Long id = 1L;
        Integer quantidadeParaIncrementar = 50; // Vai atingir exatamente o máximo

        Cerveja cerveja = Cerveja.builder()
                .id(id)
                .nome("Heineken")
                .marca("Heineken")
                .maximo(100)
                .quantidade(50)
                .tipo(TipoCerveja.LAGER)
                .build();

        Cerveja cervejaAtualizada = Cerveja.builder()
                .id(id)
                .nome("Heineken")
                .marca("Heineken")
                .maximo(100)
                .quantidade(100) // 50 + 50 = máximo
                .tipo(TipoCerveja.LAGER)
                .build();

        when(repository.findById(id)).thenReturn(Optional.of(cerveja));
        when(repository.save(any(Cerveja.class))).thenReturn(cervejaAtualizada);

        // Act
        CervejaResponseDTO resultado = cervejaService.incrementarEstoque(id, quantidadeParaIncrementar);

        // Assert
        assertNotNull(resultado);
        assertEquals(100, resultado.quantidade());
        assertEquals(100, resultado.maximo());
    }

    @Test
    void deveLancarExcecao_QuandoQuantidadeZero() {
        // Arrange
        Long id = 1L;
        Integer quantidadeInvalida = 0;

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> cervejaService.incrementarEstoque(id, quantidadeInvalida));

        // Verifica que não houve acesso ao repositório
        verifyNoInteractions(repository);
    }
    @Test
    void deveLancarExcecao_QuandoQuantidadeNegativa() {
        // Arrange
        Long id = 1L;
        Integer quantidadeNegativa = -5;

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> cervejaService.incrementarEstoque(id, quantidadeNegativa));

        // Verifica que não houve acesso ao repositório
        verifyNoInteractions(repository);
    }

}
