package com.jeannychiu.learningnotesapi.controller;

import com.jeannychiu.learningnotesapi.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 測試端點控制器
 *
 * 提供 JWT 相關的測試端點，僅在開發和測試環境中啟用。
 * 透過設定 app.enable-test-endpoints=true 來啟用這些端點。
 *
 * 安全注意事項：這些端點不應在正式環境中啟用。
 *
 * @author Jeanny Chiu
 * @since 1.0.0
 */
@RestController
@RequestMapping("/test")
@ConditionalOnProperty(name = "app.enable-test-endpoints", havingValue = "true")
public class TestController {
    private static final Logger log = LoggerFactory.getLogger(TestController.class);

    private final JwtUtil jwtUtil;

    public TestController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 產生測試用的 JWT token
     *
     * 產生一個測試用的 JWT token，使用固定的測試信箱。
     *
     * @param request HTTP 請求物件，用於記錄請求來源 IP
     * @return 測試用的 JWT token
     */
    @GetMapping("/jwt")
    public ResponseEntity<String> testJwt(HttpServletRequest request) {
        log.warn("安全警告: 測試端點 /test/jwt 被呼叫，IP: {}", request.getRemoteAddr());
        return ResponseEntity.ok(jwtUtil.generateToken("test@example.com", "USER"));
    }

    /**
     * 解析 JWT token
     *
     * 從 JWT token 中解析出使用者信箱。
     *
     * @param token 要解析的 JWT token
     * @param request HTTP 請求物件，用於記錄請求來源 IP
     * @return 從 token 中解析出的信箱
     */
    @GetMapping("/parse-jwt")
    public ResponseEntity<String> testParseJwt(@RequestParam String token, HttpServletRequest
            request) {
        log.warn("安全警告: 測試端點 /test/parse-jwt 被呼叫，IP: {}",
                request.getRemoteAddr());
        return ResponseEntity.ok(jwtUtil.getEmailFromToken(token));
    }

    /**
     * 驗證 JWT token
     *
     * 驗證 JWT token 是否有效且屬於指定的使用者。
     *
     * @param token 要驗證的 JWT token
     * @param email 要驗證的使用者信箱
     * @param request HTTP 請求物件，用於記錄請求來源 IP
     * @return token 是否有效
     */
    @GetMapping("/validate-jwt")
    public ResponseEntity<Boolean> testValidateJwt(
            @RequestParam String token,
            @RequestParam String email,
            HttpServletRequest request) {
        log.warn("安全警告: 測試端點 /test/validate-jwt 被呼叫，IP: {}",
                request.getRemoteAddr());
        return ResponseEntity.ok(jwtUtil.validateToken(token, email));
    }
}