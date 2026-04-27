package com.huellas.util;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private ValidationUtil() {}

    
    public static boolean isEmpty(String text) {
        return text == null || text.trim().isEmpty();
    }

    /**
     * Valida si un email tiene un formato correcto.
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * Valida que un número de teléfono tenga una longitud mínima aceptable.
     */
    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) return false;
        // Permite números, espacios y el signo +
        return phone.matches("^[+]?[0-9\\s]{7,15}$");
    }

    /**
     * Valida que la contraseña cumpla con la longitud mínima de 8 caracteres.
     * Referencia: SPEC-002 BR-005
     */
    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }
}
