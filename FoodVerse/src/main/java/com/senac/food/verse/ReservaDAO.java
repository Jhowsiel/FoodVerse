package com.senac.food.verse;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO {

    // Janela de 90 minutos para evitar sobreposição da mesma mesa durante um ciclo típico de refeição.
    private static final int RESERVA_CONFLITO_MINUTOS = 90;

    private boolean semContextoOperacional() {
        SessionContext ctx = SessionContext.getInstance();
        return ctx.isAdmin() && ctx.getRestauranteEfetivo() <= 0;
    }
    
    // Lista fixa de mesas para o mapa do restaurante
    public List<String> getListaMesas() {
        List<String> mesas = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            mesas.add("Mesa " + String.format("%02d", i));
        }
        return mesas;
    }

    // Busca reservas ativas para hoje
    public List<Reserva> listarReservasDoDia() {
        List<Reserva> lista = new ArrayList<>();
        if (semContextoOperacional()) {
            return lista;
        }
        int rid = SessionContext.getInstance().getRestauranteEfetivo();
        String sql = "SELECT r.ID_reserva, r.ID_cliente, c.username, r.data_reserva, r.numero_pessoas, r.mesa " +
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
                    String nomeCliente = rs.getString("username");
                    r.setNomeCliente(nomeCliente != null ? nomeCliente : "Cliente #" + rs.getInt("ID_cliente"));
                    
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
        String sql = "SELECT id_cliente AS id, username FROM tb_clientes ORDER BY username";
        ConexaoBanco cb = new ConexaoBanco();
        Connection conn = cb.abrirConexao();
        
        if (conn == null) {
            System.out.println(">> [ReservaDAO] Offline: Sem clientes para listar.");
            clientes.add("0 - Cliente Visitante (Modo Offline)");
            return clientes;
        }
        
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
             
             while(rs.next()) {
                 clientes.add(rs.getInt("id") + " - " + rs.getString("username"));
             }
        } catch(Exception e) { 
            e.printStackTrace(); 
        } finally {
            cb.fecharConexao();
        }
        return clientes;
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
