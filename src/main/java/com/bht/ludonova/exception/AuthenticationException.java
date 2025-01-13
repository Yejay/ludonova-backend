package com.bht.ludonova.exception;

public class AuthenticationException extends RuntimeException {
    private final String errorCode;

    public AuthenticationException(String message) {
        super(message);
        this.errorCode = "AUTHENTICATION_ERROR";
    }

    public AuthenticationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
