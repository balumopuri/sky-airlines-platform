package com.airline.bakend.controller;

import com.airline.bakend.dto.Dtos.*;
import com.airline.bakend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/api/auth/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/api/auth/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @GetMapping("/api/users/me")
    public UserDto me(Authentication auth) {
        return authService.me(auth.getName());
    }

    @PutMapping("/api/users/me")
    public UserDto updateMe(@Valid @RequestBody ProfileRequest req, Authentication auth) {
        return authService.updateMe(auth.getName(), req);
    }
}
