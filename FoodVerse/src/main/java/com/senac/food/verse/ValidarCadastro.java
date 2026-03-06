package com.senac.food.verse;

import java.awt.Color;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class ValidarCadastro {

    public Boolean validarUsername(String username, JLabel label) {
        if (username.isEmpty()) {
            showError(label, "Este campo é obrigatório");
            return false;
        }
        clearError(label);
        return verificarDisponibilidade("userName", username, label);
    }

    public Boolean validarEmail(String email, JLabel label) {
        if (email.isEmpty()) {
            showError(label, "Este campo é obrigatório");
            return false;
        }
        String regex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(email);

        if (!matcher.matches()) {
            showError(label, "Formato de e-mail inválido");
            return false;
        }
        clearError(label);
        return verificarDisponibilidade("email", email, label);
    }

    public Boolean validarTelefone(String phone, JLabel label) {
        if (phone.isEmpty()) {
            showError(label, "Este campo é obrigatório");
            return false;
        }
        clearError(label);
        return verificarDisponibilidade("phone", phone, label);
    }

    private Boolean verificarDisponibilidade(String coluna, String valor, JLabel label) {
        ConexaoBanco banco = new ConexaoBanco();
        Boolean disponivel = false;

        try (Connection conn = banco.abrirConexao()) {
            // MODO OFFLINE: Considera disponível
            if (conn == null) {
                clearError(label);
                return true; 
            }

            String query = "SELECT COUNT(*) FROM tb_funcionarios WHERE " + coluna + " = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, valor);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        disponivel = (rs.getInt(1) == 0);
                    }
                }
            }

            if (!disponivel) {
                showError(label, "Este " + coluna + " já está em uso");
            } else {
                clearError(label);
            }
        } catch (Exception ex) {
            System.err.println("Erro ao consultar " + coluna + ": " + ex.getMessage());
        }
        return disponivel;
    }

    public boolean validarNome(String name, JLabel label) {
        if (name.isEmpty()) {
            showError(label, "Este campo é obrigatório");
            return false;
        }
        clearError(label);
        return true;
    }

    public boolean validarAsSenhas(String senha, String confirmationSenha, JLabel labelSenha, JLabel labelConfirmation) {
        String regex = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

        if (senha.isEmpty()) {
            showError(labelSenha, "Este campo é obrigatório");
            return false;
        } else if (!senha.matches(regex)) {
            showError(labelSenha, "A senha deve conter mín: 8 chars, 1 Maiús, 1 Minús, 1 Num, 1 Especial.");
            return false;
        } else if (!senha.equals(confirmationSenha)) {
            showError(labelConfirmation, "As senhas não coincidem");
            return false;
        } else {
            clearError(labelSenha);
            clearError(labelConfirmation);
            return true;
        }
    }

    public boolean validarCargos(String cargo, JLabel label) {
        if (cargo.isEmpty()) {
            showError(label, "Por favor, preencha um cargo");
            return false;
        } else {
           clearError(label);
           return true;
        }
    }
    
    public void showError(JLabel label, String message) {
        label.setText(message);
        label.setForeground(Color.RED);
    }

    public void clearError(JLabel label) {
        label.setText("");
        label.setForeground(Color.WHITE);
    }

    public void limparCampos(JTextField username, JTextField nome, JTextField email, JTextField phone, JTextField senha, JTextField confirmarSenha) {
        username.setText("");
        nome.setText("");
        email.setText("");
        phone.setText("");
        senha.setText("");
        confirmarSenha.setText("");
    }
}