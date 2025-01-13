package com.bht.ludonova.exception;

import com.bht.ludonova.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(AuthenticationException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("errorCode", ex.getErrorCode());

        // If it's an email verification error, extract the email from the message
        if ("EMAIL_NOT_VERIFIED".equals(ex.getErrorCode())) {
            String email = extractEmailFromMessage(ex.getMessage());
            if (email != null) {
                body.put("email", email);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    private String extractEmailFromMessage(String message) {
        // Extract email from the message format: "Please verify your email (email@example.com) before logging in"
        int startIndex = message.indexOf('(');
        int endIndex = message.indexOf(')');
        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            return message.substring(startIndex + 1, endIndex);
        }
        return null;
    }

    @ExceptionHandler(SteamAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleSteamAuthenticationException(SteamAuthenticationException ex) {
        ErrorResponse error = new ErrorResponse(
                ex.getErrorCode(),
                ex.getMessage(),
                HttpStatus.UNAUTHORIZED.value()
        );
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                "INTERNAL_ERROR",
                "An unexpected error occurred",
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex) {
        ErrorResponse error = new ErrorResponse(
                "VALIDATION_ERROR",
                ex.getMessage(),
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse error = new ErrorResponse(
                "VALIDATION_ERROR",
                "Validation failed: " + errors,
                HttpStatus.BAD_REQUEST.value()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ReviewNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReviewNotFoundException(ReviewNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
                "REVIEW_NOT_FOUND",
                ex.getMessage(),
                HttpStatus.NOT_FOUND.value()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(DuplicateReviewException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateReviewException(DuplicateReviewException ex) {
        ErrorResponse error = new ErrorResponse(
                "DUPLICATE_REVIEW",
                ex.getMessage(),
                HttpStatus.CONFLICT.value()
        );
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }
}
