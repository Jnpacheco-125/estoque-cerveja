package br.com.dio.estoque_cerveja.dto;

import br.com.dio.estoque_cerveja.enums.TipoCerveja;

public record CervejaResponseDTO(Long id,
                                 String nome,
                                 String marca,
                                 Integer maximo,
                                 Integer quantidade,
                                 TipoCerveja tipo) {
}
