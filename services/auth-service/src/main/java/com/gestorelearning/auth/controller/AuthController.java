package com.gestorelearning.auth.controller;

import com.gestorelearning.auth.dto.AuthUserResponse;
import com.gestorelearning.auth.dto.LoginRequest;
import com.gestorelearning.auth.dto.LoginResponse;
import com.gestorelearning.auth.dto.OrganizationResponse;
import com.gestorelearning.auth.dto.RegisterRequest;
import com.gestorelearning.auth.service.AuthService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/organizations")
    public List<OrganizationResponse> getAllOrganizations() {
        return authService.getAllOrganizations();
    }

    @PostMapping("/register")
    public AuthUserResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @SuppressWarnings("unchecked")
    public AuthUserResponse me(Authentication authentication) {
        Map<String, Object> details = (Map<String, Object>) authentication.getDetails();
        String orgIdStr = (String) details.get("organizationId");
        return authService.me(authentication.getName(), UUID.fromString(orgIdStr));
    }
}