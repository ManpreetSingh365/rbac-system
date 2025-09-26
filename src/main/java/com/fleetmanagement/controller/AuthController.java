package com.fleetmanagement.controller;

import com.fleetmanagement.dto.request.LoginRequest;
import com.fleetmanagement.dto.response.UserLoginResponse;
import com.fleetmanagement.service.JwtService;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;

    @Autowired
    private final JwtService jwtService;

   @PostMapping("/login")
public String login(@RequestBody LoginRequest request) {
    Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
    );

    UserLoginResponse user = (UserLoginResponse) authentication.getPrincipal();
    UUID userId = user.getId(); // get UUID from your custom UserDetails implementation

    return jwtService.generateToken(user, userId);
}

}
