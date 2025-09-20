package com.jeannychiu.learningnotesapi.security;

import com.jeannychiu.learningnotesapi.dto.UserResponse;
import com.jeannychiu.learningnotesapi.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {
    private static final Logger log = LoggerFactory.getLogger(GoogleOAuth2SuccessHandler.class);

    private final AuthService authService;

    private static final String FRONTEND_BASE_URL = "https://learning-notes-app-926274779343.asia-east1.run.app";

    public GoogleOAuth2SuccessHandler(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String sub = principal.getAttribute("sub");
        String email = principal.getAttribute("email");

        // 綁定/建立使用者並產出 JWT
        UserResponse userResponse = authService.loginByGoogle(sub, email);
        String token = userResponse.getToken();

        String redirectUrl = FRONTEND_BASE_URL + "/callback.html#?token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);

        log.info("Google OAuth2 login success for email={} sub={}, redirect to {}", email, sub, redirectUrl);

        response.setStatus(HttpServletResponse.SC_FOUND);
        response.setHeader("Location", redirectUrl);
    }
}
