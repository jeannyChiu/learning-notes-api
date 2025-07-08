package com.jeannychiu.learningnotesapi.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;

@Component
@Order(1) // 使用最高優先級，確保在所有 Spring Security 過濾器之前執行
public class TestEndpointFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(TestEndpointFilter.class);

    @Value("${app.enable-test-endpoints:false}")
    private boolean enableTestEndpoints;
    
    @Value("${app.test-endpoints-allowed-ips:127.0.0.1,0:0:0:0:0:0:0:1}")
    private String allowedIpsString;
    
    private List<String> allowedIps;
    
    @Override
    public void init(FilterConfig filterConfig) {
        allowedIps = Arrays.asList(allowedIpsString.split(","));
        log.info("測試端點 IP 限制已啟用，允許的 IP: {}", allowedIps);
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String path = httpRequest.getRequestURI();
        
        // 檢查是否為測試端點
        if (path.startsWith("/auth/test-")) {
            // 如果測試端點被禁用，直接返回 404
            if (!enableTestEndpoints) {
                log.debug("測試端點已禁用，拒絕訪問: {}", path);
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
                httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
                httpResponse.setCharacterEncoding("UTF-8");
                
                Map<String, Object> errorDetails = new HashMap<>();
                errorDetails.put("status", HttpServletResponse.SC_NOT_FOUND);
                errorDetails.put("message", "找不到請求的資源");
                errorDetails.put("timestamp", LocalDateTime.now().toString());
                
                ObjectMapper objectMapper = new ObjectMapper();
                httpResponse.getWriter().write(objectMapper.writeValueAsString(errorDetails));
                return;
            }
            
            // 檢查 IP 是否在允許列表中
            String clientIp = request.getRemoteAddr();
            if (!allowedIps.contains(clientIp)) {
                log.warn("非授權 IP ({}) 嘗試訪問測試端點: {}", clientIp, path);
                HttpServletResponse httpResponse = (HttpServletResponse) response;
                httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                httpResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
                httpResponse.setCharacterEncoding("UTF-8");
                
                Map<String, Object> errorDetails = new HashMap<>();
                errorDetails.put("status", HttpServletResponse.SC_FORBIDDEN);
                errorDetails.put("message", "測試端點僅允許特定 IP 訪問");
                errorDetails.put("timestamp", LocalDateTime.now().toString());
                
                ObjectMapper objectMapper = new ObjectMapper();
                httpResponse.getWriter().write(objectMapper.writeValueAsString(errorDetails));
                return;
            }
            
            log.info("允許 IP ({}) 訪問測試端點: {}", clientIp, path);
        }
        
        chain.doFilter(request, response);
    }
}
