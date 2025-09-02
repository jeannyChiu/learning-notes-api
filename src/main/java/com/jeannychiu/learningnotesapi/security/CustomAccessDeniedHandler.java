package com.jeannychiu.learningnotesapi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannychiu.learningnotesapi.service.ApiLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private static final Logger log = LoggerFactory.getLogger(CustomAccessDeniedHandler.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApiLogService apiLogService;

    public CustomAccessDeniedHandler(ApiLogService apiLogService) {
        this.apiLogService = apiLogService;
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        log.error("拒絕存取: {}", accessDeniedException.getMessage());
        
        int statusCode = HttpServletResponse.SC_FORBIDDEN;
        String errorMessage = "您沒有權限存取此資源";
        
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
