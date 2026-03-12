package com.senac.food.verse.gui;

import org.junit.jupiter.api.Test;

import java.awt.Dimension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToastTest {

    @Test
    void usaLarguraMinimaParaMensagensCurtas() {
        Dimension size = Toast.calculateToastSize("OK");

        assertEquals(360, size.width);
        assertTrue(size.height >= 56);
    }

    @Test
    void aumentaAlturaQuandoMensagemForLonga() {
        Dimension curta = Toast.calculateToastSize("Operação concluída.");
        Dimension longa = Toast.calculateToastSize("Operação concluída com várias observações importantes para garantir que o feedback visual continue legível sem cortar o texto em telas menores.");

        assertTrue(longa.height > curta.height);
        assertTrue(longa.width >= curta.width);
    }

    @Test
    void geraHtmlPadraoQuandoMensagemVierVazia() {
        String html = Toast.toHtmlMessage(" ");

        assertTrue(html.contains("Operação concluída."));
        assertTrue(html.startsWith("<html>"));
    }
}
