package com.huellas.exception;

/**
 * Excepción lanzada cuando no se encuentra un recurso solicitado (por ID, email, etc).
 */
public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
