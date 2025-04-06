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
    
    final String SERVIDOR = "jdbc:sqlserver://127.0.0.1:1433;databaseName=FoodVerseDB;encrypt=false;trustServerCertificate=true";
    final String USUARIO = "sa";
    private final String SENHA = "pw_user_app";
    private final String DRIVER = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
    
    
    public Connection abrirConexao(){
        try {
            Class.forName(DRIVER);
            conn = DriverManager.getConnection(SERVIDOR, USUARIO, SENHA);
            stmt = conn.createStatement();
                
            System.out.println("Conexão aberta com sucesso!");
        }
        catch (ClassNotFoundException | SQLException ex){
            System.out.println("Erro ao acessar banco de dados, verifique! " + ex.getMessage());
        }
        return conn;
    }
    
    public void fecharConexao(){
        try{
            conn.close();
            System.out.println("Conexão finalizada com sucesso!");
        }
        catch(SQLException ex){
            System.out.println("Erro ao encerrar conexão: " + ex.getMessage());
        }
    }
}
