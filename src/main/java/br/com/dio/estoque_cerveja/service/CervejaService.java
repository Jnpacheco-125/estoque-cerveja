package br.com.dio.estoque_cerveja.service;

import br.com.dio.estoque_cerveja.dto.CervejaRequestDTO;
import br.com.dio.estoque_cerveja.dto.CervejaResponseDTO;
import br.com.dio.estoque_cerveja.entity.Cerveja;
import br.com.dio.estoque_cerveja.exception.CervejaJaExisteException;
import br.com.dio.estoque_cerveja.exception.CervejaNaoEncontradaException;
import br.com.dio.estoque_cerveja.exception.EstoqueExcedidoException;
import br.com.dio.estoque_cerveja.mapper.CervejaMapper;
import br.com.dio.estoque_cerveja.repository.CervejaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CervejaService {

    @Autowired
    private CervejaRepository repository;

    public CervejaResponseDTO criarCerveja(CervejaRequestDTO dto) {
        // VALIDAÇÃO 1: Quantidade não pode ser negativa (DEVE VIR PRIMEIRO)
        if (dto.quantidade() < 0) {
            throw new IllegalArgumentException("A quantidade não pode ser negativa!");
        }

        // VALIDAÇÃO 2: Quantidade não pode exceder o máximo
        if (dto.quantidade() > dto.maximo()) {
            throw new IllegalArgumentException("A quantidade inicial não pode exceder o máximo permitido!");
        }

        // VALIDAÇÃO 3: Verifica se já existe (SÓ DEPOIS das validações básicas)
        repository.findByNome(dto.nome()).ifPresent(c -> {
            throw new CervejaJaExisteException("Já existe uma cerveja cadastrada com o nome: " + dto.nome());
        });

        // SÓ AQUI conversão e salvamento
        Cerveja cerveja = CervejaMapper.toEntity(dto);
        Cerveja salva = repository.save(cerveja);
        return CervejaMapper.toDTO(salva);
    }

    public CervejaResponseDTO encontrarPorNome(String nome) {
        Cerveja cerveja = repository.findByNome(nome)
                .orElseThrow(() -> new CervejaNaoEncontradaException("Cerveja não encontrada com o nome: " + nome));
        return CervejaMapper.toDTO(cerveja);
    }

    public List<CervejaResponseDTO> listarTudo() {
        return repository.findAll()
                .stream()
                .map(CervejaMapper::toDTO)
                .toList();
    }

    public void deletarPorId(Long id) {
        if (!repository.existsById(id)) {
            throw new CervejaNaoEncontradaException("Cerveja não encontrada com id: " + id);
        }
        repository.deleteById(id);
    }


    public CervejaResponseDTO incrementarEstoque(Long id, Integer quantidadeParaIncrementar) {
        // Busca a cerveja
        if (quantidadeParaIncrementar <= 0) {
            throw new IllegalArgumentException("Quantidade deve ser maior que zero");
        }
        Cerveja cerveja = repository.findById(id)
                .orElseThrow(() -> new CervejaNaoEncontradaException("Cerveja não encontrada com id: " + id));

        // Calcula a quantidade após incremento
        int quantidadeAposIncremento = cerveja.getQuantidade() + quantidadeParaIncrementar;

        // Verifica se não excede o máximo
        if (quantidadeAposIncremento <= cerveja.getMaximo()) {
            cerveja.setQuantidade(quantidadeAposIncremento);
            Cerveja cervejaAtualizada = repository.save(cerveja);
            return CervejaMapper.toDTO(cervejaAtualizada);
        }

        throw new EstoqueExcedidoException(id, quantidadeParaIncrementar);
    }
}
