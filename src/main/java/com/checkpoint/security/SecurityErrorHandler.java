package com.checkpoint.security;

import com.checkpoint.common.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Handles Spring Security errors that never reach GlobalExceptionHandler:
 *
 *  - AuthenticationEntryPoint → 401  missing or invalid JWT
 *  - AccessDeniedHandler      → 403  valid JWT but insufficient permissions
 */
@Component
@RequiredArgsConstructor
public class SecurityErrorHandler
        implements AuthenticationEntryPoint, AccessDeniedHandler {

    // Spring Boot auto-configures this — already handles LocalDateTime,
    // JavaTimeModule, and your @JsonInclude(NON_NULL) settings
    private final ObjectMapper objectMapper;

    // 401 — no token, expired token, bad signature
    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException ex) throws IOException {
        // Check if there's a detailed JWT exception stored by JwtFilter
        JwtException jwtEx = (JwtException) request.getAttribute(JwtFilter.JWT_EXCEPTION_ATTR);

        String message;
        if (jwtEx != null) {
            // Use the detailed JWT error message
            message = jwtEx.getMessage();
        } else {
            // Fallback to generic message
            message = "Authentication required";
        }

        write(response, HttpServletResponse.SC_UNAUTHORIZED, message);
    }

    // 403 — authenticated but not authorised
    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException ex) throws IOException {
        write(response, HttpServletResponse.SC_FORBIDDEN, "Access denied");
    }

    private void write(HttpServletResponse response, int status, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                objectMapper.writeValueAsString(ApiResponse.error(message))
        );
    }
}