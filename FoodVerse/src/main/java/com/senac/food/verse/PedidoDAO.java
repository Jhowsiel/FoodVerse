package com.senac.food.verse;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

            String sql = "SELECT "
                    + "p.ID_pedido, "
                    + "c.name AS nome_cliente, "
                    + // pegar nome do cliente na tabela tb_clientes
                    "p.data_pedido AS hora_pedido, "
                    + "p.hora_entrega, "
                    + "p.codigo_localizador, "
                    + "p.endereco_completo, "
                    + "p.nome_entregador, "
                    + "p.telefone_entregador, "
                    + "p.modo_consumo, "
                    + "p.observacoes, "
                    + "s.status_nome, "
                    + "t.tipo_nome "
                    + "FROM tb_pedidos p "
                    + "JOIN tb_clientes c ON p.ID_cliente = c.UserId "
                    + "JOIN tb_status_pedido s ON p.status_id = s.status_id "
                    + "JOIN tb_tipo_pedido t ON p.tipo_id = t.tipo_id";

            ResultSet rs = conexao.stmt.executeQuery(sql);

            while (rs.next()) {
                String idPedido = rs.getString("ID_pedido");
                String nomeCliente = rs.getString("nome_cliente");
                String horaPedido = rs.getString("hora_pedido");
                String horaEntrega = rs.getString("hora_entrega");
                String codigoLocalizador = rs.getString("codigo_localizador");
                String enderecoCompleto = rs.getString("endereco_completo");
                String nomeEntregador = rs.getString("nome_entregador");
                String telefoneEntregador = rs.getString("telefone_entregador");
                String modoConsumo = rs.getString("modo_consumo");
                String observacoes = rs.getString("observacoes");
                String statusPedido = rs.getString("status_nome");
                String tipoPedido = rs.getString("tipo_nome");

                List<ItemPedido> itens = buscarItensDoPedido(idPedido);

                Pedidos pedido = new Pedidos(idPedido, nomeCliente, horaPedido, horaEntrega,
                        codigoLocalizador, enderecoCompleto, nomeEntregador,
                        telefoneEntregador, modoConsumo, observacoes,
                        itens, statusPedido, tipoPedido);

                listaPedidos.add(pedido);

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
        buscarTodosPedidos();
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
                    ultimoIdCarregado = novoUltimoId;
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

    // Método para buscar os itens de um pedido pelo id do pedido
    private List<ItemPedido> buscarItensDoPedido(String idPedido) {
        List<ItemPedido> itens = new ArrayList<>();
        ConexaoBanco conexao = new ConexaoBanco();

        try {
            conexao.abrirConexao();

            String sql = "SELECT * FROM tb_itens_pedido WHERE pedido_id = ?";
            try (java.sql.PreparedStatement pstmt = conexao.conn.prepareStatement(sql)) {
                pstmt.setString(1, idPedido);

                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    // Ajuste os nomes das colunas conforme seu banco e a classe ItemPedido
                    String idItem = rs.getString("id_item");
                    String nomeProduto = rs.getString("nome_produto");
                    int quantidade = rs.getInt("quantidade");
                    double preco = rs.getDouble("preco");

                    ItemPedido item = new ItemPedido(idItem, nomeProduto, quantidade, preco);
                    itens.add(item);
                }
            }
            conexao.fecharConexao();

        } catch (SQLException ex) {
            System.out.println("Erro ao buscar itens do pedido: " + ex.getMessage());
        }

        return itens;
    }

}
