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
            stmt.setString(7, PasswordUtils.hash(this.password));
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

            String query = "SELECT cargo, senha FROM tb_funcionarios WHERE email = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            resultSet = stmt.executeQuery();

            if (resultSet.next() && PasswordUtils.matches(senha, resultSet.getString("senha"))) {
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

    /**
     * Realiza o login e preenche o SessionContext com os dados completos do funcionário.
     *
     * @return mensagem de erro em caso de falha, ou null em caso de sucesso.
     */
    public static String loginComContexto(String emailOuUsername, String senha) {
        ConexaoBanco banco = new ConexaoBanco();
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = banco.abrirConexao();
            if (conn == null) {
                return "Erro de conexão com o banco.";
            }

            String query = "SELECT f.ID_funcionario, f.nome, f.cargo, f.status, "
                    + "ISNULL(f.ID_restaurante, 0) AS ID_restaurante, "
                    + "ISNULL(r.ativo, 1) AS restaurante_ativo, "
                    + "f.senha "
                    + "FROM tb_funcionarios f "
                    + "LEFT JOIN tb_restaurantes r ON r.ID_restaurante = f.ID_restaurante "
                    + "WHERE (f.email = ? OR f.username = ?)";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, emailOuUsername);
            stmt.setString(2, emailOuUsername);
            rs = stmt.executeQuery();

            if (!rs.next() || !PasswordUtils.matches(senha, rs.getString("senha"))) {
                return "Credenciais inválidas.";
            }

            String status = rs.getString("status");
            if ("bloqueado".equalsIgnoreCase(status)) {
                return "Usuário bloqueado. Contate o administrador.";
            }
            if ("pendente".equalsIgnoreCase(status)) {
                return "Seu cadastro ainda está em análise.";
            }
            if (!"ativo".equalsIgnoreCase(status)) {
                return "Usuário inativo. Contate o administrador.";
            }

            String cargo     = rs.getString("cargo");
            boolean isAdmin  = "Admin".equalsIgnoreCase(cargo);
            int restauranteId = rs.getInt("ID_restaurante");
            boolean restauranteAtivo = rs.getBoolean("restaurante_ativo");

            if (!isAdmin && restauranteId == 0) {
                return "Funcionário sem restaurante vinculado. Contate o administrador.";
            }
            if (!isAdmin && !restauranteAtivo) {
                return "Restaurante inativo na plataforma. Operação não permitida.";
            }

            int id       = rs.getInt("ID_funcionario");
            String nome  = rs.getString("nome");

            SessionContext.getInstance().inicializar(id, nome, cargo, status, restauranteId);
            return null; // sucesso

        } catch (SQLException ex) {
            System.err.println("Erro ao fazer login: " + ex.getMessage());
            return "Erro interno: " + ex.getMessage();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) banco.fecharConexao();
            } catch (SQLException e) {
                System.err.println("Erro ao fechar recursos: " + e.getMessage());
            }
        }
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