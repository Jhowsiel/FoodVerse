package com.senac.food.verse;

import java.sql.PreparedStatement;
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
                    + "p.data_pedido AS hora_pedido, "
                    + "p.hora_entrega, "
                    + "p.codigo_localizador, "
                    + "p.endereco_completo, "
                    + "p.nome_entregador, "
                    + "p.telefone_entregador, "
                    + "p.modo_consumo, "
                    + "p.observacoes, "
                    + "s.status_nome, "
                    + "t.tipo_nome, "
                    + "pg.metodo_pagamento AS forma_pagamento, "
                    + "pg.valor_total AS subtotal, "
                    + "r.mesa "
                    + "FROM tb_pedidos p "
                    + "JOIN tb_clientes c ON p.ID_cliente = c.UserId "
                    + "JOIN tb_status_pedido s ON p.status_id = s.status_id "
                    + "JOIN tb_tipo_pedido t ON p.tipo_id = t.tipo_id "
                    + "LEFT JOIN tb_reservas r ON p.ID_reserva = r.ID_reserva "
                    + "LEFT JOIN tb_pagamentos pg ON p.ID_pedido = pg.ID_pedido";

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
                String formaPagamento = rs.getString("forma_pagamento");
                double subtotal = rs.getDouble("subtotal");
                String mesa = rs.getString("mesa");

                List<ItemPedido> itens = buscarItensDoPedido(idPedido);

                Pedidos pedido = new Pedidos(idPedido, nomeCliente, horaPedido, horaEntrega,
                        codigoLocalizador, enderecoCompleto, nomeEntregador,
                        telefoneEntregador, modoConsumo, observacoes,
                        itens, statusPedido, tipoPedido, formaPagamento, subtotal, mesa);

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
        ConexaoBanco conexao = new ConexaoBanco();
        Pedidos pedido = null;

        try {
            conexao.abrirConexao();

            String sql = "SELECT "
                    + "p.ID_pedido, "
                    + "c.name AS nome_cliente, "
                    + "p.data_pedido AS hora_pedido, "
                    + "p.hora_entrega, "
                    + "p.codigo_localizador, "
                    + "p.endereco_completo, "
                    + "p.nome_entregador, "
                    + "p.telefone_entregador, "
                    + "p.modo_consumo, "
                    + "p.observacoes, "
                    + "s.status_nome, "
                    + "t.tipo_nome, "
                    + "pg.metodo_pagamento AS forma_pagamento, "
                    + "pg.valor_total AS subtotal, "
                    + "r.mesa "
                    + "FROM tb_pedidos p "
                    + "JOIN tb_clientes c ON p.ID_cliente = c.UserId "
                    + "JOIN tb_status_pedido s ON p.status_id = s.status_id "
                    + "JOIN tb_tipo_pedido t ON p.tipo_id = t.tipo_id "
                    + "LEFT JOIN tb_reservas r ON p.ID_reserva = r.ID_reserva "
                    + "LEFT JOIN tb_pagamentos pg ON p.ID_pedido = pg.ID_pedido "
                    + "WHERE p.ID_pedido = ?";

            PreparedStatement stmt = conexao.conn.prepareStatement(sql);
            stmt.setString(1, pedidoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
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
                String formaPagamento = rs.getString("forma_pagamento");
                double subtotal = rs.getDouble("subtotal");
                String mesa = rs.getString("mesa");

                List<ItemPedido> itens = buscarItensDoPedido(idPedido);

                pedido = new Pedidos(idPedido, nomeCliente, horaPedido, horaEntrega,
                        codigoLocalizador, enderecoCompleto, nomeEntregador,
                        telefoneEntregador, modoConsumo, observacoes,
                        itens, statusPedido, tipoPedido, formaPagamento, subtotal, mesa);
            }

            rs.close();
            stmt.close();
            conexao.fecharConexao();

        } catch (SQLException ex) {
            System.out.println("Erro ao buscar pedido por ID: " + ex.getMessage());
        }

        return pedido;
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

            String sql = """
            SELECT 
                pp.ID_produto,
                pr.nome_produto,
                pr.preco_produto,
                pp.quantidade
            FROM tb_pedidos_produtos pp
            JOIN tb_produtos pr ON pp.ID_produto = pr.ID_produto
            WHERE pp.ID_pedido = ?
        """;

            try (PreparedStatement pstmt = conexao.conn.prepareStatement(sql)) {
                pstmt.setString(1, idPedido);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String idProduto = rs.getString("ID_produto");
                    String nomeProduto = rs.getString("nome_produto");
                    int quantidade = rs.getInt("quantidade");
                    double precoUnitario = rs.getDouble("preco_produto");
                    double precoTotal = precoUnitario * quantidade;

                    ItemPedido item = new ItemPedido(idProduto, nomeProduto, quantidade, precoTotal);
                    itens.add(item);
                }
            }

        } catch (SQLException ex) {
            System.out.println("Erro ao buscar itens do pedido: " + ex.getMessage());
        } finally {
            conexao.fecharConexao();
        }

        return itens;
    }

}