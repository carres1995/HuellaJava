package com.huellas.exception;

/**
 * Excepción para errores de autenticación (Login).
 * Referencia: SPEC-001
 */
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
