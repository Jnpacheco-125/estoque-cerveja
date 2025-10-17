package br.com.dio.estoque_cerveja.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CervejaEstoqueIncrementadoDTO(
        @NotNull
        @Positive(message = "A quantidade deve ser positiva")
        Integer quantidade
) {}
