package com.senac.food.verse;

public class Pedidos {
    private String idPedido;
    private String statusPedido;
    private String tipoPedido;
    private String modoEntrega;

    public Pedidos(String idPedido, String statusPedido, String tipoPedido, String modoEntrega) {
        this.idPedido = idPedido;
        this.statusPedido = statusPedido;
        this.tipoPedido = tipoPedido;
        this.modoEntrega = modoEntrega;
    }

    
    
    public void definirEntrega() {
        if (this.modoEntrega.equals("delivery")) {
            
        } else {
            
        }
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
    
    public String getModoEntrega() {
        return modoEntrega;
    }
    
}
