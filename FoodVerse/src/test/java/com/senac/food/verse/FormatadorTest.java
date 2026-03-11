package com.senac.food.verse;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FormatadorTest {

    @Test
    public void testFormatarTelefoneNuloRetornaVazio() {
        assertEquals("", Formatador.formatarTelefone(null));
    }

    @Test
    public void testFormatarTelefoneEmBrancoRetornaVazio() {
        assertEquals("", Formatador.formatarTelefone("   "));
    }

    @Test
    public void testFormatarTelefoneCelularComMascara() {
        assertEquals("(11) 91234-5678", Formatador.formatarTelefone("(11) 91234-5678"));
    }

    @Test
    public void testFormatarTelefoneCurtoNaoQuebra() {
        assertDoesNotThrow(() -> Formatador.formatarTelefone("123456789"));
        assertEquals("123456789", Formatador.formatarTelefone("123456789"));
    }
}
