package com.senac.food.verse;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReservaDAO {
    
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
        String sql = "SELECT r.ID_reserva, r.ID_cliente, c.name, r.data_reserva, r.num_pessoas, r.mesa " +
                     "FROM tb_reservas r " +
                     "LEFT JOIN tb_clientes c ON r.ID_cliente = c.UserId " +
                     "WHERE CAST(r.data_reserva AS DATE) = CAST(GETDATE() AS DATE) " +
                     "ORDER BY r.data_reserva ASC";

        ConexaoBanco cb = new ConexaoBanco();
        try {
            if (cb.conn == null || cb.conn.isClosed()) {
                // Fallback de segurança se a conexão não abrir no construtor
                cb.conn = DriverManager.getConnection("jdbc:sqlserver://127.0.0.1:1433;databaseName=FoodVerseDB;encrypt=false;trustServerCertificate=true;loginTimeout=10", "sa", "123456");
            }
            
            PreparedStatement ps = cb.conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Reserva r = new Reserva();
                r.setIdReserva(rs.getInt("ID_reserva"));
                r.setIdCliente(rs.getInt("ID_cliente"));
                r.setNomeCliente(rs.getString("name"));
                
                Timestamp ts = rs.getTimestamp("data_reserva");
                if (ts != null) r.setDataReserva(ts.toLocalDateTime());
                
                r.setNumPessoas(rs.getInt("num_pessoas"));
                r.setMesa(rs.getString("mesa"));
                r.setStatus("RESERVADA");
                lista.add(r);
            }
        } catch (SQLException e) {
            System.err.println("Erro ao listar reservas: " + e.getMessage());
        }
        return lista;
    }

    public boolean criarReserva(Reserva r) {
        String sql = "INSERT INTO tb_reservas (ID_cliente, data_reserva, num_pessoas, mesa) VALUES (?, ?, ?, ?)";
        ConexaoBanco cb = new ConexaoBanco();
        try {
            if (cb.conn == null) cb.conn = DriverManager.getConnection("jdbc:sqlserver://127.0.0.1:1433;databaseName=FoodVerseDB;encrypt=false", "sa", "123456"); // Ajuste suas credenciais se necessário
            
            PreparedStatement ps = cb.conn.prepareStatement(sql);
            ps.setInt(1, r.getIdCliente());
            ps.setTimestamp(2, Timestamp.valueOf(r.getDataReserva()));
            ps.setInt(3, r.getNumPessoas());
            ps.setString(4, r.getMesa());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    // Método auxiliar para buscar clientes para o combobox
    public List<String> listarClientesSimples() {
        List<String> clientes = new ArrayList<>();
        String sql = "SELECT UserId, name FROM tb_clientes ORDER BY name";
        ConexaoBanco cb = new ConexaoBanco();
        try {
             if (cb.conn == null) cb.conn = DriverManager.getConnection("jdbc:sqlserver://127.0.0.1:1433;databaseName=FoodVerseDB;encrypt=false", "sa", "123456");
             Statement st = cb.conn.createStatement();
             ResultSet rs = st.executeQuery(sql);
             while(rs.next()) {
                 clientes.add(rs.getInt("UserId") + " - " + rs.getString("name"));
             }
        } catch(Exception e) { e.printStackTrace(); }
        return clientes;
    }
}