package com.senac.food.verse.gui;

import com.senac.food.verse.SessionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PermissionCheckerTest {

    @BeforeEach
    void setUp() {
        SessionContext.getInstance().limpar();
    }

    @Test
    void adminGlobalSemContextoNaoAcessaModulosOperacionais() {
        SessionContext ctx = SessionContext.getInstance();
        ctx.inicializar(1, "Admin", "Admin", "ativo", 0);

        assertFalse(PermissionChecker.canAccessModule(ctx, "ESTOQUE"));
        assertFalse(PermissionChecker.canAccessModule(ctx, "MESAS"));
        assertTrue(PermissionChecker.canAccessModule(ctx, "RESTAURANTES"));
        assertEquals("Selecione um restaurante antes de acessar este módulo.",
                PermissionChecker.buildBlockedModuleMessage(ctx, "ESTOQUE"));
    }

    @Test
    void gerentePodeEditarEstoqueECancelarReservaDoSeuRestaurante() {
        SessionContext ctx = SessionContext.getInstance();
        ctx.inicializar(2, "Maria", "Gerente", "ativo", 7);

        assertTrue(PermissionChecker.canAccessInventory(ctx));
        assertTrue(PermissionChecker.canEditInventory(ctx));
        assertTrue(PermissionChecker.canAccessTables(ctx));
        assertTrue(PermissionChecker.canCancelReservation(ctx));
    }

    @Test
    void cozinheiroTemAcessoSomenteLeituraAoEstoque() {
        SessionContext ctx = SessionContext.getInstance();
        ctx.inicializar(3, "Caio", "Cozinheiro", "ativo", 4);

        assertTrue(PermissionChecker.canAccessInventory(ctx));
        assertFalse(PermissionChecker.canEditInventory(ctx));
        assertFalse(PermissionChecker.canAccessTables(ctx));
    }
}
