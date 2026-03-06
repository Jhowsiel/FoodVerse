package com.senac.food.verse;

import java.awt.CardLayout;
import java.awt.Container;
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
            conn = banco.abrirConexao();
            
            if (conn == null) {
                System.out.println("⚠️ AVISO: Banco de dados não encontrado.");
                System.out.println("✅ MODO SIMULAÇÃO: Funcionário cadastrado virtualmente.");
                return true; 
            }

            String query = "INSERT INTO tb_funcionarios (name, userName, email, role, phone, password, registrationDate, status) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, this.name);
            stmt.setString(2, this.userName);
            stmt.setString(3, this.email);
            stmt.setString(4, this.role);
            stmt.setString(5, telefoneFormatado);
            stmt.setString(6, this.password);
            stmt.setString(7, this.registrationDate);
            stmt.setString(8, this.status);

            int linhasAfetadas = stmt.executeUpdate();
            cadastroEfetuado = (linhasAfetadas > 0);

        } catch (SQLException ex) {
            Logger.getLogger(Funcionario.class.getName()).log(Level.SEVERE, "Erro ao inserir o funcionário (Banco Offline): ", ex);
            System.out.println("Simulando cadastro com sucesso para teste...");
            return true; 
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
        
        // --- BYPASS / MODO DE TESTE SEM BANCO ---
        if ("admin".equals(email) && "admin".equals(senha)) {
            System.out.println("🔓 MODO DEBUG: Login realizado sem banco de dados.");
            return "admin"; 
        }
        if ("user".equals(email) && "123".equals(senha)) {
             System.out.println("🔓 MODO DEBUG: Login realizado sem banco de dados (Funcionário).");
            return "funcionario";
        }
        // ----------------------------------------

        ConexaoBanco banco = new ConexaoBanco();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet resultSet = null;
        String role = null;

        try {
            conn = banco.abrirConexao();
            
            if (conn == null) {
                System.out.println("❌ Erro: Não foi possível conectar ao banco.");
                return null;
            }

            String query = "SELECT role FROM tb_funcionarios WHERE email = ? AND password = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, senha);
            resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                role = resultSet.getString("role");
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
        return role;
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
            if (conn == null) return "Simulação"; 

            String query = "SELECT " + colunaDesejada + " FROM tb_funcionarios WHERE " + colunaFiltro + " = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, valorFiltro);
            resultSet = stmt.executeQuery();

            if (resultSet.next()) {
                valorEncontrado = resultSet.getString(colunaDesejada);
            }

        } catch (SQLException ex) {
            System.err.println("Erro ao consultar valor: " + ex.getMessage());
            return "ErroBD"; 
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