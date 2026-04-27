package com.huellas.exception;

/**
 * Excepción personalizada para errores en el módulo de mascotas.
 * Referencia: SPEC-003
 */
public class PetException extends RuntimeException {
    public PetException(String message) {
        super(message);
    }

    public PetException(String message, Throwable cause) {
        super(message, cause);
    }
}
