package br.com.dio.estoque_cerveja.exception;

public class CervejaJaExisteException extends RuntimeException{
    public CervejaJaExisteException(String mensagem) {
        super(mensagem);
    }
}
