package com.gilberto.task_manager_api.service;

import com.gilberto.task_manager_api.dto.auth.AuthResponse;
import com.gilberto.task_manager_api.dto.auth.LoginRequest;
import com.gilberto.task_manager_api.dto.auth.RegisterRequest;
import com.gilberto.task_manager_api.model.User;
import com.gilberto.task_manager_api.model.enums.UserRole;
import com.gilberto.task_manager_api.repository.UserRepository;
import com.gilberto.task_manager_api.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = User.builder()
                .nome(request.getNome())
                .email(request.getEmail())
                .senha(passwordEncoder.encode(request.getSenha()))
                .role(UserRole.USER)
                .build();

        User saved = userRepository.save(user);
        UserDetails userDetails = toUserDetails(saved);
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .expiresIn(jwtService.getExpirationMillis())
                .build();
    }

    public AuthResponse authenticate(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getSenha())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        UserDetails userDetails = toUserDetails(user);
        String token = jwtService.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .expiresIn(jwtService.getExpirationMillis())
                .build();
    }

    private UserDetails toUserDetails(User user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getSenha())
                .authorities("ROLE_" + user.getRole().name())
                .build();
    }
}

