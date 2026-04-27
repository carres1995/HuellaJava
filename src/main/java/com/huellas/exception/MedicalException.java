package com.huellas.exception;

public class MedicalException extends RuntimeException {
    public MedicalException(String message){
        super(message);
    }

    public MedicalException(String message, Throwable cause){
        super(message, cause);
        //mensage de error en para el usuario, causa del fallo para el programador.
    }

}
