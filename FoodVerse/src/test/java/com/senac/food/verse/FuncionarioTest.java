package com.senac.food.verse;

import org.junit.jupiter.api.Test;
import java.sql.Connection;
import static org.junit.jupiter.api.Assertions.*;

public class FuncionarioTest {

    @Test
    public void testCriarFuncionario() {
        Funcionario funcionario = new Funcionario(
                1,
                "João",
                "Gerente",
                "(11) 9 1234-5678",
                "joao",
                "joao@email.com",
                "Senha@123",
                true,
                "2025-04-06",
                "ativo"
        );
        assertNotNull(funcionario);
        assertEquals("João", funcionario.name);
        assertEquals("Gerente", funcionario.role);
        assertEquals(1, funcionario.restauranteId);
    }

    @Test
    public void testCadastrarFuncionario() {
        Funcionario funcionario = new Funcionario(
                2,
                "Maria",
                "Atendente",
                "(11) 9 8765-4321",
                "maria",
                "maria@email.com",
                "Senha@456",
                false,
                "2025-04-07",
                "pendente"
        );
        boolean resultado = funcionario.cadastrarFuncionario();
        ConexaoBanco conexao = new ConexaoBanco();
        Connection conn = conexao.abrirConexao();
        try {
            assertEquals(conn != null, resultado);
        } finally {
            conexao.fecharConexao();
        }
    }
}
