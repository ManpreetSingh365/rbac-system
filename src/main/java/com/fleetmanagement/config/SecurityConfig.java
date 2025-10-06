package com.fleetmanagement.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fleetmanagement.security.JwtAuthenticationEntryPoint;
import com.fleetmanagement.security.JwtAuthenticationFilter;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security Configuration
 * Integrates CORS with JWT authentication for cookie and header support
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // ✅ Enable CORS with our configuration - MUST come first
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ✅ Disable CSRF for REST API with JWT
                .csrf(csrf -> csrf.disable())

                // ✅ Set session management to stateless (JWT-based)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // ✅ Configure authentication entry point
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint(jwtAuthenticationEntryPoint))

                // ✅ Set permissions on endpoints
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/devices/register/sms").permitAll() // SMS device registration
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/favicon.ico")
                        .permitAll()

                        // Protected endpoints - require authentication
                        .requestMatchers("/api/v1/admin/**").hasAuthority("SUPER_ADMIN")
                        .requestMatchers("/api/v1/users/**")
                        .hasAnyAuthority("SUPER_ADMIN", "USER_READ", "USER_CREATE", "USER_UPDATE", "USER_DELETE")
                        .requestMatchers("/api/v1/devices/**")
                        .hasAnyAuthority("SUPER_ADMIN", "DEVICE_READ", "DEVICE_REGISTER", "DEVICE_ASSIGN")
                        .requestMatchers("/api/v1/vehicles/**")
                        .hasAnyAuthority("SUPER_ADMIN", "VEHICLE_READ", "VEHICLE_CREATE", "VEHICLE_UPDATE")
                        .requestMatchers("/api/v1/permissions/**")
                        .hasAnyAuthority("SUPER_ADMIN", "PERMISSION_READ", "PERMISSION_CREATE", "PERMISSION_UPDATE")

                        // All other requests require authentication
                        .anyRequest().authenticated())

                // ✅ Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ Allow specific origins with patterns for flexibility
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*", // Any localhost port
                "http://127.0.0.1:*" // Any 127.0.0.1 port
        ));

        // ✅ Allow all standard HTTP methods including OPTIONS for preflight
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));

        // ✅ Allow all headers
        configuration.setAllowedHeaders(Arrays.asList("*"));

        // ✅ Expose important headers to frontend (critical for cookies)
        configuration.setExposedHeaders(Arrays.asList(
                "Set-Cookie",
                "Authorization",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"));

        // ✅ CRITICAL: Enable credentials for cookie support
        configuration.setAllowCredentials(true);

        // ✅ Cache preflight requests for 1 hour to reduce OPTIONS requests
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}