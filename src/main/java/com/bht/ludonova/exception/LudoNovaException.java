package com.bht.ludonova.exception;

public class LudoNovaException extends RuntimeException {
    public LudoNovaException(String message) {
        super(message);
    }

    public LudoNovaException(String message, Throwable cause) {
        super(message, cause);
    }
}
