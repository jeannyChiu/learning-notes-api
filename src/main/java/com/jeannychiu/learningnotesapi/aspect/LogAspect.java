package com.jeannychiu.learningnotesapi.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannychiu.learningnotesapi.model.ApiLog;
import com.jeannychiu.learningnotesapi.repository.ApiLogRepository;
import com.jeannychiu.learningnotesapi.repository.UserRepository;
import com.jeannychiu.learningnotesapi.security.JwtUtil;
import com.jeannychiu.learningnotesapi.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.Duration;
import java.time.LocalDateTime;

@Aspect
@Component
public class LogAspect {
    private final ApiLogRepository apiLogRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final ObjectMapper mapper;

    public LogAspect(ApiLogRepository apiLogRepository, JwtUtil jwtUtil, UserRepository userRepository, ObjectMapper mapper) {
        this.apiLogRepository = apiLogRepository;
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Around("execution(* com.jeannychiu.learningnotesapi.controller..*(..))")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable{
        // 記錄呼叫前的時間
        LocalDateTime start = LocalDateTime.now();

        Object result = joinPoint.proceed();

        int statusCode = 200;
        if (result instanceof org.springframework.http.ResponseEntity) {
            statusCode = ((org.springframework.http.ResponseEntity<Object>) result).getStatusCodeValue();
        }
        System.out.println("StatusCode: " + statusCode);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String uri = null;
        String method = null;
        Long userId = null;
        
        if(attributes != null){
            HttpServletRequest request = attributes.getRequest();
            uri = request.getRequestURI();
            method = request.getMethod();
            
            // 提取使用者 ID
            userId = extractUserIdFromRequest(request);
            
            System.out.println("Path: " + uri + " Method: " + method + " UserId: " + userId);
        }

        // 記錄呼叫後的時間
        LocalDateTime end = LocalDateTime.now();

        // 算出duration
        Long time = Duration.between(start, end).toMillis();

        String requestBody = "";
        String responseBody = "";

        try {
            // 使用新方法來提取 @RequestBody 參數
            requestBody = extractRequestData(joinPoint, method);
            // 將ResponseBody轉成字串
            String fullResponseBody = mapper.writeValueAsString(result);
            
            // 限制回應內容大小（避免過大導致資料庫問題）
            if (fullResponseBody != null && fullResponseBody.length() > 50000) {
                responseBody = fullResponseBody.substring(0, 50000) + "... [TRUNCATED]";
            } else {
                responseBody = fullResponseBody;
            }
        } catch (Exception e) {
            responseBody = "Failed to parse response body: " + e.getMessage();
        }

        // 組成ApiLog
        ApiLog apilog = new ApiLog();
        apilog.setApiPath(uri);
        apilog.setHttpMethod(method);
        apilog.setRequestTime(start);
        apilog.setResponseTime(end);
        apilog.setDuration(time);
        apilog.setRequestBody(requestBody);
        apilog.setResponseBody(responseBody);
        apilog.setStatusCode(statusCode);

        // 設定使用者 ID
        apilog.setUserId(userId);

        // 存進資料庫
        apiLogRepository.save(apilog);
        return result;
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
            System.out.println("Failed to extract user ID: " + e.getMessage());
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
