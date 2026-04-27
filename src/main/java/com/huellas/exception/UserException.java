package com.huellas.exception;

/**
 * Excepción para errores en la gestión de usuarios.
 * Referencia: SPEC-002
 */
public class UserException extends RuntimeException {
    public UserException(String message) {
        super(message);
    }
}
