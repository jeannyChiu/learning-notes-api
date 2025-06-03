package com.jeannychiu.learningnotesapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

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
            authHeader = authHeader.substring(7);

            // 3. 驗證 JWT
            String email = jwtUtil.getEmailFromToken(authHeader);
            if (email != null && jwtUtil.validateToken(authHeader, email)) {
                // 4. 建立 Authentication 物件並存入 SecurityContext
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(email, null, null);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        // 5. 呼叫 filterChain.doFilter(request, response)
        filterChain.doFilter(request, response);
    }
}
