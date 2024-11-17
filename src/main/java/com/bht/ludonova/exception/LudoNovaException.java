package com.bht.ludonova.exception;

public class LudoNovaException extends RuntimeException {
    private final String errorCode;

    public LudoNovaException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
