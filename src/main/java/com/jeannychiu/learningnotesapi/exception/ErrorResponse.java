package com.jeannychiu.learningnotesapi.exception;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorResponse {
    private int status;
    private String message;
    private Object errors;
    private LocalDateTime timestamp;
}
