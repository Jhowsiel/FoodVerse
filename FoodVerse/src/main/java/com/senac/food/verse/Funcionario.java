package com.senac.food.verse;

import java.awt.CardLayout;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Funcionario extends Usuario implements FuncionarioInterface {

    String name;
    String role;
    String phone;
    String status;
    int restauranteId;

    // Construtor
    public Funcionario(int restauranteId, String name, String role, String phone, String userName, String email,
            String password, Boolean isLogin, String registrationDate, String status) {
        super(userName, email, password, isLogin, registrationDate);
        this.restauranteId = restauranteId;
        this.name = name;
        this.role = role;
        this.phone = phone;
        this.status = status;
    }

    @Override
    public boolean cadastrarFuncionario() {
        String telefoneFormatado = Formatador.formatarTelefone(this.phone);

        boolean cadastroEfetuado = false;
        ConexaoBanco banco = new ConexaoBanco();
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = banco.abrirConexao();

            if (conn == null) {
                return false;
            }

            String query = "INSERT INTO tb_funcionarios (ID_restaurante, nome, username, email, cargo, telefone, senha, data_cadastro, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE(), ?)";

            stmt = conn.prepareStatement(query);
            stmt.setInt(1, this.restauranteId);
            stmt.setString(2, this.name);
            stmt.setString(3, this.userName);
            stmt.setString(4, this.email);
            stmt.setString(5, this.role);
            stmt.setString(6, telefoneFormatado);
            stmt.setString(7, this.password);
            stmt.setString(8, this.status);

            int linhasAfetadas = stmt.executeUpdate();
            cadastroEfetuado = (linhasAfetadas > 0);

        } catch (SQLException ex) {
            Logger.getLogger(Funcionario.class.getName()).log(Level.SEVERE, "Erro ao inserir o funcionário: ", ex);
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) banco.fecharConexao();
            } catch (SQLException e) {
                Logger.getLogger(Funcionario.class.getName()).log(Level.SEVERE, "Erro ao fechar conexão: ", e);
            }
        }
        return cadastroEfetuado;
    }

    // --- MÉTODOS ESTÁTICOS ---

    public static String loginFuncionario(String email, String senha) {
        ConexaoBanco banco = new ConexaoBanco();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        String cargo = null;

        try {
            conn = banco.abrirConexao();

            if (conn == null) {
                return null;
            }

            String query = "SELECT cargo FROM tb_funcionarios WHERE email = ? AND senha = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, senha);
            resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                cargo = resultSet.getString("cargo");
            }

        } catch (SQLException ex) {
            System.err.println("Erro ao consultar usuário: " + ex.getMessage());
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (stmt != null) stmt.close();
                if (conn != null) banco.fecharConexao();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }
        return cargo;
    }

    public static void permissaoFunc(String userRole, javax.swing.JFrame frame) {
        if (userRole == null) {
            System.out.println("Erro: Cargo do usuário é inválido (null). Login falhou.");
            return;
        }

        if (userRole.equalsIgnoreCase("admin")) {
            System.out.println("Exibindo Dashboard de Administrador...");
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "DashBoardAdmin");
        } else {
            System.out.println("Exibindo Dashboard de Funcionário...");
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "DashBoard");
        }
    }

    public static String buscarNoBanco(String colunaFiltro, String valorFiltro, String colunaDesejada) {
        ConexaoBanco banco = new ConexaoBanco();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        String valorEncontrado = null;

        try {
            conn = banco.abrirConexao();
            if (conn == null) return null;

            String query = "SELECT " + colunaDesejada + " FROM tb_funcionarios WHERE " + colunaFiltro + " = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, valorFiltro);
            resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                valorEncontrado = resultSet.getString(colunaDesejada);
            }

        } catch (SQLException ex) {
            System.err.println("Erro ao consultar valor: " + ex.getMessage());
        } finally {
            try {
                if (resultSet != null) resultSet.close();
                if (stmt != null) stmt.close();
                if (conn != null) banco.fecharConexao();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }

        return valorEncontrado;
    }

    public static String pegarDataAtual() {
        LocalDate dataAtual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return dataAtual.format(formatter);
    }
}