package com.huellas.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utilidades para el manejo de contraseñas y seguridad.
 * Referencia: SPEC-001 / SPEC-002 (Hashing SHA-256)
 */
public class PasswordUtil {

    private PasswordUtil() {}

    /**
     * Genera un hash SHA-256 a partir de una cadena de texto plano.
     */
    public static String hash(String plainPassword) {
        if (plainPassword == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(
                plainPassword.getBytes(StandardCharsets.UTF_8));
            
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error al inicializar algoritmo de hash", e);
        }
    }

    /**
     * Compara una contraseña en texto plano con un hash almacenado.
     */
    public static boolean verify(String plainPassword, String storedHash) {
        if (plainPassword == null || storedHash == null) return false;
        String hashedInput = hash(plainPassword);
        return storedHash.equals(hashedInput);
    }

    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
