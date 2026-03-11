package com.senac.food.verse.gui;

import com.senac.food.verse.SessionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AprovacaoCadastrosPanelContextTest {

    @BeforeEach
    void setUp() {
        SessionContext.getInstance().limpar();
    }

    @Test
    void deveRestringirPainelGlobalApenasParaGerentes() {
        SessionContext ctx = SessionContext.getInstance();
        ctx.inicializar(1, "Admin", "Admin", "ativo", 0);

        assertTrue(AprovacaoCadastrosPanel.isGlobalManagerMode(ctx));
        assertEquals("Gestão Global de Gerentes", AprovacaoCadastrosPanel.buildPanelTitle(ctx));
        assertEquals("Novo Gerente", AprovacaoCadastrosPanel.buildPrimaryActionLabel(ctx));
        assertEquals("Buscar gerente por nome, e-mail ou restaurante...", AprovacaoCadastrosPanel.buildSearchPlaceholder(ctx));
        assertEquals(List.of("Gerente"), AprovacaoCadastrosPanel.buildAllowedRoles(ctx));
    }

    @Test
    void deveUsarEquipeDoRestauranteQuandoAdminEntrarEmContexto() {
        SessionContext ctx = SessionContext.getInstance();
        ctx.inicializar(1, "Admin", "Admin", "ativo", 0);
        ctx.setRestauranteSelecionadoId(4);

        assertFalse(AprovacaoCadastrosPanel.isGlobalManagerMode(ctx));
        assertEquals("Gestão de Equipe do Restaurante", AprovacaoCadastrosPanel.buildPanelTitle(ctx));
        assertEquals("Novo Colaborador", AprovacaoCadastrosPanel.buildPrimaryActionLabel(ctx));
        assertEquals(
                List.of("Gerente", "Atendente", "Cozinheiro", "Entregador"),
                AprovacaoCadastrosPanel.buildAllowedRoles(ctx));
    }

    @Test
    void deveLimitarGerenteAEquipeOperacionalDoRestaurante() {
        SessionContext ctx = SessionContext.getInstance();
        ctx.inicializar(2, "Maria", "Gerente", "ativo", 7);

        assertFalse(AprovacaoCadastrosPanel.isGlobalManagerMode(ctx));
        assertEquals("Gestão de Equipe do Restaurante", AprovacaoCadastrosPanel.buildPanelTitle(ctx));
        assertEquals(
                List.of("Atendente", "Cozinheiro", "Entregador"),
                AprovacaoCadastrosPanel.buildAllowedRoles(ctx));
    }
}
