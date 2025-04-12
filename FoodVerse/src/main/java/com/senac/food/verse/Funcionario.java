package com.senac.food.verse;

import java.awt.CardLayout;
import java.awt.Container;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Funcionario extends Usuario implements FuncionarioInterface {

    private static Container getContentPane() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    String name;
    String role;
    String phone;
    String status;

    // Construtor
    public Funcionario(String name, String role, String phone, String userName, String email,
            String password, Boolean isLogin, String registrationDate, String status) {
        super(userName, email, password, isLogin, registrationDate);
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
            // Abrir conexão
            conn = banco.abrirConexao();

            // Query parametrizada para evitar SQL Injection
            String query = "INSERT INTO tb_funcionarios (name, userName, email, role, phone, password, registrationDate, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            // Prepare the statement
            stmt = conn.prepareStatement(query);
            stmt.setString(1, this.name);
            stmt.setString(2, this.userName);
            stmt.setString(3, this.email);
            stmt.setString(4, this.role);
            stmt.setString(5, telefoneFormatado);
            stmt.setString(6, this.password);
            stmt.setString(7, this.registrationDate);
            stmt.setString(8, this.status);

            // Executar e verificar sucesso
            int linhasAfetadas = stmt.executeUpdate();
            cadastroEfetuado = (linhasAfetadas > 0);

        } catch (SQLException ex) {
            Logger.getLogger(Funcionario.class.getName()).log(Level.SEVERE, "Erro ao inserir o funcionário: ", ex);
        } finally {
            // Fechar recursos com segurança
            try {
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    banco.fecharConexao();
                }
            } catch (SQLException e) {
                Logger.getLogger(Funcionario.class.getName()).log(Level.SEVERE, "Erro ao fechar conexão: ", e);
            }
        }
        return cadastroEfetuado;
    }

    //MÉTODOS
    public static String loginFuncionario(String email, String senha) {
        ConexaoBanco banco = new ConexaoBanco();
        Connection conn = null;
        PreparedStatement stmt = null;
        java.sql.ResultSet resultSet = null;
        String role = null; // Inicializa a role como null

        try {
            conn = banco.abrirConexao();
            String query = "SELECT role FROM tb_funcionarios WHERE email = ? AND password = ?"; // Seleciona a role
            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, senha);
            resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                role = resultSet.getString("role"); // Obtém a role do resultado.
            }

        } catch (SQLException ex) {
            System.err.println("Erro ao consultar usuário: " + ex.getMessage());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    banco.fecharConexao();
                }
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }
        return role; // Retorna a role (ou null se o login falhou)
    }

    public static void permissaoFunc(String userRole, javax.swing.JFrame frame) {
        if (userRole.equals("admin")) {
            System.out.println("Exibindo Dashboard de Administrador...");
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "DashBoardAdmin");
        } else {
            System.out.println("Exibindo Dashboard de Funcionário...");
            CardLayout layout = (CardLayout) frame.getContentPane().getLayout();
            layout.show(frame.getContentPane(), "DashBoard");
        }
    }

    public static String buscarUsernameNoBanco(String coluna, String valor) {
        ConexaoBanco banco = new ConexaoBanco();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        String username = null;

        try {
            conn = banco.abrirConexao();
            String query = "SELECT username FROM tb_funcionarios WHERE " + coluna + " = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, valor);
            resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                username = resultSet.getString("username");
            }

        } catch (SQLException ex) {
            System.err.println("Erro ao consultar usuário: " + ex.getMessage());
        } finally {
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    banco.fecharConexao();
                }
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }

        return username;
    }

    public String getStatus() {
        return status;
    }
}
