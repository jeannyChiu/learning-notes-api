package com.jeannychiu.learningnotesapi.aspect;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannychiu.learningnotesapi.model.ApiLog;
import com.jeannychiu.learningnotesapi.repository.ApiLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.time.LocalDateTime;

@Aspect
@Component
public class LogAspect {
    private final ApiLogRepository apiLogRepository;
    private ObjectMapper mapper = new ObjectMapper();

    public LogAspect(ApiLogRepository apiLogRepository) {
        this.apiLogRepository = apiLogRepository;
    }

    @Around("execution(* com.jeannychiu.learningnotesapi.controller..*(..))")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable{
        // 記錄呼叫前的時間
        LocalDateTime start = LocalDateTime.now();

        Object result = joinPoint.proceed();

        int statusCode = 200;
        if (result instanceof org.springframework.http.ResponseEntity) {
            statusCode = ((org.springframework.http.ResponseEntity<?>) result).getStatusCodeValue();
        }
        System.out.println("StatusCode: " + statusCode);

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String uri = null;
        String method = null;
        if(attributes != null){
            HttpServletRequest request = attributes.getRequest();
            uri = request.getRequestURI();
            method = request.getMethod();
            System.out.println("Path: " + uri + " Method: " + method);
        }

        // 記錄呼叫後的時間
        LocalDateTime end = LocalDateTime.now();

        // 算出duration
        Long time = Duration.between(start, end).toMillis();

        String requestBody = "";
        String responseBody = "";

        try {
            // 將Request參數轉成字串 (RequestBody)
            requestBody = mapper.writeValueAsString(joinPoint.getArgs());
            // 將ResponseBody轉成字串
            responseBody = mapper.writeValueAsString(result);
        } catch (Exception e) {
            responseBody = "Failed to parse response body";
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

        // 存進資料庫
        apiLogRepository.save(apilog);
        return result;
    }
}
