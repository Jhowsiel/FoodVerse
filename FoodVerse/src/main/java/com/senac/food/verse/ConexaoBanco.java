package com.senac.food.verse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexaoBanco {
    // Variáveis mantidas públicas para não quebrar o ValidarCadastro.java
    public Connection conn = null;
    public Statement stmt = null;
    public ResultSet resultSet = null;
    
    // Configurações do Banco
    final String SERVIDOR = "jdbc:sqlserver://127.0.0.1:1433;databaseName=FoodVerseDB;encrypt=false;trustServerCertificate=true";
    final String USUARIO = "sa";
    private final String SENHA = "SenhaForte#2026";
    private final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    
    public Connection abrirConexao(){
        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(SERVIDOR, USUARIO, SENHA);
            stmt = conn.createStatement();
            
            System.out.println("Conexão com o banco aberta com sucesso!");
        }
        catch (ClassNotFoundException | SQLException ex){
            System.out.println(">> [Aviso] Banco de dados indisponível. A rodar em Modo Offline.");
            conn = null;
        }
        return conn;
    }
    
    public void fecharConexao(){
        try{
            if (resultSet != null && !resultSet.isClosed()) {
                resultSet.close();
            }
            if (stmt != null && !stmt.isClosed()) {
                stmt.close();
            }
            if (conn != null && !conn.isClosed()) {
                conn.close();
                System.out.println("Conexão finalizada com sucesso!");
            }
        }
        catch(SQLException ex){
            System.out.println("Erro ao encerrar conexão: " + ex.getMessage());
        }
    }
}