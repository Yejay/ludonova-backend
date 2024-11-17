package com.bht.ludonova.exception;

public class SteamAuthenticationException extends AuthenticationException {
    public SteamAuthenticationException(String message) {
        super("Steam authentication failed: " + message);
    }
}
