package com.senac.food.verse;

public class Pedidos {
    private String idPedido;
    private String statusPedido;
    private String tipoEntrega;

    public Pedidos(String idPedido, String statusPedido, String tipoEntrega) {
        this.idPedido = idPedido;
        this.statusPedido = statusPedido;
        this.tipoEntrega = tipoEntrega;
    }
    
    public void defiinirEntrega() {
        if (this.tipoEntrega.equals("delivery")) {
            
        } else {
            
        }
    }
}
