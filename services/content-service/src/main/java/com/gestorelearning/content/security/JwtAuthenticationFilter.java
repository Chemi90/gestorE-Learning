package com.gestorelearning.content.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/health") || path.equals("/info");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        System.out.println("[DEBUG-AUTH] Request: " + method + " " + path);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("[DEBUG-AUTH] No Bearer token found for path: " + path);
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);
        System.out.println("[DEBUG-AUTH] Token received: " + token.substring(0, Math.min(token.length(), 15)) + "...");

        try {
            Claims claims = jwtService.parseClaims(token);
            String email = claims.getSubject();
            String role = claims.get("role", String.class);
            String organizationId = claims.get("organizationId", String.class);

            System.out.println("[DEBUG-AUTH] Claims - Email: " + email + ", Role: " + role + ", OrgId: " + organizationId);

            if (email != null && role != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        email,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role))
                );
                if (organizationId != null) {
                    authentication.setDetails(java.util.Map.of("organizationId", organizationId));
                }
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("[DEBUG-AUTH] Authentication set for " + email + " with ROLE_" + role);
            }
        } catch (Exception ex) {
            System.err.println("[DEBUG-AUTH] JWT Validation FAILED: " + ex.getMessage());
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"message\":\"Invalid JWT\", \"error\":\"" + ex.getMessage() + "\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
