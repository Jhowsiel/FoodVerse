package com.senac.food.verse;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    private final ArrayList<Pedidos> listaPedidos = new ArrayList<>();
    private ArrayList<Pedidos> cachePedidos = new ArrayList<>();
    private String ultimoIdCarregado = null;

    // --- MÉTODOS AUXILIARES PARA MODO OFFLINE (SIMULAÇÃO) ---
    private ArrayList<Pedidos> gerarPedidosSimulados() {
        ArrayList<Pedidos> mocks = new ArrayList<>();

        List<ItemPedido> itens1 = new ArrayList<>();
        itens1.add(new ItemPedido("1", "X-Burger Especial", 2, 25.50));
        itens1.add(new ItemPedido("2", "Coca-Cola Lata", 2, 6.00));

        List<ItemPedido> itens2 = new ArrayList<>();
        itens2.add(new ItemPedido("3", "Pizza Calabresa", 1, 45.00));

        mocks.add(new Pedidos("1001", "Cliente Teste 01", "12:00", "12:45", 
                "LOC-01", "Rua Exemplo, 123", "Entregador João", 
                "(11) 99999-9999", "Delivery", "Sem cebola", 
                itens1, "pendente", "Lanche", "Pix", 63.00, null));

        mocks.add(new Pedidos("1002", "Cliente Teste 02", "12:10", "13:00", 
                "LOC-02", "Av. Principal, 500", "Entregador Maria", 
                "(11) 98888-8888", "Salão", "", 
                itens2, "em preparo", "Jantar", "Cartão", 45.00, "Mesa 05"));

        return mocks;
    }

    public ArrayList<Pedidos> buscarTodosPedidosFresh() {
        ArrayList<Pedidos> pedidosFresh = new ArrayList<>();
        ConexaoBanco conexao = new ConexaoBanco();

        try {
            conexao.abrirConexao();

            if (conexao.conn == null) {
                return gerarPedidosSimulados();
            }

            String sql = "SELECT "
                    + "p.ID_pedido, "
                    + "c.username AS nome_cliente, "
                    + "c.telefone AS telefone_cliente, "
                    + "p.data_pedido AS hora_pedido, "
                    + "p.endereco_entrega, "
                    + "s.nome_status, "
                    + "pg.metodo_pagamento AS forma_pagamento, "
                    + "COALESCE(pg.valor, p.valor_total) AS subtotal "
                    + "FROM tb_pedidos p "
                    + "JOIN tb_clientes c ON p.ID_cliente = c.id_cliente "
                    + "JOIN tb_status_pedido s ON p.status_id = s.ID_status "
                    + "LEFT JOIN tb_pagamentos pg ON p.ID_pedido = pg.ID_pedido";

            ResultSet rs = conexao.stmt.executeQuery(sql);

            while (rs.next()) {
                String idPedido = rs.getString("ID_pedido");
                List<ItemPedido> itens = buscarItensDoPedido(idPedido);
                String enderecoEntrega = rs.getString("endereco_entrega");
                String modoEntrega = (enderecoEntrega != null && !enderecoEntrega.isEmpty()) ? "Delivery" : "Salão";

                Pedidos pedido = new Pedidos(idPedido, rs.getString("nome_cliente"), rs.getString("hora_pedido"), null,
                        null, enderecoEntrega, null,
                        rs.getString("telefone_cliente"), modoEntrega, null,
                        itens, rs.getString("nome_status"), null, rs.getString("forma_pagamento"), rs.getDouble("subtotal"), null);

                pedidosFresh.add(pedido);

                if (ultimoIdCarregado == null || idPedido.compareTo(ultimoIdCarregado) > 0) {
                    ultimoIdCarregado = idPedido;
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao buscar pedidos: " + ex.getMessage());
        } finally {
            conexao.fecharConexao();
        }

        return pedidosFresh;
    }

    public ArrayList<Pedidos> buscarTodosPedidos() {
        if (!listaPedidos.isEmpty()) {
            return listaPedidos;
        }

        ConexaoBanco conexao = new ConexaoBanco();

        try {
            conexao.abrirConexao();

            if (conexao.conn == null) {
                System.out.println(">> [PedidoDAO] Modo Offline: Carregando pedidos simulados.");
                listaPedidos.addAll(gerarPedidosSimulados());
                return listaPedidos;
            }

            String sql = "SELECT "
                    + "p.ID_pedido, "
                    + "c.username AS nome_cliente, "
                    + "c.telefone AS telefone_cliente, "
                    + "p.data_pedido AS hora_pedido, "
                    + "p.endereco_entrega, "
                    + "s.nome_status, "
                    + "pg.metodo_pagamento AS forma_pagamento, "
                    + "COALESCE(pg.valor, p.valor_total) AS subtotal "
                    + "FROM tb_pedidos p "
                    + "JOIN tb_clientes c ON p.ID_cliente = c.id_cliente "
                    + "JOIN tb_status_pedido s ON p.status_id = s.ID_status "
                    + "LEFT JOIN tb_pagamentos pg ON p.ID_pedido = pg.ID_pedido";

            ResultSet rs = conexao.stmt.executeQuery(sql);

            while (rs.next()) {
                String idPedido = rs.getString("ID_pedido");
                List<ItemPedido> itens = buscarItensDoPedido(idPedido);
                String enderecoEntrega = rs.getString("endereco_entrega");
                String modoEntrega = (enderecoEntrega != null && !enderecoEntrega.isEmpty()) ? "Delivery" : "Salão";

                Pedidos pedido = new Pedidos(idPedido, rs.getString("nome_cliente"), rs.getString("hora_pedido"), null,
                        null, enderecoEntrega, null,
                        rs.getString("telefone_cliente"), modoEntrega, null,
                        itens, rs.getString("nome_status"), null, rs.getString("forma_pagamento"), rs.getDouble("subtotal"), null);

                listaPedidos.add(pedido);

                if (ultimoIdCarregado == null || idPedido.compareTo(ultimoIdCarregado) > 0) {
                    ultimoIdCarregado = idPedido;
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao buscar pedidos: " + ex.getMessage());
        } finally {
            conexao.fecharConexao();
        }

        return listaPedidos;
    }

    public void recarregarPedidos() {
        listaPedidos.clear();
        listaPedidos.addAll(buscarTodosPedidosFresh());
        cachePedidos.clear();
        cachePedidos.addAll(listaPedidos);
    }

    public boolean houveAlteracoesPedidos() {
        ArrayList<Pedidos> pedidosAtuais = buscarTodosPedidosFresh();
        if (cachePedidos.size() != pedidosAtuais.size()) {
            return true;
        }
        for (int i = 0; i < pedidosAtuais.size(); i++) {
            Pedidos atual = pedidosAtuais.get(i);
            Pedidos cache = cachePedidos.get(i);
            if (!atual.getIdPedido().equals(cache.getIdPedido())
                    || !atual.getStatusPedido().equals(cache.getStatusPedido())) {
                return true;
            }
        }
        return false;
    }

    public boolean haNovoPedido() {
        ConexaoBanco conexao = new ConexaoBanco();
        boolean temNovo = false;

        try {
            conexao.abrirConexao();
            if (conexao.conn == null) return false;

            String sql = "SELECT MAX(ID_pedido) AS ultimo_id FROM tb_pedidos";
            ResultSet rs = conexao.stmt.executeQuery(sql);

            if (rs.next()) {
                String novoUltimoId = rs.getString("ultimo_id");
                if (novoUltimoId != null && !novoUltimoId.equals(ultimoIdCarregado)) {
                    temNovo = true;
                    ultimoIdCarregado = novoUltimoId;
                }
            }
        } catch (SQLException ex) {
            System.out.println("Erro ao verificar novo pedido: " + ex.getMessage());
        } finally {
            conexao.fecharConexao();
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
            
            if (conexao.conn == null) {
                for(Pedidos p : gerarPedidosSimulados()){
                    if(p.getIdPedido().equals(pedidoId)) return p;
                }
                return null;
            }

            String sql = "SELECT "
                    + "p.ID_pedido, "
                    + "c.username AS nome_cliente, "
                    + "c.telefone AS telefone_cliente, "
                    + "p.data_pedido AS hora_pedido, "
                    + "p.endereco_entrega, "
                    + "s.nome_status, "
                    + "pg.metodo_pagamento AS forma_pagamento, "
                    + "COALESCE(pg.valor, p.valor_total) AS subtotal "
                    + "FROM tb_pedidos p "
                    + "JOIN tb_clientes c ON p.ID_cliente = c.id_cliente "
                    + "JOIN tb_status_pedido s ON p.status_id = s.ID_status "
                    + "LEFT JOIN tb_pagamentos pg ON p.ID_pedido = pg.ID_pedido "
                    + "WHERE p.ID_pedido = ?";

            PreparedStatement stmt = conexao.conn.prepareStatement(sql);
            stmt.setString(1, pedidoId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String idPedido = rs.getString("ID_pedido");
                List<ItemPedido> itens = buscarItensDoPedido(idPedido);
                String enderecoEntrega = rs.getString("endereco_entrega");
                String modoEntrega = (enderecoEntrega != null && !enderecoEntrega.isEmpty()) ? "Delivery" : "Salão";

                pedido = new Pedidos(idPedido, rs.getString("nome_cliente"), rs.getString("hora_pedido"), null,
                        null, enderecoEntrega, null,
                        rs.getString("telefone_cliente"), modoEntrega, null,
                        itens, rs.getString("nome_status"), null, rs.getString("forma_pagamento"), rs.getDouble("subtotal"), null);
            }

            rs.close();
            stmt.close();

        } catch (SQLException ex) {
            System.out.println("Erro ao buscar pedido por ID: " + ex.getMessage());
        } finally {
            conexao.fecharConexao();
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

    private List<ItemPedido> buscarItensDoPedido(String idPedido) {
        List<ItemPedido> itens = new ArrayList<>();
        ConexaoBanco conexao = new ConexaoBanco();

        try {
            conexao.abrirConexao();
            if(conexao.conn == null) return itens;

            String sql = "SELECT pp.ID_produto, pr.nome_produto, pr.preco, pp.quantidade "
                       + "FROM tb_pedidos_produtos pp "
                       + "JOIN tb_produtos pr ON pp.ID_produto = pr.ID_produto "
                       + "WHERE pp.ID_pedido = ?";

            try (PreparedStatement pstmt = conexao.conn.prepareStatement(sql)) {
                pstmt.setString(1, idPedido);
                ResultSet rs = pstmt.executeQuery();

                while (rs.next()) {
                    String idProduto = rs.getString("ID_produto");
                    String nomeProduto = rs.getString("nome_produto");
                    int quantidade = rs.getInt("quantidade");
                    double precoUnitario = rs.getDouble("preco");
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

    public void atualizarStatusPedido(String idPedido, String novoStatus) {
        ConexaoBanco conexao = new ConexaoBanco();

        try {
            conexao.abrirConexao();
            
            if (conexao.conn == null) {
                System.out.println(">> [Simulação] Status do pedido " + idPedido + " atualizado para: " + novoStatus);
                for(Pedidos p : listaPedidos) {
                    if(p.getIdPedido().equals(idPedido)) {
                        p.setStatusPedido(novoStatus);
                    }
                }
                return;
            }

            String sqlBuscaStatus = "SELECT ID_status FROM tb_status_pedido WHERE nome_status = ?";
            PreparedStatement stmtBusca = conexao.conn.prepareStatement(sqlBuscaStatus);
            stmtBusca.setString(1, novoStatus);
            ResultSet rs = stmtBusca.executeQuery();

            if (rs.next()) {
                int statusId = rs.getInt("ID_status");

                String sqlUpdate = "UPDATE tb_pedidos SET status_id = ? WHERE ID_pedido = ?";
                PreparedStatement stmtUpdate = conexao.conn.prepareStatement(sqlUpdate);
                stmtUpdate.setInt(1, statusId);
                stmtUpdate.setString(2, idPedido);

                int rowsUpdated = stmtUpdate.executeUpdate();

                if (rowsUpdated > 0) {
                    System.out.println("Status do pedido " + idPedido + " atualizado para '" + novoStatus + "'.");
                }

                stmtUpdate.close();
            }

            rs.close();
            stmtBusca.close();

        } catch (SQLException ex) {
            System.out.println("Erro ao atualizar status do pedido: " + ex.getMessage());
        } finally {
            conexao.fecharConexao();
        }
    }
}