package com.jeannychiu.learningnotesapi.dto;

import com.jeannychiu.learningnotesapi.validator.StrongPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @StrongPassword
    private String password;
}
