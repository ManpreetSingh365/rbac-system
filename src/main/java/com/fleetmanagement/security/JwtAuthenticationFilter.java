package com.fleetmanagement.security;

import com.fleetmanagement.service.JwtService;
import com.fleetmanagement.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * JWT Authentication Filter
 * Processes JWT tokens from request headers and sets up security context
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            String jwt = extractJwtFromRequest(request);
            
            if (jwt != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UUID userId = jwtService.extractUserId(jwt); // sub = UUID
                
                if (userId != null) {        
                    UserDetails userDetails = userDetailsService.loadUserById(userId);
                    
                    if (jwtService.isTokenValid(jwt, userDetails, userId)) {
                        UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                            );
                        
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        SecurityContextHolder.getContext().setAuthentication(authToken);

                        log.debug("✅ Successfully authenticated user: {}", userId);
                    } else {
                        log.warn("❌ Invalid JWT for user: {}", userId);
                    }
                }
            }


        } catch (Exception e) {
            log.error("Cannot set user authentication", e);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        
        return null;
    }
}