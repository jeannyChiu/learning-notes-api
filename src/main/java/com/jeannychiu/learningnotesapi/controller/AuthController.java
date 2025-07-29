package com.jeannychiu.learningnotesapi.controller;

import com.jeannychiu.learningnotesapi.dto.LoginRequest;
import com.jeannychiu.learningnotesapi.dto.RegisterRequest;
import com.jeannychiu.learningnotesapi.dto.UserResponse;
import com.jeannychiu.learningnotesapi.exception.InvalidCredentialsException;
import com.jeannychiu.learningnotesapi.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 使用者驗證控制器
 *
 * 處理使用者註冊、登入和取得使用者資訊等認證相關的
 REST API 端點。
 * 所有端點都會透過 AOP 自動記錄請求和回應日誌。
 *
 * @author Jeanny Chiu
 * @since 1.0.0
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 使用者註冊
     *
     * 建立新的使用者帳號，預設角色為 USER。
     * 會檢查信箱是否已被註冊以及密碼強度要求。
     *
     * @param request 註冊請求資料，包含信箱和密碼
     * @return 使用者回應資料，包含 JWT token
     */
    @PostMapping("/register")
    public UserResponse register(@RequestBody @Valid RegisterRequest request){
        return authService.register(request);
    }

    /**
     * 使用者登入
     *
     * 驗證使用者信箱和密碼，成功後回傳 JWT token。
     *
     * @param request 登入請求資料，包含信箱和密碼
     * @return 使用者回應資料，包含 JWT token
     * @throws InvalidCredentialsException 當信箱或密碼錯誤時
     */
    @PostMapping("/login")
    public UserResponse login(@RequestBody @Valid LoginRequest request){
        return authService.login(request);
    }

    /**
     * 取得當前登入使用者資訊
     *
     * 需要有效的 JWT token，可供一般使用者和管理員使用。
     *
     * @param authentication Spring Security 的認證物件，包含當前使用者資訊
     * @return 使用者回應資料（不包含 token）
     */
    @GetMapping("/me")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public UserResponse getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        return authService.getUserByEmail(email);
    }
}
