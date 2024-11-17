package com.bht.ludonova.dto;

import lombok.Data;

@Data
public class ErrorResponse {
    private final String errorCode;
    private final String message;
    private final int status;
}
