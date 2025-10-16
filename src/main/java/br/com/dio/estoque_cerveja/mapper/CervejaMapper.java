package br.com.dio.estoque_cerveja.mapper;

import br.com.dio.estoque_cerveja.dto.CervejaRequestDTO;
import br.com.dio.estoque_cerveja.dto.CervejaResponseDTO;
import br.com.dio.estoque_cerveja.entity.Cerveja;

public class CervejaMapper {
    public static Cerveja toEntity(CervejaRequestDTO dto) {
        return Cerveja.builder()
                .nome(dto.nome())
                .marca(dto.marca())
                .maximo(dto.maximo())
                .quantidade(dto.quantidade())
                .tipo(dto.tipo())
                .build();
    }

    public static CervejaResponseDTO toDTO(Cerveja entity) {
        return new CervejaResponseDTO(
                entity.getId(),
                entity.getNome(),
                entity.getMarca(),
                entity.getMaximo(),
                entity.getQuantidade(),
                entity.getTipo()
        );
    }
}
