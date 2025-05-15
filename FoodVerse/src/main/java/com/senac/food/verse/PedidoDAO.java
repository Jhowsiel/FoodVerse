package com.senac.food.verse;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class PedidoDAO {

    private final ArrayList<Pedidos> listaPedidos = new ArrayList<>();
    private String ultimoIdCarregado = null;

    public ArrayList<Pedidos> buscarTodosPedidos() {
        if (!listaPedidos.isEmpty()) {
            return listaPedidos; // retorna o cache se já estiver carregado
        }

        ConexaoBanco conexao = new ConexaoBanco();

        try {
            conexao.abrirConexao();

            String sql = "SELECT p.ID_pedido, s.status_nome, t.tipo_nome, p.modo_consumo "
                    + "FROM tb_pedidos p "
                    + "JOIN tb_status_pedido s ON p.status_id = s.status_id "
                    + "JOIN tb_tipo_pedido t ON p.tipo_id = t.tipo_id";
            ResultSet rs = conexao.stmt.executeQuery(sql);

            while (rs.next()) {
                String idPedido = rs.getString("ID_pedido");
                String statusPedido = rs.getString("status_nome");
                String tipoPedido = rs.getString("tipo_nome");
                String modoConsumo = rs.getString("modo_consumo");

                Pedidos pedido = new Pedidos(idPedido, statusPedido, tipoPedido, modoConsumo);
                listaPedidos.add(pedido);

                // atualiza o último ID carregado
                if (ultimoIdCarregado == null || idPedido.compareTo(ultimoIdCarregado) > 0) {
                    ultimoIdCarregado = idPedido;
                }
            }

            conexao.fecharConexao();

        } catch (SQLException ex) {
            System.out.println("Erro ao buscar pedidos: " + ex.getMessage());
        }

        return listaPedidos;
    }

    public void recarregarPedidos() {
        listaPedidos.clear();
        buscarTodosPedidos(); // recarrega e atualiza ultimoIdCarregado
    }

    public boolean haNovoPedido() {
        ConexaoBanco conexao = new ConexaoBanco();
        boolean temNovo = false;

        try {
            conexao.abrirConexao();

            String sql = "SELECT MAX(ID_pedido) AS ultimo_id FROM tb_pedidos";
            ResultSet rs = conexao.stmt.executeQuery(sql);

            if (rs.next()) {
                String novoUltimoId = rs.getString("ultimo_id");

                if (novoUltimoId != null && !novoUltimoId.equals(ultimoIdCarregado)) {
                    temNovo = true;
                    ultimoIdCarregado = novoUltimoId; // já atualiza o ID
                }
            }

            conexao.fecharConexao();

        } catch (SQLException ex) {
            System.out.println("Erro ao verificar novo pedido: " + ex.getMessage());
        }

        return temNovo;
    }

    public int quantidadePedidosPendentes() {
        if (listaPedidos.isEmpty()) {
            this.buscarTodosPedidos();
        }

        int quantidade = 0;

        for (Pedidos pedido : listaPedidos) {
            String statusPedido = pedido.getStatusPedido().trim();

            if ("pendente".equalsIgnoreCase(statusPedido)) {
                quantidade++;
            }
        }

        return quantidade;
    }

    public Pedidos buscarPedidoPorId(String pedidoId) {
        if (listaPedidos.isEmpty()) {
            this.buscarTodosPedidos();
        }

        try {
            for (Pedidos pedido : this.listaPedidos) {
                if (pedido.getIdPedido().equals(pedidoId)) {
                    return pedido;
                }
            }
        } catch (Exception ex) {
            System.out.println("Erro ao buscar pedidos pendentes: " + ex.getMessage());
        }
        return null;
    }

    public Pedidos filtrarPedidos(String nomeFiltro) {
        if (this.listaPedidos.isEmpty()) {
            this.buscarTodosPedidos();
        }

        try {
            for (Pedidos pedido : this.listaPedidos) {
                if (nomeFiltro.equalsIgnoreCase("Todos")
                        || pedido.getStatusPedido().equalsIgnoreCase(nomeFiltro)) {
                    return pedido;
                }
            }
        } catch (Exception ex) {
            System.out.println("Erro ao buscar status: " + ex.getMessage());
        }
        return null;
    }

}
