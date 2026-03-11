package com.senac.food.verse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SessionContextTest {

    @BeforeEach
    public void setUp() {
        SessionContext.getInstance().limpar();
    }

    @Test
    public void testAdmin_naoRequereRestaurante() {
        SessionContext ctx = SessionContext.getInstance();
        ctx.inicializar(1, "Admin User", "Admin", "ativo", 0);
        assertTrue(ctx.isAdmin());
        assertEquals(0, ctx.getRestauranteId());
        assertEquals(0, ctx.getRestauranteEfetivo()); // sem restaurante selecionado
    }

    @Test
    public void testFuncionario_usaRestauranteDoLogin() {
        SessionContext ctx = SessionContext.getInstance();
        ctx.inicializar(2, "João", "Gerente", "ativo", 5);
        assertFalse(ctx.isAdmin());
        assertEquals(5, ctx.getRestauranteEfetivo());
    }

    @Test
    public void testAdmin_comContextoRestaurante() {
        SessionContext ctx = SessionContext.getInstance();
        ctx.inicializar(1, "Admin User", "Admin", "ativo", 0);
        ctx.setRestauranteSelecionadoId(3);
        assertEquals(3, ctx.getRestauranteEfetivo());
        assertTrue(ctx.adminTemContextoRestaurante());
    }

    @Test
    public void testLimpar_resetaTudo() {
        SessionContext ctx = SessionContext.getInstance();
        ctx.inicializar(2, "Maria", "Gerente", "ativo", 7);
        ctx.limpar();
        assertNull(ctx.getNome());
        assertEquals(0, ctx.getFuncionarioId());
        assertEquals(0, ctx.getRestauranteId());
        assertFalse(ctx.isAdmin());
    }

    @Test
    public void testCargo_isAdminCaseInsensitive() {
        SessionContext ctx = SessionContext.getInstance();
        ctx.inicializar(1, "Test", "admin", "ativo", 0);
        assertTrue(ctx.isAdmin());
    }
}
