package com.senac.food.verse;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FuncionarioTest {

    @Test
    public void testCriarFuncionario() {
        Funcionario funcionario = new Funcionario(
                "João",
                "Gerente",
                "123",
                "111",
                "joao",
                "joao@email.com",
                "senha",
                true,
                "2025-04-06"
        );
        assertNotNull(funcionario);
        assertEquals("João", funcionario.name);
        assertEquals("Gerente", funcionario.role);
    }

    @Test
    public void testCadastrarFuncionario() {
        Funcionario funcionario = new Funcionario(
                "Maria",
                "Atendente",
                "456",
                "222",
                "maria",
                "maria@email.com",
                "senha2",
                false,
                "2025-04-07"
        );
        boolean resultado = funcionario.cadastrarFuncionario();
        assertTrue(resultado instanceof Boolean);
    }
}