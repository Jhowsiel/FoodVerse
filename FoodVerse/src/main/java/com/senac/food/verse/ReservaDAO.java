package com.senac.food.verse;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO {


    public static class MesaConfig {
        private int id;
        private String nome;
        private int capacidade;
        private boolean ativa;

        public MesaConfig(int id, String nome, int capacidade, boolean ativa) {
            this.id = id;
            this.nome = nome;
            this.capacidade = capacidade;
            this.ativa = ativa;
        }

        public int getId() { return id; }
        public String getNome() { return nome; }
        public int getCapacidade() { return capacidade; }
        public boolean isAtiva() { return ativa; }
    }

    // Janela de 90 minutos para evitar sobreposição da mesma mesa durante um ciclo típico de refeição.
    private static final int RESERVA_CONFLITO_MINUTOS = 90;

    private boolean semContextoOperacional() {
        SessionContext ctx = SessionContext.getInstance();
        return ctx.isAdmin() && ctx.getRestauranteEfetivo() <= 0;
    }
    
    public List<String> getListaMesas() {
        List<String> mesas = new ArrayList<>();
        if (semContextoOperacional()) return mesas;
        
        int rid = SessionContext.getInstance().getRestauranteEfetivo();
        ConexaoBanco cb = new ConexaoBanco();
        Connection conn = cb.abrirConexao();
        if (conn == null) return mesas; // Se offline, não inventa mesas
        
        try {
            garantirTabelaMesas(conn);
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT nome FROM tb_mesas WHERE ID_restaurante = ? AND ativa = 1 ORDER BY nome")) {
                ps.setInt(1, rid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        mesas.add(rs.getString("nome"));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("Erro ao ler mesas: " + e.getMessage());
        } finally {
            cb.fecharConexao();
        }
        return mesas;
    }
    public List<MesaConfig> listarMesasConfig() {
        List<MesaConfig> lista = new ArrayList<>();
        if (semContextoOperacional()) {
            return lista;
        }
        int rid = SessionContext.getInstance().getRestauranteEfetivo();
        ConexaoBanco cb = new ConexaoBanco();
        Connection conn = cb.abrirConexao();
        if (conn == null) {
            for (int i = 1; i <= 20; i++) {
                lista.add(new MesaConfig(i, "Mesa " + String.format("%02d", i), 4, true));
            }
            return lista;
        }
        try {
            garantirTabelaMesas(conn);
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT ID_mesa, nome, capacidade, ativa FROM tb_mesas WHERE ID_restaurante = ? ORDER BY nome")) {
                ps.setInt(1, rid);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(new MesaConfig(
                                rs.getInt("ID_mesa"),
                                rs.getString("nome"),
                                rs.getInt("capacidade"),
                                rs.getBoolean("ativa")));
                    }
                }
            }
        } catch (SQLException ignored) {
        } finally {
            cb.fecharConexao();
        }
        return lista;
    }

    public boolean salvarMesa(String nome, int capacidade) {
        if (nome == null || nome.isBlank() || semContextoOperacional()) {
            return false;
        }
        int rid = SessionContext.getInstance().getRestauranteEfetivo();
        ConexaoBanco cb = new ConexaoBanco();
        Connection conn = cb.abrirConexao();
        if (conn == null) {
            return false;
        }
        try {
            garantirTabelaMesas(conn);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO tb_mesas (ID_restaurante, nome, capacidade, ativa) VALUES (?, ?, ?, 1)")) {
                ps.setInt(1, rid);
                ps.setString(2, nome.trim());
                ps.setInt(3, Math.max(1, capacidade));
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException ignored) {
            return false;
        } finally {
            cb.fecharConexao();
        }
    }

    public boolean atualizarMesa(int idMesa, String nome, int capacidade, boolean ativa) {
        if (idMesa <= 0 || nome == null || nome.isBlank() || semContextoOperacional()) {
            return false;
        }
        int rid = SessionContext.getInstance().getRestauranteEfetivo();
        ConexaoBanco cb = new ConexaoBanco();
        Connection conn = cb.abrirConexao();
        if (conn == null) {
            return false;
        }
        try {
            garantirTabelaMesas(conn);
            try (PreparedStatement ps = conn.prepareStatement(
                    "UPDATE tb_mesas SET nome = ?, capacidade = ?, ativa = ? WHERE ID_mesa = ? AND ID_restaurante = ?")) {
                ps.setString(1, nome.trim());
                ps.setInt(2, Math.max(1, capacidade));
                ps.setBoolean(3, ativa);
                ps.setInt(4, idMesa);
                ps.setInt(5, rid);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException ignored) {
            return false;
        } finally {
            cb.fecharConexao();
        }
    }

    public boolean excluirMesa(int idMesa) {
        if (idMesa <= 0 || semContextoOperacional()) {
            return false;
        }
        int rid = SessionContext.getInstance().getRestauranteEfetivo();
        ConexaoBanco cb = new ConexaoBanco();
        Connection conn = cb.abrirConexao();
        if (conn == null) {
            return false;
        }
        try {
            garantirTabelaMesas(conn);
            try (PreparedStatement ps = conn.prepareStatement(
                    "DELETE FROM tb_mesas WHERE ID_mesa = ? AND ID_restaurante = ?")) {
                ps.setInt(1, idMesa);
                ps.setInt(2, rid);
                return ps.executeUpdate() > 0;
            }
        } catch (SQLException ignored) {
            return false;
        } finally {
            cb.fecharConexao();
        }
    }

    private void garantirTabelaMesas(Connection conn) throws SQLException {
        String sql = "IF OBJECT_ID('dbo.tb_mesas', 'U') IS NULL " +
                "BEGIN " +
                "CREATE TABLE dbo.tb_mesas (" +
                "ID_mesa INT IDENTITY(1,1) PRIMARY KEY," +
                "ID_restaurante INT NOT NULL," +
                "nome NVARCHAR(50) NOT NULL," +
                "capacidade INT NOT NULL DEFAULT 4," +
                "ativa BIT NOT NULL DEFAULT 1" +
                "); " +
                "CREATE UNIQUE INDEX UX_tb_mesas_rest_nome ON dbo.tb_mesas(ID_restaurante, nome);" +
                "END";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.execute();
        }
    }

    // Busca reservas ativas para hoje
    public List<Reserva> listarReservasDoDia() {
        List<Reserva> lista = new ArrayList<>();
        if (semContextoOperacional()) {
            return lista;
        }
        int rid = SessionContext.getInstance().getRestauranteEfetivo();
        String sql = "SELECT r.ID_reserva, r.ID_cliente, c.username, c.nome, r.data_reserva, r.numero_pessoas, r.mesa " +
                     "FROM tb_reservas r " +
                     "LEFT JOIN tb_clientes c ON r.ID_cliente = c.id_cliente " +
                     "WHERE CAST(r.data_reserva AS DATE) = CAST(GETDATE() AS DATE)" +
                     (rid > 0 ? " AND r.ID_restaurante = ?" : "") +
                     " ORDER BY r.data_reserva ASC";

        ConexaoBanco cb = new ConexaoBanco();
        Connection conn = cb.abrirConexao();
        
        if (conn == null) {
            System.out.println(">> [ReservaDAO] Offline: Retornando lista de reservas vazia.");
            return lista; 
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (rid > 0) ps.setInt(1, rid);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Reserva r = new Reserva();
                    r.setIdReserva(rs.getInt("ID_reserva"));
                    r.setIdCliente(rs.getInt("ID_cliente"));
                    String nomeCliente = rs.getString("nome");
                    if (nomeCliente == null || nomeCliente.isBlank()) {
                        nomeCliente = rs.getString("username");
                    }
                    r.setNomeCliente(nomeCliente != null && !nomeCliente.isBlank() ? nomeCliente : "Cliente sem nome");
                    
                    Timestamp ts = rs.getTimestamp("data_reserva");
                    if (ts != null) r.setDataReserva(ts.toLocalDateTime());
                    
                    r.setNumPessoas(rs.getInt("numero_pessoas"));
                    r.setMesa(rs.getString("mesa"));
                    r.setStatus("RESERVADA");
                    lista.add(r);
                }
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar reservas: " + e.getMessage());
        } finally {
            cb.fecharConexao(); 
        }
        return lista;
    }

    public boolean criarReserva(Reserva r) {
        if (r == null || semContextoOperacional()) {
            return false;
        }
        String sql = "INSERT INTO tb_reservas (ID_cliente, ID_restaurante, data_reserva, numero_pessoas, mesa) VALUES (?, ?, ?, ?, ?)";
        ConexaoBanco cb = new ConexaoBanco();
        Connection conn = cb.abrirConexao();
        
        if (conn == null) {
            System.out.println(">> [ReservaDAO] Offline: Não é possível criar reserva no momento.");
            return false;
        }
        
        try {
            Integer restauranteId = r.getIdRestaurante();
            if (restauranteId == null || restauranteId <= 0) {
                restauranteId = SessionContext.getInstance().getRestauranteEfetivo();
            }
            if (restauranteId == null || restauranteId <= 0 || r.getDataReserva() == null
                    || r.getMesa() == null || r.getMesa().isBlank()) {
                return false;
            }
            if (existeConflitoReserva(conn, restauranteId, r.getMesa(), r.getDataReserva(), r.getIdReserva())) {
                return false;
            }

            try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, r.getIdCliente());
            ps.setInt(2, restauranteId);
            ps.setTimestamp(3, Timestamp.valueOf(r.getDataReserva()));
            ps.setInt(4, r.getNumPessoas());
            ps.setString(5, r.getMesa());
            
            return ps.executeUpdate() > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            cb.fecharConexao();
        }
    }

    public boolean cancelarReserva(int idReserva) {
        if (idReserva <= 0 || semContextoOperacional()) {
            return false;
        }
        int rid = SessionContext.getInstance().getRestauranteEfetivo();
        String sql = "DELETE FROM tb_reservas WHERE ID_reserva = ?" + (rid > 0 ? " AND ID_restaurante = ?" : "");
        ConexaoBanco cb = new ConexaoBanco();
        Connection conn = cb.abrirConexao();
        if (conn == null) {
            System.out.println(">> [ReservaDAO] Offline: Não é possível cancelar reserva no momento.");
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idReserva);
            if (rid > 0) {
                ps.setInt(2, rid);
            }
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            cb.fecharConexao();
        }
    }
    
    // Método auxiliar para buscar clientes para o combobox
    public List<String> listarClientesSimples() {
        List<String> clientes = new ArrayList<>();
        int rid = SessionContext.getInstance().getRestauranteEfetivo();

        String sql;
        if (rid > 0) {
            sql = "SELECT DISTINCT c.id_cliente AS id, c.username FROM tb_clientes c " +
                  "WHERE c.id_cliente IN (" +
                  "  SELECT r2.ID_cliente FROM tb_reservas r2 WHERE r2.ID_restaurante = ?" +
                  "  UNION" +
                  "  SELECT p.ID_cliente FROM tb_pedidos p WHERE p.ID_restaurante = ?" +
                  ") ORDER BY c.username";
        } else {
            sql = "SELECT id_cliente AS id, username FROM tb_clientes ORDER BY username";
        }

        ConexaoBanco cb = new ConexaoBanco();
        Connection conn = cb.abrirConexao();
        
        if (conn == null) {
            System.out.println(">> [ReservaDAO] Offline: Sem clientes para listar.");
            clientes.add("0 - Cliente Visitante (Modo Offline)");
            return clientes;
        }
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
             if (rid > 0) {
                 ps.setInt(1, rid);
                 ps.setInt(2, rid);
             }
             try (ResultSet rs = ps.executeQuery()) {
                 while(rs.next()) {
                     clientes.add(rs.getInt("id") + " - " + rs.getString("username"));
                 }
             }
        } catch(Exception e) { 
            e.printStackTrace(); 
        } finally {
            cb.fecharConexao();
        }
        return clientes;
    }


    public int obterOuCriarClienteLocal(String nomeCliente) {
        if (nomeCliente == null || nomeCliente.isBlank() || semContextoOperacional()) {
            return 0;
        }
        int rid = SessionContext.getInstance().getRestauranteEfetivo();
        if (rid <= 0) {
            return 0;
        }

        String nomeNormalizado = nomeCliente.trim();
        ConexaoBanco cb = new ConexaoBanco();
        Connection conn = cb.abrirConexao();
        if (conn == null) {
            return 0;
        }

        try {
            String sqlBusca =
                    "SELECT TOP 1 c.id_cliente " +
                    "FROM tb_clientes c " +
                    "WHERE (LOWER(c.nome) = LOWER(?) OR LOWER(c.username) = LOWER(?)) " +
                    "AND (c.id_cliente IN (SELECT r.ID_cliente FROM tb_reservas r WHERE r.ID_restaurante = ?) " +
                    "OR c.id_cliente IN (SELECT p.ID_cliente FROM tb_pedidos p WHERE p.ID_restaurante = ?)) " +
                    "ORDER BY c.id_cliente DESC";
            try (PreparedStatement ps = conn.prepareStatement(sqlBusca)) {
                ps.setString(1, nomeNormalizado);
                ps.setString(2, nomeNormalizado);
                ps.setInt(3, rid);
                ps.setInt(4, rid);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt("id_cliente");
                    }
                }
            }

            String usernameBase = nomeNormalizado
                    .toLowerCase()
                    .replaceAll("[^a-z0-9]+", ".")
                    .replaceAll("^\\.+|\\.+$", "");
            if (usernameBase.isBlank()) {
                usernameBase = "cliente";
            }
            String username = usernameBase + "." + System.currentTimeMillis();

            String sqlInsert =
                    "INSERT INTO tb_clientes (username, nome, data_cadastro) VALUES (?, ?, GETDATE())";
            try (PreparedStatement ps = conn.prepareStatement(sqlInsert, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, nomeNormalizado);
                ps.executeUpdate();
                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        return rs.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            cb.fecharConexao();
        }
        return 0;
    }

    private boolean existeConflitoReserva(Connection conn, int restauranteId, String mesa, LocalDateTime dataReserva, int idReservaAtual)
            throws SQLException {
        String sql = "SELECT COUNT(*) FROM tb_reservas " +
                "WHERE ID_restaurante = ? AND mesa = ? AND ABS(DATEDIFF(MINUTE, data_reserva, ?)) < ?" +
                (idReservaAtual > 0 ? " AND ID_reserva <> ?" : "");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, restauranteId);
            ps.setString(2, mesa);
            ps.setTimestamp(3, Timestamp.valueOf(dataReserva));
            ps.setInt(4, RESERVA_CONFLITO_MINUTOS);
            if (idReservaAtual > 0) {
                ps.setInt(5, idReservaAtual);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }
}
