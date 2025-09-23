package com.fleetmanagement.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT Authentication Entry Point
 * Handles authentication failures and returns proper HTTP 401 responses
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    
    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response, 
                        AuthenticationException authException) throws IOException {
        
//        log.error("Unauthorized access attempt: {} - {}",
//            request.getRequestURI(), authException.getMessage());

        log.error("Unauthorized access attempt to {}: ", request.getRequestURI(), authException);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonResponse = String.format(
            "{\"error\": \"Unauthorized\", \"message\": \"%s\", \"path\": \"%s\", \"timestamp\": %d}",
            "Authentication required to access this resource",
            request.getRequestURI(),
            System.currentTimeMillis()
        );
        
        response.getWriter().write(jsonResponse);
    }
}