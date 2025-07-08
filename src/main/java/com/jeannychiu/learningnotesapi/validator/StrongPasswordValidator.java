package com.jeannychiu.learningnotesapi.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {

    @Override
    public void initialize(StrongPassword constraintAnnotation) {
        // 初始化，如果需要的話
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        // 如果密碼為空，讓 @NotBlank 處理
        if (password == null || password.isEmpty()) {
            return true;
        }
        
        List<String> errors = PasswordValidator.validate(password);
        
        // 如果有錯誤，自定義錯誤訊息
        if (!errors.isEmpty()) {
            context.disableDefaultConstraintViolation();
            
            // 將所有錯誤訊息合併為一個字串
            String errorMessage = String.join("; ", errors);
            context.buildConstraintViolationWithTemplate(errorMessage)
                   .addConstraintViolation();
            
            return false;
        }
        
        return true;
    }
}