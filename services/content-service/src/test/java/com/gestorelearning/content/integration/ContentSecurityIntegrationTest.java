package com.gestorelearning.content.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class ContentSecurityIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withInitScript("init-test.sql");

    private static final String SECRET = "01234567890123456789012345678901";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void pingWithoutJwtReturns401() throws Exception {
        mockMvc.perform(get("/api/v1/ping"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void coursesGetWithStudentRoleReturns404() throws Exception {
        String token = createToken("STUDENT");
        String dummyUuid = "123e4567-e89b-12d3-a456-426614174000";

        // Un STUDENT tiene permiso para leer, así que llegará al controlador y dará 404 (porque no existe)
        mockMvc.perform(get("/api/v1/courses/" + dummyUuid)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void coursesPostWithStudentRoleReturns403() throws Exception {
        String token = createToken("STUDENT");

        // Un STUDENT NO tiene permiso para crear (POST), así que dará 403 Forbidden
        mockMvc.perform(post("/api/v1/courses/bulk")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    private static String createToken(String role) {
        SecretKey signingKey = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();

        return Jwts.builder()
                .subject("user@example.com")
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(60, ChronoUnit.MINUTES)))
                .signWith(signingKey)
                .compact();
    }
}
