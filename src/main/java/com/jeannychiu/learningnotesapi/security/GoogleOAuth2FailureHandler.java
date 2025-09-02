package com.jeannychiu.learningnotesapi.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class GoogleOAuth2FailureHandler implements AuthenticationFailureHandler {

    private static final Logger log = LoggerFactory.getLogger(GoogleOAuth2FailureHandler.class);

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException, ServletException {
        // 常見：state 不符、授權流程中斷、invalid_client、redirect_uri_mismatch…
        log.warn("Google OAuth2 login FAILED: uri={}, error={}, query={}",
                request.getRequestURI(),
                exception.getMessage(),
                request.getQueryString());
        
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "OAuth2 login failed");
    }
}
