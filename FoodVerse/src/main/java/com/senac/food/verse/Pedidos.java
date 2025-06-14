package com.senac.food.verse;

import java.util.List;

public class Pedidos {
    private String idPedido;
    private String nomeCliente;
    private String horaPedido;
    private String horaEntrega;
    private String codigoLocalizador;
    private String enderecoCompleto;
    private String nomeEntregador;
    private String telefoneEntregador;
    private String modoEntrega;
    private String observacoes;
    private String statusPedido;       // <- ADICIONADO
    private String tipoPedido;         // <- ADICIONADO
    private List<ItemPedido> itens;

    public Pedidos(String idPedido, String statusPedido, String tipoPedido, String modoEntrega) {
        this.idPedido = idPedido;
        this.statusPedido = statusPedido;
        this.tipoPedido = tipoPedido;
        this.modoEntrega = modoEntrega;
    }

    public Pedidos(String idPedido, String nomeCliente, String horaPedido, String horaEntrega,
                   String codigoLocalizador, String enderecoCompleto, String nomeEntregador,
                   String telefoneEntregador, String modoEntrega, String observacoes,
                   List<ItemPedido> itens, String statusPedido, String tipoPedido) {
        this.idPedido = idPedido;
        this.nomeCliente = nomeCliente;
        this.horaPedido = horaPedido;
        this.horaEntrega = horaEntrega;
        this.codigoLocalizador = codigoLocalizador;
        this.enderecoCompleto = enderecoCompleto;
        this.nomeEntregador = nomeEntregador;
        this.telefoneEntregador = telefoneEntregador;
        this.modoEntrega = modoEntrega;
        this.observacoes = observacoes;
        this.itens = itens;
        this.statusPedido = statusPedido;
        this.tipoPedido = tipoPedido;
    }

    public String getIdPedido() {
        return idPedido;
    }

    public String getStatusPedido() {
        return statusPedido;
    }

    public String getTipoPedido() {
        return tipoPedido;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public String getHoraPedido() {
        return horaPedido;
    }

    public String getHoraEntrega() {
        return horaEntrega;
    }

    public String getCodigoLocalizador() {
        return codigoLocalizador;
    }

    public String getEnderecoCompleto() {
        return enderecoCompleto;
    }

    public String getNomeEntregador() {
        return nomeEntregador;
    }

    public String getTelefoneEntregador() {
        return telefoneEntregador;
    }

    public String getModoEntrega() {
        return modoEntrega;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public List<ItemPedido> getItens() {
        return itens;
    }
    
}
