package com.senac.food.verse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public void permissaoFunc() {
        // Implementação futura
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
   public Boolean verificarUsuario(String email, String senha) {
       System.out.println(senha);
    ConexaoBanco banco = new ConexaoBanco();
    boolean resultUsuario = false;

    try {
        banco.abrirConexao();
        String query = "SELECT * FROM tb_funcionarios WHERE email = ? AND password = ?";
        PreparedStatement stmt = banco.conn.prepareStatement(query);
        stmt.setString(1, email);
        stmt.setString(2, senha);
        banco.resultSet = stmt.executeQuery();

        resultUsuario = banco.resultSet.next(); // Se encontrou, retorna true

        stmt.close();
        banco.fecharConexao();

    } catch (Exception ex) {
        System.out.println("Erro ao consultar usuário: " + ex.getMessage());
    }
    return resultUsuario;
}

    public String getStatus() {
        return status;
    }
}
