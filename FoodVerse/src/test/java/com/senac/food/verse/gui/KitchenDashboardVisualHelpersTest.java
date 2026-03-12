package com.senac.food.verse.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KitchenDashboardVisualHelpersTest {

    @Test
    void cozinhaFormataOrigemDeliverySemEmoji() {
        assertEquals("Delivery", GestaoCozinhaPanel.buildKitchenOriginText("Delivery", "12"));
    }

    @Test
    void cozinhaFormataOrigemSalaoComMesa() {
        assertEquals("Salão (7)", GestaoCozinhaPanel.buildKitchenOriginText("Salão", "7"));
    }

    @Test
    void cozinhaFormataOrigemSalaoSemMesaComoFallbackSeguro() {
        assertEquals("Salão", GestaoCozinhaPanel.buildKitchenOriginText("Salão", null));
    }

    @Test
    void cozinhaFormataObservacaoComCorWarningPadrao() {
        String markup = GestaoCozinhaPanel.buildKitchenObservationMarkup("Sem cebola");

        assertTrue(markup.contains(UIConstants.toHex(UIConstants.WARNING_ORANGE)));
        assertTrue(markup.contains("Sem cebola"));
    }

    @Test
    void dashboardUsaMensagemPadraoParaFalhaDeModulo() {
        assertEquals("Não foi possível carregar o módulo Entregas.",
                TelaInicial.buildModuleLoadErrorText("Entregas"));
    }
}
