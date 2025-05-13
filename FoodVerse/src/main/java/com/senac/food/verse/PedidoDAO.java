package com.senac.food.verse;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PedidoDAO {

    ArrayList<Pedidos> listaPedidos = new ArrayList<>();

    public ArrayList<Pedidos> buscarTodosPedidos() {

        ConexaoBanco conexao = new ConexaoBanco();

        try {
            conexao.abrirConexao();

            // Modificando a consulta para usar JOIN com tb_status_pedido e tb_tipo_pedido
            String sql = "SELECT p.ID_pedido, s.status_nome, t.tipo_nome, p.modo_consumo "
                    + "FROM tb_pedidos p "
                    + "JOIN tb_status_pedido s ON p.status_id = s.status_id "
                    + "JOIN tb_tipo_pedido t ON p.tipo_id = t.tipo_id"; // JOIN com a tabela de tipo_pedido
            ResultSet rs = conexao.stmt.executeQuery(sql);

            while (rs.next()) {
                String idPedido = rs.getString("ID_pedido");
                String statusPedido = rs.getString("status_nome"); // Agora vem o nome do status
                String tipoPedido = rs.getString("tipo_nome"); // Agora vem o nome do tipo de pedido
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

    public int quantidadePedidosPendentes() {
        int quantidade = 0;

        for (Pedidos pedido : listaPedidos) {
            String statusPedido = pedido.getStatusPedido().trim();

            System.out.println(statusPedido);

            if ("pendente".equalsIgnoreCase(statusPedido)) {
                quantidade++;
            }
        }

        return quantidade;
    }

    public Pedidos buscarPedidoPorId(String pedidoId) {
        try {
            for (Pedidos pedido : listaPedidos) {
                if (pedido.getIdPedido().equals(pedidoId)) {
                    return pedido;
                }
            }
        } catch (Exception ex) {
            System.out.println("Erro ao buscar pedidos pendentes: " + ex.getMessage());
        }
        return null;
    }
}
