package com.jeannychiu.learningnotesapi.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannychiu.learningnotesapi.model.ApiLog;
import com.jeannychiu.learningnotesapi.model.User;
import com.jeannychiu.learningnotesapi.repository.ApiLogRepository;
import com.jeannychiu.learningnotesapi.repository.UserRepository;
import com.jeannychiu.learningnotesapi.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class ApiLogService {
    private final ApiLogRepository apiLogRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ObjectMapper mapper;

    public ApiLogService(ApiLogRepository apiLogRepository, JwtUtil jwtUtil, UserRepository userRepository, ObjectMapper mapper) {
        this.apiLogRepository = apiLogRepository;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    /**
     * 記錄一般 API 請求（給 LogAspect 使用）
     */
    public void logApiRequest(ProceedingJoinPoint joinPoint, HttpServletRequest request, Object result, 
                            LocalDateTime startTime, LocalDateTime endTime) {
        try {
            String uri = request.getRequestURI();
            String method = request.getMethod();
            Long userId = extractUserIdFromRequest(request);
            
            int statusCode = 200;
            if (result instanceof org.springframework.http.ResponseEntity) {
                statusCode = ((org.springframework.http.ResponseEntity<Object>) result).getStatusCodeValue();
            }
            
            Long duration = Duration.between(startTime, endTime).toMillis();
            
            String requestBody = extractRequestData(joinPoint, method);
            String responseBody = serializeResponse(result);
            
            ApiLog apiLog = createApiLog(uri, method, requestBody, responseBody, statusCode, 
                                       startTime, endTime, duration, userId);
            
            apiLogRepository.save(apiLog);
        } catch (Exception e) {
            System.err.println("Failed to log API request: " + e.getMessage());
        }
    }

    /**
     * 記錄安全相關錯誤（給 security handlers 使用）
     */
    public void logSecurityError(HttpServletRequest request, int statusCode, String errorMessage) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String uri = request.getRequestURI();
            String method = request.getMethod();
            Long userId = extractUserIdFromRequest(request);
            
            // 對於安全錯誤，不記錄請求內容以避免敏感資訊洩露
            String requestBody = "";
            String responseBody = errorMessage;
            
            ApiLog apiLog = createApiLog(uri, method, requestBody, responseBody, statusCode, 
                                       now, now, 0L, userId);
            
            apiLogRepository.save(apiLog);
        } catch (Exception e) {
            System.err.println("Failed to log security error: " + e.getMessage());
        }
    }

    private ApiLog createApiLog(String uri, String method, String requestBody, String responseBody, 
                              int statusCode, LocalDateTime startTime, LocalDateTime endTime, 
                              Long duration, Long userId) {
        ApiLog apiLog = new ApiLog();
        apiLog.setApiPath(uri);
        apiLog.setHttpMethod(method);
        apiLog.setRequestBody(requestBody);
        apiLog.setResponseBody(responseBody);
        apiLog.setStatusCode(statusCode);
        apiLog.setRequestTime(startTime);
        apiLog.setResponseTime(endTime);
        apiLog.setDuration(duration);
        apiLog.setUserId(userId);
        return apiLog;
    }

    private Long extractUserIdFromRequest(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String email = jwtUtil.getEmailFromToken(token);
                
                User user = userRepository.findByEmail(email).orElse(null);
                return user != null ? user.getId() : null;
            }
        } catch (Exception e) {
            // JWT 解析失敗或使用者不存在，回傳 null
        }
        return null;
    }

    private String extractRequestData(ProceedingJoinPoint joinPoint, String httpMethod) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Object[] args = joinPoint.getArgs();

        if (isPostOrPutMethod(httpMethod)) {
            return extractRequestBodyData(method, args);
        } else {
            return extractGetDeleteData(args);
        }
    }

    private boolean isPostOrPutMethod(String httpMethod) {
        return "POST".equals(httpMethod) || "PUT".equals(httpMethod);
    }

    private String extractRequestBodyData(Method method, Object[] args) {
        for (int i = 0; i < method.getParameterCount(); i++) {
            if (method.getParameters()[i].isAnnotationPresent(RequestBody.class)) {
                return serializeToJson(args[i]);
            }
        }
        return "";
    }

    private String extractGetDeleteData(Object[] args) {
        if (args.length == 0) {
            return "";
        }

        try {
            return mapper.writeValueAsString(args);
        } catch (JsonProcessingException e) {
            return buildParametersManually(args);
        }
    }

    private String serializeResponse(Object result) {
        try {
            String fullResponseBody = mapper.writeValueAsString(result);
            
            // 限制回應內容大小（避免過大導致資料庫問題）
            if (fullResponseBody != null && fullResponseBody.length() > 50000) {
                return fullResponseBody.substring(0, 50000) + "... [TRUNCATED]";
            } else {
                return fullResponseBody;
            }
        } catch (Exception e) {
            return "Failed to parse response body: " + e.getMessage();
        }
    }

    private String serializeToJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            return "Failed to serialize: " + e.getMessage();
        }
    }

    private String buildParametersManually(Object[] args) {
        StringBuilder params = new StringBuilder("[");
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                params.append(",");
            }
            appendParameter(params, args[i]);
        }
        params.append("]");
        return params.toString();
    }

    private void appendParameter(StringBuilder params, Object arg) {
        try {
            params.append(mapper.writeValueAsString(arg));
        } catch (Exception ex) {
            params.append("\"").append(arg).append("\"");
        }
    }
}