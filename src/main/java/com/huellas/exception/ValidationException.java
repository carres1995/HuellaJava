package com.huellas.exception;

/**
 * Excepción lanzada cuando una regla de negocio o validación de datos falla.
 */
public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
