package com.jeannychiu.learningnotesapi.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannychiu.learningnotesapi.exception.InvalidTokenException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        // 1. 取得 Authorization header
        String authHeader = request.getHeader("Authorization");

        // 2. 檢查 JWT 是否存在且格式正確
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            try {
                // 3. 先嘗試解析 token (這會檢查 token 是否有效)
                String email = jwtUtil.getEmailFromToken(token);
                String role = jwtUtil.getRoleFromToken(token);
                
                // 4. 再檢查 token 是否過期
                if (jwtUtil.isTokenExpired(token)) {
                    SecurityContextHolder.clearContext();
                    logger.warn("Token 已過期");
                    sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token 已過期，請重新登入");
                    return;
                }
                
                // 5. 如果 token 有效且未過期，進行驗證
                if (jwtUtil.validateToken(token, email)) {
                    // 6. 建立 Authentication 物件並存入 SecurityContext
                    List<SimpleGrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    SecurityContextHolder.clearContext();
                    logger.warn("Token 驗證失敗");
                    sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token 驗證失敗，請重新登入");
                    return;
                }
            } catch (InvalidTokenException e) {
                // 當 token 處理（解析、驗證）失敗時，清除 SecurityContext
                SecurityContextHolder.clearContext();
                logger.error("Token 處理異常: {}", e.getMessage());
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "無效的 Token: " + e.getMessage());
                return;
            } catch (Exception e) {
                // 其他未預期的異常
                SecurityContextHolder.clearContext();
                logger.error("Token 處理發生未預期錯誤: {}", e.getMessage());
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "認證處理發生錯誤，請重新登入");
                return;
            }
        }

        // 7. 繼續 filter chain
        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", status.value());
        errorDetails.put("message", message);
        errorDetails.put("timestamp", LocalDateTime.now().toString());
        
        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }
}


