package com.senac.food.verse;

import org.junit.jupiter.api.Test;
import javax.swing.JLabel;
import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

public class ValidarCadastroTest {

    @Test
    public void testValidarUsernameVazio() {
        ValidarCadastro validator = new ValidarCadastro();
        JLabel label = new JLabel();
        assertFalse(validator.validarUsername("", label));
        assertEquals("Este campo é obrigatório", label.getText());
        assertEquals(Color.RED, label.getForeground());
    }

    @Test
    public void testValidarEmailFormatoInvalido() {
        ValidarCadastro validator = new ValidarCadastro();
        JLabel label = new JLabel();
        assertFalse(validator.validarEmail("email_invalido", label));
        assertEquals("Formato de e-mail inválido", label.getText());
        assertEquals(Color.RED, label.getForeground());
    }

    @Test
    public void testValidarNomeNaoVazio() {
        ValidarCadastro validator = new ValidarCadastro();
        JLabel label = new JLabel();
        assertTrue(validator.validarNome("Nome Teste", label));
        assertEquals("", label.getText());
        assertEquals(Color.WHITE, label.getForeground());
    }

    @Test
    public void testSenhasNaoCoincidem() {
        ValidarCadastro validator = new ValidarCadastro();
        JLabel labelSenha = new JLabel();
        JLabel labelConfirmacao = new JLabel();
        assertFalse(validator.validarAsSenhas("senha1", "senha2", labelSenha, labelConfirmacao));
        assertEquals("As senhas não coincidem", labelConfirmacao.getText());
        assertEquals(Color.RED, labelConfirmacao.getForeground());
    }

    @Test
    public void testSenhaFracaMesmoQuandoCoincide() {
        ValidarCadastro validator = new ValidarCadastro();
        JLabel labelSenha = new JLabel();
        JLabel labelConfirmacao = new JLabel();
        assertFalse(validator.validarAsSenhas("senhafraca", "senhafraca", labelSenha, labelConfirmacao));
        assertEquals("A senha deve conter mín: 8 chars, 1 Maiús, 1 Minús, 1 Num, 1 Especial.", labelSenha.getText());
        assertEquals(Color.WHITE, labelConfirmacao.getForeground());
    }
}
