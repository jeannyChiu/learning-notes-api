package com.jeannychiu.learningnotesapi.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String BEARER_SCHEME = "bearer";
    
    @Bean
    public OpenAPI customOpenAPI() {
        // 1. 建立 Info 物件設定 API 基本資訊
        Info info = new Info()
                .title("線上學習筆記平台 API")
                .description("提供使用者一個可以隨時做筆記，並且方便查詢過往筆記的服務。")
                .version("1.0.0")
                .contact(new Contact()
                        .name("Jeanny Chiu")
                        .email("jeannychiu608@gmail.com")
                        .url("https://github.com/jeannychiu"));

        // 2. 建立 SecurityScheme 設定 JWT 認證
        SecurityScheme securityScheme = new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme(BEARER_SCHEME)
                .bearerFormat("JWT")
                .description("請輸入登入後取得的 JWT token");

        // 3. 組合成完整的 OpenAPI 物件
        return new OpenAPI()
                .info(info)
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, securityScheme)
                )
                .addSecurityItem(new SecurityRequirement()
                        .addList(BEARER_SCHEME));
    }
}
