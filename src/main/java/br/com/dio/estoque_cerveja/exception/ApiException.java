package br.com.dio.estoque_cerveja.exception;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ApiException {

    private final LocalDateTime timestamp;
    private final int status;
    private final String erro;
    private final String mensagem;
    private final String caminho;

    public ApiException(int status, String erro, String mensagem, String caminho) {
        this.timestamp = LocalDateTime.now();
        this.status = status;
        this.erro = erro;
        this.mensagem = mensagem;
        this.caminho = caminho;
    }
}
