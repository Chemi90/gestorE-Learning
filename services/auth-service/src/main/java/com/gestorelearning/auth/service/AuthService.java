package com.gestorelearning.auth.service;

import com.gestorelearning.auth.domain.OrganizationEntity;
import com.gestorelearning.auth.domain.UserEntity;
import com.gestorelearning.auth.dto.AuthUserResponse;
import com.gestorelearning.auth.dto.LoginRequest;
import com.gestorelearning.auth.dto.LoginResponse;
import com.gestorelearning.auth.dto.OrganizationResponse;
import com.gestorelearning.auth.dto.RegisterRequest;
import com.gestorelearning.auth.repository.OrganizationRepository;
import com.gestorelearning.auth.repository.UserRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            OrganizationRepository organizationRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public List<OrganizationResponse> getAllOrganizations() {
        return organizationRepository.findAll().stream()
                .filter(OrganizationEntity::isActive)
                .map(org -> new OrganizationResponse(org.getId(), org.getName()))
                .toList();
    }

    public AuthUserResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        
        if (userRepository.existsByEmailIgnoreCaseAndOrganizationId(normalizedEmail, request.organizationId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already exists in this organization");
        }

        OrganizationEntity organization = organizationRepository.findById(request.organizationId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found"));

        UserEntity user = new UserEntity();
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setOrganization(organization);
        user.setActive(true);

        UserEntity savedUser = userRepository.save(user);
        return new AuthUserResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getRole());
    }

    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
            );
        } catch (AuthenticationException ex) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        UserEntity user = findByEmailAndOrganization(normalizedEmail, request.organizationId(), 
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found in this organization"));

        String token = jwtService.generateToken(user.getEmail(), user.getRole(), user.getOrganization().getId());
        return new LoginResponse(token, user.getRole());
    }

    public AuthUserResponse me(String email, UUID organizationId) {
        UserEntity user = findByEmailAndOrganization(email, organizationId, 
                new UsernameNotFoundException("User not found in this organization"));
        return new AuthUserResponse(user.getId(), user.getEmail(), user.getRole());
    }

    private UserEntity findByEmailAndOrganization(String email, UUID organizationId, RuntimeException exception) {
        return userRepository.findByEmailIgnoreCaseAndOrganizationId(email, organizationId)
                .filter(UserEntity::isActive)
                .orElseThrow(() -> exception);
    }
}
