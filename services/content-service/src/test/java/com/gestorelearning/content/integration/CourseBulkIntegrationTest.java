package com.gestorelearning.content.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class CourseBulkIntegrationTest {

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

    // --- helpers ---

    private String teacherToken() {
        return createToken("TEACHER", ORG_ID);
    }

    private static String createToken(String role, String orgId) {
        SecretKey key = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return Jwts.builder()
                .subject("teacher@test.com")
                .claim("role", role)
                .claim("organizationId", orgId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(60, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    private static String bulkPayload(String orgId) {
        return """
        {
            "title": "Curso de Integracion",
            "description": "Descripcion de prueba",
            "level": "INTERMEDIATE",
            "version": 1,
            "organizationId": "%s",
            "modules": [
                {
                    "title": "Modulo 1",
                    "summary": "Resumen del modulo",
                    "orderIndex": 0,
                    "units": [
                        {
                            "title": "Unidad 1",
                            "orderIndex": 0,
                            "elements": [
                                {
                                    "resourceType": "TEXT",
                                    "title": "Introduccion al tema",
                                    "body": "Contenido de la unidad...",
                                    "orderIndex": 0
                                }
                            ],
                            "objectives": [
                                { "description": "Objetivo 1", "orderIndex": 0 },
                                { "description": "Objetivo 2", "orderIndex": 1 }
                            ]
                        },
                        {
                            "title": "Unidad 2",
                            "orderIndex": 1,
                            "elements": [
                                {
                                    "resourceType": "QUIZ",
                                    "title": "Quiz de autoevaluacion",
                                    "body": null,
                                    "orderIndex": 0
                                }
                            ],
                            "objectives": []
                        }
                    ]
                }
            ]
        }
        """.formatted(orgId);
    }

    // --- tests ---

    @Test
    void createFullCourse_returnsTreeWithElementAndObjectives() throws Exception {
        mockMvc.perform(post("/api/v1/courses/bulk")
                        .header("Authorization", "Bearer " + teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bulkPayload(ORG_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Curso de Integracion"))
                .andExpect(jsonPath("$.modules[0].title").value("Modulo 1"))
                .andExpect(jsonPath("$.modules[0].units[0].title").value("Unidad 1"))
                .andExpect(jsonPath("$.modules[0].units[0].elements[0].resourceType").value("TEXT"))
                .andExpect(jsonPath("$.modules[0].units[0].elements[0].title").value("Introduccion al tema"))
                .andExpect(jsonPath("$.modules[0].units[0].objectives.length()").value(2))
                .andExpect(jsonPath("$.modules[0].units[0].objectives[0].description").value("Objetivo 1"))
                .andExpect(jsonPath("$.modules[0].units[1].elements[0].resourceType").value("QUIZ"))
                .andExpect(jsonPath("$.modules[0].units[1].objectives.length()").value(0));
    }

    @Test
    void getCourseTree_afterCreate_matchesBulkResponse() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/courses/bulk")
                        .header("Authorization", "Bearer " + teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bulkPayload(ORG_ID)))
                .andExpect(status().isOk())
                .andReturn();

        CourseTreeResponse created = objectMapper.readValue(
                result.getResponse().getContentAsString(), CourseTreeResponse.class);

        mockMvc.perform(get("/api/v1/courses/" + created.courseId() + "/tree")
                        .header("Authorization", "Bearer " + teacherToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.courseId").value(created.courseId().toString()))
                .andExpect(jsonPath("$.modules[0].units[0].objectives.length()").value(2));
    }

    @Test
    void updateCourseWithTree_replacesModulesAndElements() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/courses/bulk")
                        .header("Authorization", "Bearer " + teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bulkPayload(ORG_ID)))
                .andExpect(status().isOk())
                .andReturn();

        CourseTreeResponse created = objectMapper.readValue(
                result.getResponse().getContentAsString(), CourseTreeResponse.class);

        String updatedPayload = """
        {
            "title": "Curso Actualizado",
            "description": "Nueva descripcion",
            "level": "ADVANCED",
            "version": 2,
            "organizationId": "%s",
            "modules": [
                {
                    "title": "Modulo Nuevo",
                    "summary": null,
                    "orderIndex": 0,
                    "units": [
                        {
                            "title": "Unidad Nueva",
                            "orderIndex": 0,
                            "elements": [
                                {
                                    "resourceType": "VIDEO",
                                    "title": "Video introductorio",
                                    "body": "URL del video",
                                    "orderIndex": 0
                                }
                            ],
                            "objectives": [
                                { "description": "Ver el video completo", "orderIndex": 0 }
                            ]
                        }
                    ]
                }
            ]
        }
        """.formatted(ORG_ID);

        mockMvc.perform(put("/api/v1/courses/" + created.courseId())
                        .header("Authorization", "Bearer " + teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatedPayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Curso Actualizado"))
                .andExpect(jsonPath("$.version").value(2));

        mockMvc.perform(get("/api/v1/courses/" + created.courseId() + "/tree")
                        .header("Authorization", "Bearer " + teacherToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modules.length()").value(1))
                .andExpect(jsonPath("$.modules[0].title").value("Modulo Nuevo"))
                .andExpect(jsonPath("$.modules[0].units[0].elements[0].resourceType").value("VIDEO"))
                .andExpect(jsonPath("$.modules[0].units[0].objectives.length()").value(1));
    }

    @Test
    void deleteCourse_logicalDelete_courseBecomesInactive() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/courses/bulk")
                        .header("Authorization", "Bearer " + teacherToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bulkPayload(ORG_ID)))
                .andExpect(status().isOk())
                .andReturn();

        CourseTreeResponse created = objectMapper.readValue(
                result.getResponse().getContentAsString(), CourseTreeResponse.class);

        mockMvc.perform(delete("/api/v1/courses/" + created.courseId())
                        .header("Authorization", "Bearer " + teacherToken()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/courses/" + created.courseId())
                        .header("Authorization", "Bearer " + teacherToken()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }

    @Test
    void createCourse_withUnknownOrganization_throwsDataIntegrityException() {
        String unknownOrgId = UUID.randomUUID().toString();
        
        org.junit.jupiter.api.Assertions.assertThrows(
                Exception.class,
                () -> mockMvc.perform(post("/api/v1/courses/bulk")
                        .header("Authorization", "Bearer " + createToken("TEACHER", unknownOrgId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bulkPayload(unknownOrgId))));
    }
}
