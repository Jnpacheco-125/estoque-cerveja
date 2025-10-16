package br.com.dio.estoque_cerveja.dto;

import br.com.dio.estoque_cerveja.enums.TipoCerveja;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CervejaRequestDTO( @NotBlank(message = "O nome da cerveja é obrigatório")
                                 String nome,

                                 @NotBlank(message = "A marca da cerveja é obrigatória")
                                 String marca,

                                 @NotNull(message = "O valor máximo é obrigatório")
                                 @Positive(message = "O valor máximo deve ser maior que zero")
                                 Integer maximo,

                                 @NotNull(message = "A quantidade inicial é obrigatória")
                                 @Min(value = 0, message = "A quantidade inicial não pode ser negativa")
                                 Integer quantidade,

                                 @NotNull(message = "O tipo de cerveja é obrigatório")
                                 TipoCerveja tipo) {
}
