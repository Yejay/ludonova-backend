package com.bht.ludonova.exception;

public class SteamAuthenticationException extends RuntimeException {
    private final String errorCode;

    public SteamAuthenticationException(String message) {
        super(message);
        this.errorCode = "STEAM_AUTH_ERROR";
    }

    public SteamAuthenticationException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
