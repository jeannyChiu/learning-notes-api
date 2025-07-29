package com.jeannychiu.learningnotesapi.service;

import com.jeannychiu.learningnotesapi.dto.LoginRequest;
import com.jeannychiu.learningnotesapi.dto.RegisterRequest;
import com.jeannychiu.learningnotesapi.dto.UserResponse;
import com.jeannychiu.learningnotesapi.exception.InvalidCredentialsException;
import com.jeannychiu.learningnotesapi.exception.InvalidPasswordException;
import com.jeannychiu.learningnotesapi.exception.UserAlreadyExistsException;
import com.jeannychiu.learningnotesapi.exception.UserNotFoundException;
import com.jeannychiu.learningnotesapi.model.User;
import com.jeannychiu.learningnotesapi.repository.UserRepository;
import com.jeannychiu.learningnotesapi.security.JwtUtil;
import com.jeannychiu.learningnotesapi.validator.PasswordValidator;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 使用者認證服務層
 *
 * 處理使用者註冊、登入和認證相關的業務邏輯。
 * 負責密碼加密、JWT token 生成、使用者資訊管理等功能。
 *
 * @author Jeanny Chiu
 * @since 1.0.0
 */
@Service
public class AuthService {
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthService(BCryptPasswordEncoder passwordEncoder,
                       UserRepository userRepository,
                       JwtUtil jwtUtil) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 註冊新使用者
     *
     * 檢查信箱是否已被使用，加密密碼後儲存使用者資訊。
     * 新使用者預設角色為 USER。
     *
     * @param request 註冊請求，包含信箱和密碼
     * @return 包含 JWT token 的使用者回應
     * @throws UserAlreadyExistsException 當信箱已被註冊時
     * @throws InvalidPasswordException 當密碼不符合強度要求時
     */
    public UserResponse register(RegisterRequest request){
        // 檢查 email 是否已存在
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email 已經被註冊");
        }

        // 檢查密碼強度
        List<String> passwordErrors = PasswordValidator.validate(request.getPassword());
        if (!passwordErrors.isEmpty()) {
            throw new InvalidPasswordException("密碼不符合強度要求: " + String.join("; ", passwordErrors));
        }

        // 建立新用戶
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER"); // 預設新註冊為 USER

        // 存入資料庫
        User savedUser = userRepository.save(user);

        // 轉換成 UserResponse 回傳，包含 JWT token
        return createUserResponse(savedUser, true);
    }

    /**
     * 使用者登入
     *
     * 驗證信箱和密碼，成功後生成 JWT token
     *
     * @param request 登入請求，包含信箱和密碼
     * @return 包含 JWT token 的使用者回應
     * @throws InvalidCredentialsException 當帳號或密碼錯誤時
     */
    public UserResponse login(LoginRequest request){
        // 檢查 email 是否已存在
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("帳號或密碼錯誤");
        }

        // 登入成功，回傳 UserResponse
        return createUserResponse(user, true);
    }

    /**
     * 根據信箱取得使用者資訊
     *
     * 不回傳 JWT token，因為呼叫此方法時使用者已有有效的token。
     *
     * @param email 使用者信箱
     * @return 使用者回應 (不包含 token)
     * @throws UserNotFoundException 當使用者不存在時
     */
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("使用者不存在"));

        // 不包含 token，因為用戶已經有有效的 token
        return createUserResponse(user, false);
    }

    /**
     * 將使用者實體轉換為回應物件 (包含 JWT token)
     * @param user 使用者實體
     * @param includeToken 是否包含 token
     * @return 使用者回應物件
     */
    private UserResponse createUserResponse(User user, boolean includeToken) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());

        if (includeToken) {
            response.setToken(jwtUtil.generateToken(user.getEmail(), user.getRole()));
        }

        return response;
    }
}
