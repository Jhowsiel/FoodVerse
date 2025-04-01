package com.senac.food.verse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Funcionario extends Usuario implements FuncionarioInterface {

    String name;
    String role;
    String acessCode;
    String phone;

    // Construtor
    public Funcionario(String name, String role, String acessCode, String phone, String userName, String email,
            String password, Boolean isLogin, String registrationDate) {
        super(userName, email, password, isLogin, registrationDate);
        this.name = name;
        this.role = role;
        this.acessCode = acessCode;
        this.phone = phone;
    }

    @Override
    public void permissaoFunc() {
        // Implementação futura
    }

    @Override
    public boolean cadastrarFuncionario() {
        String telefoneFormatado = Formatador.formatarTelefone(phone);
                
                
        boolean cadastroEfetuado = false;
        ConexaoBanco banco = new ConexaoBanco();
        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            // Abrir conexão
            conn = banco.abrirConexao();

            // Query parametrizada para evitar SQL Injection
            String query = "INSERT INTO tb_funcionarios (name, userName, email, role, phone, password, acessCode, registrationDate) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

            // Prepare the statement
            stmt = conn.prepareStatement(query);
            stmt.setString(1, name);
            stmt.setString(2, userName);
            stmt.setString(3, email);
            stmt.setString(4, role);
            stmt.setString(5, telefoneFormatado);
            stmt.setString(6, password);
            stmt.setString(7, acessCode);
            stmt.setString(8, registrationDate);

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
}
