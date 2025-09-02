package com.jeannychiu.learningnotesapi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannychiu.learningnotesapi.service.ApiLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApiLogService apiLogService;

    public JwtAuthenticationEntryPoint(ApiLogService apiLogService) {
        this.apiLogService = apiLogService;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        log.error("未授權的請求: {}", authException.getMessage());
        
        int statusCode = HttpServletResponse.SC_UNAUTHORIZED;
        String errorMessage = "未授權：請登入或檢查您的 Token";
        
        // 記錄到 api_log 資料表
        apiLogService.logSecurityError(request, statusCode, errorMessage);
        
        response.setStatus(statusCode);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", statusCode);
        errorDetails.put("message", errorMessage);
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        
        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }
}
