package com.payflow.userservice.service;

import com.payflow.common.exception.ResourceNotFoundException;
import com.payflow.userservice.dto.*;
import com.payflow.userservice.entity.RefreshToken;
import com.payflow.userservice.entity.User;
import com.payflow.userservice.repository.RefreshTokenRepository;
import com.payflow.userservice.repository.UserRepository;
import com.payflow.userservice.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-token-expiry}")
    private long refreshTokenExpiry;

    @Transactional
    public UserProfileDto register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email()))
            throw new IllegalArgumentException("Email already in use");
        if (userRepository.existsByUsername(request.username()))
            throw new IllegalArgumentException("Username already taken");

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        User saved = userRepository.save(user);
        return new UserProfileDto(saved.getId(), saved.getUsername(), saved.getEmail());
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.password(), user.getPassword()))
            throw new IllegalArgumentException("Invalid credentials");

        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String refreshToken = generateAndSaveRefreshToken(user);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Transactional
    public AuthResponse refresh(String refreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now()))
            throw new IllegalArgumentException("Refresh token expired");

        User user = token.getUser();
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getEmail());
        String newRefreshToken = generateAndSaveRefreshToken(user);

        refreshTokenRepository.delete(token);
        return new AuthResponse(newAccessToken, newRefreshToken);
    }

    public UserProfileDto getProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return new UserProfileDto(user.getId(), user.getUsername(), user.getEmail());
    }

    private String generateAndSaveRefreshToken(User user) {
        refreshTokenRepository.deleteByUser_Id(user.getId());

        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiry / 1000));
        refreshTokenRepository.save(token);

        return token.getToken();
    }
}
