package com.senac.food.verse;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexaoBanco {
    public Connection conn = null;
    public Statement stmt = null;
    public ResultSet resultSet = null;
    
    // Configurações do Banco (mantemos para quando quiseres conectar no futuro)
    final String SERVIDOR = "jdbc:sqlserver://127.0.0.1:1433;databaseName=FoodVerseDB;encrypt=false;trustServerCertificate=true";
    final String USUARIO = "sa";
    private final String SENHA = "pw_user_app";
    private final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    
    public Connection abrirConexao(){
        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(SERVIDOR, USUARIO, SENHA);
            stmt = conn.createStatement();
            
            System.out.println("Conexão com o banco aberta com sucesso!");
        }
        catch (ClassNotFoundException | SQLException ex){
            // ALTERAÇÃO: Em vez de mostrar o erro completo, mostramos um aviso simples.
            System.out.println(">> [Aviso] Banco de dados indisponível. A rodar em Modo Offline.");
            // O conn continua null, o que sinaliza para a classe Funcionario que deve usar a simulação.
            conn = null;
        }
        return conn;
    }
    
    public void fecharConexao(){
        try{
            // ALTERAÇÃO: Só tentamos fechar se a conexão realmente existir
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