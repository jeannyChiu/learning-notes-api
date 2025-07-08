package com.jeannychiu.learningnotesapi.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class JwtSecretConfig implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(JwtSecretConfig.class);
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Override
    public void run(String... args) {
        // 檢查 JWT 密鑰強度
        if (jwtSecret.length() < 32) {
            log.warn("安全警告: JWT 密鑰長度不足，建議至少 32 個字元");
        }
        
        // 檢查是否使用預設密鑰
        if (jwtSecret.equals("kM8DG2xjbQP7Rq4tYF3sZpW5vN1cL6aE9HmUdV0yX7C")) {
            log.warn("安全警告: 正在使用預設的 JWT 密鑰，建議在生產環境中使用環境變數設定不同的密鑰");
        }
    }
}