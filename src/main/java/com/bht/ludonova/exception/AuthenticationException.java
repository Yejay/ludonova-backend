package com.bht.ludonova.exception;

public class AuthenticationException extends LudoNovaException {
    public AuthenticationException(String message) {
        super(message, "AUTH_ERROR");
    }
}
