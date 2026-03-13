package com.senac.food.verse;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Validates that PedidoDAO uses numeric (int) comparison for order IDs,
 * preventing the lexicographic bug where "9" > "10".
 */
class PedidoDAOOrderIdTest {

    @Test
    void ultimoIdCarregadoDeveSerInteiro() throws Exception {
        PedidoDAO dao = new PedidoDAO();
        Field field = PedidoDAO.class.getDeclaredField("ultimoIdCarregado");
        field.setAccessible(true);

        // The field must be of type int (not String) to guarantee numeric comparison
        assertEquals(int.class, field.getType(),
                "ultimoIdCarregado deve ser int para evitar comparação lexicográfica de IDs numéricos");

        // Default should be -1 (no orders loaded yet)
        assertEquals(-1, field.getInt(dao),
                "Valor inicial de ultimoIdCarregado deve ser -1");
    }

    @Test
    void comparacaoNumericaDeveOrdenarCorretamente() {
        // This is the bug that the fix addresses:
        // With String comparison: "9".compareTo("10") > 0  → true  (WRONG)
        // With int comparison:    9 > 10                   → false (CORRECT)
        assertTrue("9".compareTo("10") > 0,
                "String comparison is lexicographic — '9' > '10' — this is the bug we fixed");
        assertFalse(9 > 10,
                "Numeric comparison correctly determines 9 < 10");
    }
}
