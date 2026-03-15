package com.senac.food.verse;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class PasswordUtils {

    private static final String HASH_PREFIX = "{SHA256}";

    private PasswordUtils() {}

    public static String hash(String senha) {
        if (senha == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(senha.getBytes(StandardCharsets.UTF_8));
            return HASH_PREFIX + toHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo SHA-256 indisponível.", e);
        }
    }

    public static boolean matches(String senhaDigitada, String senhaArmazenada) {
        if (senhaDigitada == null || senhaArmazenada == null) {
            return false;
        }
        if (senhaArmazenada.startsWith(HASH_PREFIX)) {
            return hash(senhaDigitada).equals(senhaArmazenada);
        }
        return senhaDigitada.equals(senhaArmazenada);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
