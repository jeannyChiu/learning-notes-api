package com.jeannychiu.learningnotesapi.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = StrongPasswordValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface StrongPassword {
    String message() default "密碼不符合強度要求";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}