package br.com.aweb.sistema_vendas.model;

public enum StatusPedido {

    ATIVO("Ativo"),
    CANCELADO("Cancelado");

    private final String descricao;

    StatusPedido(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }

}
