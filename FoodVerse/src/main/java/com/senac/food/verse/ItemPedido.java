package com.senac.food.verse;

public class ItemPedido {
    private String idItem;
    private String nomeProduto;
    private int quantidade;
    private double preco;

    public ItemPedido(String idItem, String nomeProduto, int quantidade, double preco) {
        this.idItem = idItem;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.preco = preco;
    }

    public String getIdItem() {
        return idItem;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public double getPreco() {
        return preco;
    }

    public String getObservacao() {
        return "observaões do pedido";
    }
}
