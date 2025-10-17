package br.com.dio.estoque_cerveja.exception;

public class EstoqueExcedidoException extends RuntimeException{

    private final Long id;
    private final Integer quantidade;

    public EstoqueExcedidoException(Long id, Integer quantidade) {
        super(String.format("Não é possível incrementar %d unidades. Estoque máximo excedido para a cerveja com id: %d", quantidade, id));
        this.id = id;
        this.quantidade = quantidade;
    }

    public EstoqueExcedidoException(String mensagem) {
        super(mensagem);
        this.id = null;
        this.quantidade = null;
    }

    // Getters
    public Long getId() { return id; }
    public Integer getQuantidade() { return quantidade; }
}