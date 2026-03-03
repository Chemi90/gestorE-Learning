package com.gestorelearning.auth.service;

import com.gestorelearning.auth.domain.UserEntity;
import com.gestorelearning.auth.dto.AuthUserResponse;
import com.gestorelearning.auth.dto.LoginRequest;
import com.gestorelearning.auth.dto.LoginResponse;
import com.gestorelearning.auth.dto.RegisterRequest;
import com.gestorelearning.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthUserResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists");
        }

        UserEntity user = new UserEntity();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setActive(true);

        UserEntity savedUser = userRepository.save(user);
        return new AuthUserResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
    }

    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
        );

        if (!authentication.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        UserEntity user = findByEmail(normalizedEmail);
        String token = jwtService.generateToken(user.getEmail(), user.getRole());
        return new LoginResponse(token, user.getRole());
    }

    public AuthUserResponse me(String email) {
        UserEntity user = findByEmail(email);
        return new AuthUserResponse(user.getId(), user.getEmail(), user.getRole());
    }

    private UserEntity findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email)
                .filter(UserEntity::isActive)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
