package com.jeannychiu.learningnotesapi.service;

import com.jeannychiu.learningnotesapi.dto.LoginRequest;
import com.jeannychiu.learningnotesapi.dto.RegisterRequest;
import com.jeannychiu.learningnotesapi.dto.UserResponse;
import com.jeannychiu.learningnotesapi.exception.InvalidCredentialsException;
import com.jeannychiu.learningnotesapi.exception.InvalidPasswordException;
import com.jeannychiu.learningnotesapi.exception.UserAlreadyExistsException;
import com.jeannychiu.learningnotesapi.model.User;
import com.jeannychiu.learningnotesapi.repository.UserRepository;
import com.jeannychiu.learningnotesapi.security.JwtUtil;
import com.jeannychiu.learningnotesapi.validator.PasswordValidator;

import java.util.List;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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

        // 轉換成 UserResponse 回傳
        UserResponse userResponse = new UserResponse();
        userResponse.setId(savedUser.getId());
        userResponse.setEmail(savedUser.getEmail());
        userResponse.setRole(savedUser.getRole());
        return userResponse;
    }

    public UserResponse login(LoginRequest request){
        // 檢查 email 是否已存在
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("帳號或密碼錯誤");
        }

        // 登入成功，回傳 UserResponse
        UserResponse userResponse = new UserResponse();
        userResponse.setId(user.getId());
        userResponse.setEmail(user.getEmail());
        userResponse.setRole(user.getRole());
        userResponse.setToken(jwtUtil.generateToken((user.getEmail()), user.getRole()));
        return userResponse;
    }
}
