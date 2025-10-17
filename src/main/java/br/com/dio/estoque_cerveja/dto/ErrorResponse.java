package br.com.dio.estoque_cerveja.dto;

import java.time.Instant;

public record ErrorResponse(
        int status,
        String erro,
        String mensagem,
        String caminho,
        Instant timestamp
) {}
