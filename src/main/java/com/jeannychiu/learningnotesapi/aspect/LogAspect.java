package com.jeannychiu.learningnotesapi.aspect;

import com.jeannychiu.learningnotesapi.service.ApiLogService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
public class LogAspect {
    private final ApiLogService apiLogService;

    public LogAspect(ApiLogService apiLogService) {
        this.apiLogService = apiLogService;
    }

    @Around("execution(* com.jeannychiu.learningnotesapi.controller..*(..))")
    public Object logApiCall(ProceedingJoinPoint joinPoint) throws Throwable{
        // 記錄呼叫前的時間
        LocalDateTime start = LocalDateTime.now();

        Object result = joinPoint.proceed();

        // 記錄呼叫後的時間
        LocalDateTime end = LocalDateTime.now();

        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if(attributes != null){
            HttpServletRequest request = attributes.getRequest();
            apiLogService.logApiRequest(joinPoint, request, result, start, end);
        }

        return result;
    }
}
