package br.com.dio.estoque_cerveja.exception;

public class CervejaNaoEncontradaException extends RuntimeException{
    public CervejaNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
