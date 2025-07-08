package com.jeannychiu.learningnotesapi.controller;

import com.jeannychiu.learningnotesapi.dto.LoginRequest;
import com.jeannychiu.learningnotesapi.dto.RegisterRequest;
import com.jeannychiu.learningnotesapi.dto.UserResponse;
import com.jeannychiu.learningnotesapi.security.JwtUtil;
import com.jeannychiu.learningnotesapi.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    
    private final AuthService authService;
    private final JwtUtil jwtUtil;
    
    @Value("${app.enable-test-endpoints:false}")
    private boolean enableTestEndpoints;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/register")
    public UserResponse register(@RequestBody @Valid RegisterRequest request){
        return authService.register(request);
    }

    @PostMapping("/login")
    public UserResponse login(@RequestBody @Valid LoginRequest request){
        return authService.login(request);
    }

    @GetMapping("/test-jwt")
    public ResponseEntity<String> testJwt(HttpServletRequest request){
        if (!enableTestEndpoints) {
            return ResponseEntity.notFound().build();
        }
        
        log.warn("安全警告: 測試端點 /test-jwt 被呼叫，IP: {}", request.getRemoteAddr());
        return ResponseEntity.ok(jwtUtil.generateToken("test@example.com", "USER"));
    }

    @GetMapping("/test-parse-jwt")
    public ResponseEntity<String> testParseJwt(@RequestParam String token, HttpServletRequest request){
        if (!enableTestEndpoints) {
            return ResponseEntity.notFound().build();
        }
        
        log.warn("安全警告: 測試端點 /test-parse-jwt 被呼叫，IP: {}", request.getRemoteAddr());
        return ResponseEntity.ok(jwtUtil.getEmailFromToken(token));
    }

    @GetMapping("/test-validate-jwt")
    public ResponseEntity<Boolean> testValidateJwt(
            @RequestParam String token, 
            @RequestParam String email, 
            HttpServletRequest request){
        if (!enableTestEndpoints) {
            return ResponseEntity.notFound().build();
        }
        
        log.warn("安全警告: 測試端點 /test-validate-jwt 被呼叫，IP: {}", request.getRemoteAddr());
        return ResponseEntity.ok(jwtUtil.validateToken(token, email));
    }
}
