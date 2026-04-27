package com.huellas.exception;

/**
 * Excepción personalizada para errores en el módulo de citas.
 * Referencia: SPEC-004
 */
public class AppointmentException extends RuntimeException {
    public AppointmentException(String message) {
        super(message);
    }

    public AppointmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
