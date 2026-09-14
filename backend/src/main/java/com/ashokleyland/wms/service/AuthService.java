package com.ashokleyland.wms.service;

import com.ashokleyland.wms.dto.LoginRequest;
import com.ashokleyland.wms.dto.LoginResponse;
import com.ashokleyland.wms.dto.UserDto;
import com.ashokleyland.wms.model.User;
import com.ashokleyland.wms.repository.UserRepository;
import com.ashokleyland.wms.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadCredentialsException("Account disabled");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username or password");
        }

        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getUserId(), user.getRole().name());

        return LoginResponse.builder()
                .accessToken(token)
                .tokenType("bearer")
                .user(UserDto.builder()
                        .userId(user.getUserId())
                        .username(user.getUsername())
                        .fullName(user.getFullName())
                        .role(user.getRole().name())
                        .badgeId(user.getBadgeId())
                        .build())
                .build();
    }
}
