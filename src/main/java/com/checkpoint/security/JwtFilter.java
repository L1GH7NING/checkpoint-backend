package com.checkpoint.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // Constant for storing JWT exception in request
    public static final String JWT_EXCEPTION_ATTR = "jwt_exception";

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            try {
                jwtUtil.validateAndParseClaims(token);

                UUID userId   = jwtUtil.extractUserId(token);
                String username = jwtUtil.extractUsername(token);

                // Build principal from JWT claims — zero DB hits on authenticated requests
                UserPrincipal principal = new UserPrincipal(userId, username);
                var auth = new UsernamePasswordAuthenticationToken(
                        principal, null, principal.getAuthorities()
                );
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (JwtException ex) {
                // Store JWT exception in request for SecurityErrorHandler to access
                request.setAttribute(JWT_EXCEPTION_ATTR, ex);
                log.debug("JWT validation failed: {}", ex.getMessage());
            }
        }

        chain.doFilter(request, response);
    }
}