package com.senac.food.verse.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OperationalPanelsFormattingTest {

    @Test
    void pedidosUsaCoresPadraoParaStatusPrincipais() {
        assertEquals(UIConstants.WARNING_ORANGE, PedidosPanel.resolverCorStatus("pendente"));
        assertEquals(UIConstants.INFO_BLUE, PedidosPanel.resolverCorStatus("em rota"));
    }

    @Test
    void pedidosFormataSlaFechadoComCorMutedPadrao() {
        String markup = PedidosPanel.calcularSlaFormatado("12:00", "concluido");

        assertTrue(markup.contains(UIConstants.toHex(UIConstants.FG_MUTED)));
        assertTrue(markup.contains("Fechado"));
    }

    @Test
    void entregasUsaCorPadraoNoSlaDeAtencao() {
        String markup = EntregasPainel.calcularTempoSlaFormatado("00:00");

        assertTrue(markup.contains(UIConstants.toHex(UIConstants.WARNING_ORANGE))
                || markup.contains(UIConstants.toHex(UIConstants.DANGER_RED)));
    }

    @Test
    void entregasMantemFallbackDeHoraQuandoFormatoForInvalido() {
        assertEquals(" Saiu às invalida", EntregasPainel.calcularTempoSlaFormatado("invalida"));
    }

    @Test
    void entregasMostraFallbackPadraoQuandoHoraNaoVierPreenchida() {
        String markup = EntregasPainel.calcularTempoSlaFormatado(null);

        assertTrue(markup.contains("Horário indisponível"));
        assertTrue(markup.contains(UIConstants.toHex(UIConstants.FG_MUTED)));
    }

    @Test
    void mesasUsaCorWarningPadraoParaReservaProxima() {
        assertEquals(UIConstants.WARNING_ORANGE, GestaoMesasPanel.resolveStatusColor("PROXIMA"));
    }
}
