package com.gestorelearning.content.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestorelearning.common.dto.CourseTreeResponse;
import com.gestorelearning.common.dto.ElementResponse;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class ResilientCourseIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withInitScript("init-test.sql");

    private static final String SECRET = "01234567890123456789012345678901";
    private static final String ORG_ID = "550e8400-e29b-41d4-a716-446655440000";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String teacherToken() {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return Jwts.builder()
                .subject("teacher@test.com")
                .claim("role", "TEACHER")
                .claim("organizationId", ORG_ID)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(60, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    @Test
    void testAtomicIncrementalHydration() throws Exception {
        // 1. Crear esqueleto (Curso + Modulo + Unidad con al menos un elemento planificado)
        String skeletonPayload = """
        {
            "title": "Skeleton Course",
            "description": "Initial structure",
            "level": "BEGINNER",
            "version": 1,
            "organizationId": "%s",
            "modules": [
                {
                    "title": "Module 1",
                    "summary": "Summary 1",
                    "orderIndex": 0,
                    "units": [
                        {
                            "title": "Unit 1.1",
                            "orderIndex": 0,
                            "elements": [
                                {
                                    "resourceType": "TEXT",
                                    "title": "Initial Element",
                                    "summary": "Instruction for later",
                                    "orderIndex": 0
                                }
                            ],
                            "objectives": []
                        }
                    ]
                }
            ]
        }
        """.formatted(ORG_ID);

        MvcResult result = mockMvc.perform(post("/api/v1/courses/bulk")
                        .header("Authorization", "Bearer " + teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skeletonPayload))
                .andExpect(status().isOk())
                .andReturn();

        CourseTreeResponse tree = objectMapper.readValue(result.getResponse().getContentAsString(), CourseTreeResponse.class);
        UUID unitId = tree.modules().get(0).units().get(0).id();

        // 2. Añadir Objetivo
        String objective1 = """
        {
            "description": "Objective 1",
            "orderIndex": 0
        }
        """;
        mockMvc.perform(post("/api/v1/units/" + unitId + "/objectives")
                        .header("Authorization", "Bearer " + teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objective1))
                .andExpect(status().isOk());

        // 3. Añadir Segundo Elemento (planificado individualmente)
        String element2 = """
        {
            "resourceType": "TEXT",
            "title": "Element 2",
            "summary": "Second instruction",
            "body": "",
            "orderIndex": 1
        }
        """;
        MvcResult el2Result = mockMvc.perform(post("/api/v1/units/" + unitId + "/elements")
                        .header("Authorization", "Bearer " + teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(element2))
                .andExpect(status().isOk())
                .andReturn();

        ElementResponse e2 = objectMapper.readValue(el2Result.getResponse().getContentAsString(), ElementResponse.class);

        // 4. Actualizar BODY del Elemento 2 (Simulando respuesta del LLM paso 2.2)
        String bodyUpdate = """
        {
            "body": "Detailed content from LLM"
        }
        """;
        mockMvc.perform(patch("/api/v1/elements/" + e2.id() + "/body")
                        .header("Authorization", "Bearer " + teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyUpdate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body").value("Detailed content from LLM"))
                .andExpect(jsonPath("$.status").value("COMPLETED"));

        // 5. Verificar arbol completo
        mockMvc.perform(get("/api/v1/courses/" + tree.courseId() + "/tree")
                        .header("Authorization", "Bearer " + teacherToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modules[0].units[0].elements[1].body").value("Detailed content from LLM"))
                .andExpect(jsonPath("$.modules[0].units[0].elements[1].status").value("COMPLETED"));
    }
}
