package com.backend.handicrafts.service;

import com.backend.handicrafts.dto.LoginRequest;
import com.backend.handicrafts.dto.RegisterRequest;
import com.backend.handicrafts.entity.Role;
import com.backend.handicrafts.entity.User;
import com.backend.handicrafts.exception.BadRequestException;
import com.backend.handicrafts.repository.UserRepository;
import com.backend.handicrafts.security.JwtService;
import com.backend.handicrafts.security.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TokenBlacklistService tokenBlacklistService;

    public Map<String, Object> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();

        User saved = userRepository.save(user);
        log.info("DATA INSERTED SUCCESSFULLY: User{{id={}, email={}}}",
            saved.getId(), saved.getEmail());

        Map<String, Object> data = new HashMap<>();
        data.put("id", saved.getId());
        data.put("name", saved.getName());
        data.put("email", saved.getEmail());
        data.put("role", saved.getRole());
        data.put("token", jwtService.generateToken(saved.getEmail(), saved.getRole().name()));
        return data;
    }

    public Map<String, Object> login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid email or password");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("role", user.getRole());
        data.put("token", jwtService.generateToken(user.getEmail(), user.getRole().name()));
        return data;
    }

    public Map<String, Object> getCurrentUser(Authentication authentication) {
        if (authentication == null || authentication.getName() == null || authentication.getName().isBlank()) {
            throw new BadRequestException("Not authenticated");
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new BadRequestException("User not found"));

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("role", user.getRole());
        return data;
    }

    public void logout(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return;
        }

        String token = authorizationHeader.substring(7);
        if (!jwtService.isTokenValid(token)) {
            return;
        }

        Date expiration = jwtService.extractExpiration(token);
        long expiresAt = expiration != null ? expiration.getTime() : System.currentTimeMillis();
        tokenBlacklistService.blacklistToken(token, expiresAt);
    }
}
