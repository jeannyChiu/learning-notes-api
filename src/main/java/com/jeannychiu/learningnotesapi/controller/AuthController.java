package com.jeannychiu.learningnotesapi.controller;

import com.jeannychiu.learningnotesapi.dto.LoginRequest;
import com.jeannychiu.learningnotesapi.dto.RegisterRequest;
import com.jeannychiu.learningnotesapi.dto.UserResponse;
import com.jeannychiu.learningnotesapi.security.JwtUtil;
import com.jeannychiu.learningnotesapi.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;

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
    public String testJwt(){
        return jwtUtil.generateToken("test@example.com");
    }

    @GetMapping("/test-parse-jwt")
    public String testParseJwt(@RequestParam String token){
        return jwtUtil.getEmailFromToken(token);
    }

    @GetMapping("/test-validate-jwt")
    public boolean testValidateJwt(@RequestParam String token, @RequestParam String email){
        return jwtUtil.validateToken(token, email);
    }
}
