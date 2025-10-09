package com.fleetmanagement.controller;

import com.fleetmanagement.dto.request.LoginRequest;
import com.fleetmanagement.dto.response.UserLoginResponse;
import com.fleetmanagement.service.JwtService;
import lombok.RequiredArgsConstructor;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(),
                        request.getPassword()));

        UserLoginResponse user = (UserLoginResponse) authentication.getPrincipal();
        UUID userId = user.getId();

        String jwt = jwtService.generateToken(user, userId);

        boolean rememberMe = request.isRememberMe();
        long expirySeconds = rememberMe ? 7 * 24 * 60 * 60 : 24 * 60 * 60; // 7 days vs 1 day

        // Local --->
        ResponseCookie cookie = ResponseCookie.from("auth_token", jwt)
                .httpOnly(true)
                .secure(false) // disable secure for localhost
                .sameSite("Lax") // allow cross-origin POST if needed
                .path("/")
                .maxAge(expirySeconds)
                .build();

        System.out.println("token: " + jwt);

        // Production --->
        // ResponseCookie cookie = ResponseCookie.from("auth_token", jwt).httpOnly(true).secure(true).sameSite("Strict").path("/").maxAge(expirySeconds).build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Login successful"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cookie = ResponseCookie.from("auth_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(Map.of("message", "Logged out successfully"));
    }

    // * Mobile Side--->
    // @PostMapping("/login")
    // public UserLoginResponseDto login(@RequestBody LoginRequest request) {
    // Authentication authentication = authenticationManager.authenticate(
    // new UsernamePasswordAuthenticationToken(request.getUsername(),
    // request.getPassword()));

    // UserLoginResponse user = (UserLoginResponse) authentication.getPrincipal();
    // UUID userId = user.getId();

    // String jwt = jwtService.generateToken(user, userId);

    // // Return JSON object with token
    // return new UserLoginResponseDto(jwt);
    // }
}