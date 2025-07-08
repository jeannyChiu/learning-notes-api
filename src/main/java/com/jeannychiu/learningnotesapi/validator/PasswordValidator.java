package com.jeannychiu.learningnotesapi.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PasswordValidator {
    // 密碼最小長度
    private static final int MIN_LENGTH = 8;
    
    // 密碼規則的正則表達式
    private static final Pattern HAS_UPPERCASE = Pattern.compile("[A-Z]");
    private static final Pattern HAS_LOWERCASE = Pattern.compile("[a-z]");
    private static final Pattern HAS_NUMBER = Pattern.compile("\\d");
    private static final Pattern HAS_SPECIAL_CHAR = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");
    
    /**
     * 驗證密碼強度
     * @param password 要驗證的密碼
     * @return 如果密碼符合強度要求，返回空列表；否則返回錯誤訊息列表
     */
    public static List<String> validate(String password) {
        List<String> errors = new ArrayList<>();
        
        // 檢查密碼長度
        if (password == null || password.length() < MIN_LENGTH) {
            errors.add("密碼長度必須至少為 " + MIN_LENGTH + " 個字元");
        }
        
        // 檢查是否包含大寫字母
        if (password == null || !HAS_UPPERCASE.matcher(password).find()) {
            errors.add("密碼必須包含至少一個大寫字母");
        }
        
        // 檢查是否包含小寫字母
        if (password == null || !HAS_LOWERCASE.matcher(password).find()) {
            errors.add("密碼必須包含至少一個小寫字母");
        }
        
        // 檢查是否包含數字
        if (password == null || !HAS_NUMBER.matcher(password).find()) {
            errors.add("密碼必須包含至少一個數字");
        }
        
        // 檢查是否包含特殊字元
        if (password == null || !HAS_SPECIAL_CHAR.matcher(password).find()) {
            errors.add("密碼必須包含至少一個特殊字元 (!@#$%^&*(),.?\":{}|<>)");
        }
        
        return errors;
    }
    
    /**
     * 檢查密碼是否符合強度要求
     * @param password 要檢查的密碼
     * @return 如果密碼符合強度要求，返回 true；否則返回 false
     */
    public static boolean isValid(String password) {
        return validate(password).isEmpty();
    }
}