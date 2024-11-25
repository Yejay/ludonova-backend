package com.bht.ludonova.exception;

public class GameInstanceNotFoundException extends RuntimeException {
    public GameInstanceNotFoundException(String message) {
        super(message);
    }
}
