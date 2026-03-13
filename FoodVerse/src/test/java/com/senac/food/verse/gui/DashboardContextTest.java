package com.senac.food.verse.gui;

import com.senac.food.verse.SessionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DashboardContextTest {

    @BeforeEach
    void setUp() {
        SessionContext.getInstance().limpar();
    }

    @Test
    void deveDescreverAdminGlobalSemRestauranteSelecionado() {
        SessionContext ctx = SessionContext.getInstance();
        ctx.inicializar(1, "Admin", "Admin", "ativo", 0);

        assertEquals("Admin global sem restaurante selecionado", TelaInicial.buildRestaurantContextText(ctx));
        assertEquals(
                "Modo global ativo. Selecione um restaurante para operar.",
                TelaInicial.buildOperationalModeText(ctx));
        assertEquals(
                "Você está no modo global. Selecione um restaurante para liberar os módulos operacionais.",
                HomePanel.buildHomeSummaryText(ctx));
    }

    @Test
    void deveDescreverOperacaoFiltradaPorRestaurante() {
        SessionContext ctx = SessionContext.getInstance();
        ctx.inicializar(2, "Maria", "Gerente", "ativo", 7);

        assertEquals("Restaurante em contexto: #7", TelaInicial.buildRestaurantContextText(ctx));
        assertEquals("Operando como Gerente no restaurante #7.", TelaInicial.buildOperationalModeText(ctx));
        assertEquals("Seu painel inicial já está filtrado para o restaurante #7.", HomePanel.buildHomeSummaryText(ctx));
    }

    @Test
    void deveUsarRestauranteSelecionadoQuandoAdminEntrarNoContexto() {
        SessionContext ctx = SessionContext.getInstance();
        ctx.inicializar(1, "Admin", "Admin", "ativo", 0);
        ctx.setRestauranteSelecionadoId(4);

        assertEquals("Restaurante em contexto: #4", TelaInicial.buildRestaurantContextText(ctx));
        assertEquals("Operando como Admin no restaurante #4.", TelaInicial.buildOperationalModeText(ctx));
    }

    @Test
    void deveExibirNomeDoRestauranteQuandoDisponivel() {
        SessionContext ctx = SessionContext.getInstance();
        ctx.inicializar(2, "Maria", "Gerente", "ativo", 7);
        ctx.setNomeRestaurante("Sabor da Casa");

        assertEquals("Restaurante em contexto: Sabor da Casa", TelaInicial.buildRestaurantContextText(ctx));
        assertEquals("Operando como Gerente no restaurante Sabor da Casa.", TelaInicial.buildOperationalModeText(ctx));
        assertEquals("Seu painel inicial já está filtrado para o restaurante Sabor da Casa.", HomePanel.buildHomeSummaryText(ctx));
    }

    @Test
    void deveExibirNomeDoRestauranteParaAdminComContexto() {
        SessionContext ctx = SessionContext.getInstance();
        ctx.inicializar(1, "Admin", "Admin", "ativo", 0);
        ctx.setRestauranteSelecionadoId(4);
        ctx.setNomeRestaurante("Pizzaria Bella");

        assertEquals("Restaurante em contexto: Pizzaria Bella", TelaInicial.buildRestaurantContextText(ctx));
        assertEquals("Operando como Admin no restaurante Pizzaria Bella.", TelaInicial.buildOperationalModeText(ctx));
    }
}
