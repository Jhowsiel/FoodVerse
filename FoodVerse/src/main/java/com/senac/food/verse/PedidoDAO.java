package com.senac.food.verse;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PedidoDAO {

    public ArrayList<Pedidos> buscarTodosPedidos() {
        ArrayList<Pedidos> listaPedidos = new ArrayList<>();
        ConexaoBanco conexao = new ConexaoBanco();

        try {
            conexao.abrirConexao();

            String sql = "SELECT ID_pedido, status_pedido, tipo_pedido, modo_consumo  FROM tb_pedidos";
            ResultSet rs = conexao.stmt.executeQuery(sql);

            while (rs.next()) {
                String idPedido = rs.getString("ID_pedido");
                String statusPedido = rs.getString("status_pedido");
                String tipoPedido = rs.getString("tipo_pedido");
                String modoConsumo = rs.getString("modo_consumo");

                Pedidos pedido = new Pedidos(idPedido, statusPedido, tipoPedido, modoConsumo);
                listaPedidos.add(pedido);
            }

            conexao.fecharConexao();

        } catch (SQLException ex) {
            System.out.println("Erro ao buscar pedidos: " + ex.getMessage());
        }

        return listaPedidos;
    }

    public int quantidadePedidos() {
        return buscarTodosPedidos().size();
    }
}
