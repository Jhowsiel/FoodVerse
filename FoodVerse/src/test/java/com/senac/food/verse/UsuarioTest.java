package com.senac.food.verse;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UsuarioTest {

    @Test
    public void testCriarUsuario() {
        Usuario usuario = new Usuario("teste", "teste@email.com", "12345", true, "2025-04-06");
        assertNotNull(usuario);
        assertEquals("teste", usuario.userName);
        assertEquals("teste@email.com", usuario.email);
    }

    @Test
    public void testUsuarioLogado() {
        Usuario usuario = new Usuario("logado", "logado@email.com", "senha", true, "2025-04-08");
        assertTrue(usuario.chechLogin());
    }

    @Test
    public void testUsuarioNaoLogado() {
        Usuario usuario = new Usuario("naologado", "naologado@email.com", "senha", false, "2025-04-09");
        assertFalse(usuario.chechLogin());
    }

    @Test
    public void testPegarNomeUsuario() {
        Usuario usuario = new Usuario("nome", "email@email.com", "pass", true, "2025-04-10");
        assertEquals("nome", usuario.getUserName());
    }
}