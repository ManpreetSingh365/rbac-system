package com.fleetmanagement.config;

import com.fleetmanagement.security.JwtAuthenticationEntryPoint;
import com.fleetmanagement.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration for Fleet Management RBAC System
 * Implements JWT-based authentication with method-level security
 * Optimized for high scalability and performance
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    /**
     * Configure security filter chain
     * Implements stateless JWT authentication with RBAC
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless API
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configure session management
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Configure authorization rules
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/devices/register/sms").permitAll() // SMS device registration
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                
                // Protected endpoints - require authentication
                .requestMatchers("/api/v1/admin/**").hasAuthority("SUPER_ADMIN")
                .requestMatchers("/api/v1/users/**").hasAnyAuthority("SUPER_ADMIN", "USER_READ", "USER_CREATE", "USER_UPDATE", "USER_DELETE")
                .requestMatchers("/api/v1/roles/**").hasAnyAuthority("SUPER_ADMIN", "ROLE_CREATE", "ROLE_READ", "ROLE_UPDATE", "ROLE_DELETE")
                .requestMatchers("/api/v1/devices/**").hasAnyAuthority("SUPER_ADMIN", "DEVICE_READ", "DEVICE_REGISTER", "DEVICE_ASSIGN")
                .requestMatchers("/api/v1/vehicles/**").hasAnyAuthority("SUPER_ADMIN", "VEHICLE_READ", "VEHICLE_CREATE", "VEHICLE_UPDATE")
                
                // All other requests require authentication
                .anyRequest().authenticated())
            
            // Configure exception handling
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint))
            
            // Add JWT filter
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * Authentication Manager Bean
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}