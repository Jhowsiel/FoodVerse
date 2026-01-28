package com.senac.food.verse;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PedidoDAO {

    // Cache principal dos pedidos já carregados para exibir na interface
    private final ArrayList<Pedidos> listaPedidos = new ArrayList<>();
    // Cache anterior para comparação e detecção de alteração de pedidos
    private ArrayList<Pedidos> cachePedidos = new ArrayList<>();
    private String ultimoIdCarregado = null;

    // --- MÉTODOS AUXILIARES PARA MODO OFFLINE (SIMULAÇÃO) ---
    private ArrayList<Pedidos> gerarPedidosSimulados() {
        ArrayList<Pedidos> mocks = new ArrayList<>();

        // Cria itens simulados
        List<ItemPedido> itens1 = new ArrayList<>();
        itens1.add(new ItemPedido("1", "X-Burger Especial", 2, 25.50));
        itens1.add(new ItemPedido("2", "Coca-Cola Lata", 2, 6.00));

        List<ItemPedido> itens2 = new ArrayList<>();
        itens2.add(new ItemPedido("3", "Pizza Calabresa", 1, 45.00));

        // Cria pedidos simulados
        // Pedido 1
        mocks.add(new Pedidos("1001", "Cliente Teste 01", "12:00", "12:45", 
                "LOC-01", "Rua Exemplo, 123", "Entregador João", 
                "(11) 99999-9999", "Delivery", "Sem cebola", 
                itens1, "pendente", "Lanche", "Pix", 63.00, null));

        // Pedido 2
        mocks.add(new Pedidos("1002", "Cliente Teste 02", "12:10", "13:00", 
                "LOC-02", "Av. Principal, 500", "Entregador Maria", 
                "(11) 98888-8888", "Salão", "", 
                itens2, "em preparo", "Jantar", "Cartão", 45.00, "Mesa 05"));

        return mocks;
    }
    // --------------------------------------------------------

    // Busca pedidos do banco para comparação (sempre consulta o banco!)
    public ArrayList<Pedidos> buscarTodosPedidosFresh() {
        ArrayList<Pedidos> pedidosFresh = new ArrayList<>();
        ConexaoBanco conexao = new ConexaoBanco();

        try {
            conexao.abrirConexao();

            // MODO OFFLINE: Se não conectar, retorna simulados
            if (conexao.conn == null) {
                return gerarPedidosSimulados();
            }

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

                pedidosFresh.add(pedido);

                if (ultimoIdCarregado == null || idPedido.compareTo(ultimoIdCarregado) > 0) {
                    ultimoIdCarregado = idPedido;
                }
            }
            conexao.fecharConexao();

        } catch (SQLException ex) {
            System.out.println("Erro ao buscar pedidos: " + ex.getMessage());
        }

        return pedidosFresh;
    }

    // Consulta e cacheia todos pedidos do banco (para interface)
    public ArrayList<Pedidos> buscarTodosPedidos() {
        if (!listaPedidos.isEmpty()) {
            return listaPedidos; // retorna o cache se já estiver carregado
        }

        ConexaoBanco conexao = new ConexaoBanco();

        try {
            conexao.abrirConexao();

            // MODO OFFLINE
            if (conexao.conn == null) {
                System.out.println(">> [PedidoDAO] Modo Offline: Carregando pedidos simulados.");
                listaPedidos.addAll(gerarPedidosSimulados());
                return listaPedidos;
            }

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

    // Limpa o cache de pedidos e recarrega do banco
    public void recarregarPedidos() {
        listaPedidos.clear();
        listaPedidos.addAll(buscarTodosPedidosFresh());
        cachePedidos.clear();
        cachePedidos.addAll(listaPedidos);
    }

    // Detecta se houve mudanças
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

    // Detecta novo pedido pelo último ID
    public boolean haNovoPedido() {
        ConexaoBanco conexao = new ConexaoBanco();
        boolean temNovo = false;

        try {
            conexao.abrirConexao();
            
            // MODO OFFLINE: Sem novidades
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
            
            // MODO OFFLINE
            if (conexao.conn == null) {
                // Tenta achar na lista simulada
                for(Pedidos p : gerarPedidosSimulados()){
                    if(p.getIdPedido().equals(pedidoId)) return p;
                }
                return null;
            }

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

    // Busca os itens de um pedido pelo id do pedido
    private List<ItemPedido> buscarItensDoPedido(String idPedido) {
        List<ItemPedido> itens = new ArrayList<>();
        ConexaoBanco conexao = new ConexaoBanco();

        try {
            conexao.abrirConexao();
            // MODO OFFLINE: Retorna vazio se não houver conexão (os itens já vêm no mock do pedido)
            if(conexao.conn == null) return itens;

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

    public void atualizarStatusPedido(String idPedido, String novoStatus) {
        ConexaoBanco conexao = new ConexaoBanco();

        try {
            conexao.abrirConexao();
            
            // MODO OFFLINE
            if (conexao.conn == null) {
                System.out.println(">> [Simulação] Status do pedido " + idPedido + " atualizado para: " + novoStatus);
                // Atualiza na memória RAM para refletir na tela imediatamente
                for(Pedidos p : listaPedidos) {
                    if(p.getIdPedido().equals(idPedido)) {
                        p.setStatusPedido(novoStatus);
                    }
                }
                return;
            }

            // Busca o status_id correspondente ao nome do status
            String sqlBuscaStatus = "SELECT status_id FROM tb_status_pedido WHERE status_nome = ?";
            PreparedStatement stmtBusca = conexao.conn.prepareStatement(sqlBuscaStatus);
            stmtBusca.setString(1, novoStatus);
            ResultSet rs = stmtBusca.executeQuery();

            if (rs.next()) {
                int statusId = rs.getInt("status_id");

                // Atualiza o status_id do pedido
                String sqlUpdate = "UPDATE tb_pedidos SET status_id = ? WHERE ID_pedido = ?";
                PreparedStatement stmtUpdate = conexao.conn.prepareStatement(sqlUpdate);
                stmtUpdate.setInt(1, statusId);
                stmtUpdate.setString(2, idPedido);

                int rowsUpdated = stmtUpdate.executeUpdate();

                if (rowsUpdated > 0) {
                    System.out.println("Status do pedido " + idPedido + " atualizado para '" + novoStatus + "'.");
                } else {
                    System.out.println("Pedido com ID " + idPedido + " não encontrado.");
                }

                stmtUpdate.close();
            } else {
                System.out.println("Status '" + novoStatus + "' não encontrado.");
            }

            rs.close();
            stmtBusca.close();
            conexao.fecharConexao();

        } catch (SQLException ex) {
            System.out.println("Erro ao atualizar status do pedido: " + ex.getMessage());
        }
    }
}