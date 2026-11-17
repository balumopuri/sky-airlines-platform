package com.airline.bakend.service;

import com.airline.bakend.dto.Dtos.*;
import com.airline.bakend.entity.Role;
import com.airline.bakend.entity.User;
import com.airline.bakend.exception.ApiException;
import com.airline.bakend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final Mapper mapper;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtService jwtService, Mapper mapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.mapper = mapper;
    }

    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (email.contains("\"") || email.contains("\\")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid email address");
        }
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "This email is already registered");
        }
        User u = new User();
        u.setFullName(req.fullName().trim());
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setPhone(req.phone());
        u.setRole(Role.CUSTOMER);
        userRepository.save(u);
        return response(u);
    }

    public AuthResponse login(LoginRequest req) {
        User u = userRepository.findByEmail(req.email().trim().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!passwordEncoder.matches(req.password(), u.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return response(u);
    }

    public UserDto me(String email) {
        return mapper.toUserDto(find(email));
    }

    public UserDto updateMe(String email, ProfileRequest req) {
        User u = find(email);
        u.setFullName(req.fullName().trim());
        u.setPhone(req.phone());
        return mapper.toUserDto(userRepository.save(u));
    }

    private User find(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private AuthResponse response(User u) {
        return new AuthResponse(jwtService.generate(u.getEmail(), u.getRole().name()),
                u.getRole().name(), u.getFullName(), u.getEmail());
    }
}
