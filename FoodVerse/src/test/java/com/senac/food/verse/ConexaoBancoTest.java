/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package com.senac.food.verse;

import java.sql.Connection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author josie
 */
public class ConexaoBancoTest {
    
    public ConexaoBancoTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of abrirConexao method, of class ConexaoBanco.
     */
    @Test
    public void testAbrirConexao() {
        System.out.println("abrirConexao");
        ConexaoBanco conexao = new ConexaoBanco();
        assertDoesNotThrow(() -> {
            Connection conn = conexao.abrirConexao();
            if (conn != null) {
                assertFalse(conn.isClosed());
            }
        });
    }

    /**
     * Test of fecharConexao method, of class ConexaoBanco.
     */
    @Test
    public void testFecharConexao() {
        System.out.println("fecharConexao");
        ConexaoBanco conexao = new ConexaoBanco();
        assertDoesNotThrow(() -> {
            Connection conn = conexao.abrirConexao();
            conexao.fecharConexao();
            if (conn != null) {
                assertTrue(conn.isClosed());
            }
        });
    }
    
 
}
