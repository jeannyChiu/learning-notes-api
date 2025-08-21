package com.jeannychiu.learningnotesapi.exception;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
public class ErrorResponse {
    private int status;
    private String message;
    private Object errors;
    private LocalDateTime timestamp;
    private String code;
    private Map<String, Object> details;
}
