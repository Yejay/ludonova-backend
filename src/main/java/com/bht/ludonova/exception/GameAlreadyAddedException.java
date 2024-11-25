package com.bht.ludonova.exception;

public class GameAlreadyAddedException extends RuntimeException {
    public GameAlreadyAddedException(String message) {
        super(message);
    }
}
