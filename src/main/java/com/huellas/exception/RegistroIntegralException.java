package com.huellas.exception;

public class RegistroIntegralException extends RuntimeException {
    public RegistroIntegralException(String message) {
        super(message);
    }
    
    public RegistroIntegralException(String message, Throwable cause) {
        super(message, cause);
    }
}
