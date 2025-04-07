package com.senac.food.verse;

public class Formatador {
    public static String formatarTelefone(String phone) {
        // Remove caracteres não numéricos
        phone = phone.replaceAll("[^0-9]", "");

        // Verifica se tem pelo menos 10 dígitos
        if (phone.length() < 10) {
            return phone; // Retorna sem formatação se for muito curto
        }

        // Limita para no máximo 11 dígitos
        phone = phone.substring(0, Math.min(phone.length(), 11));

        // Verifica se é um telefone fixo (10 dígitos) ou celular (11 dígitos)
        if (phone.length() == 10) {
            return String.format("(%s) %s-%s",
                                 phone.substring(0, 2),  // DDD
                                 phone.substring(2, 6),  // Primeiros 4 dígitos
                                 phone.substring(6, 10)); // Últimos 4 dígitos
        } else { // 11 dígitos (celular)
            return String.format("(%s) %s-%s",
                                 phone.substring(0, 2),  // DDD
                                 phone.substring(2, 7),  // Primeiros 5 dígitos
                                 phone.substring(7, 11)); // Últimos 4 dígitos
        }
    }
}
