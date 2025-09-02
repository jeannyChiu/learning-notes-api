package com.jeannychiu.learningnotesapi.config;

import com.jeannychiu.learningnotesapi.filter.TestEndpointFilter;
import com.jeannychiu.learningnotesapi.security.*;
import com.jeannychiu.learningnotesapi.service.ApiLogService;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtUtil jwtUtil, ApiLogService apiLogService) {
        return new JwtAuthenticationFilter(jwtUtil, apiLogService);
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins:*}") String allowedOrigins,
            @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}") String allowedMethods,
            @Value("${app.cors.allowed-headers:*}") String allowedHeaders,
            @Value("${app.cors.allow-credentials:false}") boolean allowCredentials,
            @Value("${app.cors.max-age:3600}") long maxAge) {
        
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        configuration.setAllowedMethods(Arrays.asList(allowedMethods.split(",")));
        configuration.setAllowedHeaders(Arrays.asList(allowedHeaders.split(",")));
        configuration.setAllowCredentials(allowCredentials);
        configuration.setMaxAge(maxAge);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint,
            CustomAccessDeniedHandler customAccessDeniedHandler,
            TestEndpointFilter testEndpointFilter,
            CorsConfigurationSource corsConfigurationSource,
            @Value("${app.enable-test-endpoints:false}") boolean enableTestEndpoints,
            GoogleOAuth2SuccessHandler googleOAuth2SuccessHandler,
            GoogleOAuth2FailureHandler googleOAuth2FailureHandler) throws Exception {
        
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
            .authorizeHttpRequests(auth -> {
                // 允許靜態資源訪問 - 更廣泛的配置
                auth.requestMatchers("/", "/index.html", "/app.js", "/static/**", "/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll();
                // 允許認證端點
                auth.requestMatchers("/auth/register", "/auth/login").permitAll();
                // 允許 Google OAuth2 端點
                auth.requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll();
                // 允許錯誤頁面
                auth.requestMatchers("/error").permitAll();

                // 允許 Swagger UI 和 API 文檔
                auth.requestMatchers(
                        "/swagger-ui/**",
                        "/swagger-ui.html",
                        "/v3/api-docs/**",
                        "/swagger-resources/**",
                        "/webjars/**"
                ).permitAll();
                
                // 測試端點的處理完全交給 TestEndpointFilter
                if (enableTestEndpoints) {
                    auth.requestMatchers("/auth/test-jwt", "/auth/test-parse-jwt", "/auth/test-validate-jwt").permitAll();
                }
                
                auth.anyRequest().authenticated();
            })
            .oauth2Login(oauth -> oauth.successHandler(googleOAuth2SuccessHandler).failureHandler(googleOAuth2FailureHandler))
            // 確保 TestEndpointFilter 在所有 Spring Security 過濾器之前執行
            .addFilterBefore(testEndpointFilter, SecurityContextHolderFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(exception -> exception
                    .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                    .accessDeniedHandler(customAccessDeniedHandler)
            );

        return http.build();
    }
}
