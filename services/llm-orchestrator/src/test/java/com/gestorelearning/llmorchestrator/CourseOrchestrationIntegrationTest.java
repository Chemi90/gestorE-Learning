package com.gestorelearning.llmorchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestorelearning.common.domain.CourseLevel;
import com.gestorelearning.common.dto.CourseTreeResponse;
import com.gestorelearning.common.dto.CreateCourseBulkRequest;
import com.gestorelearning.common.dto.CreateModuleRequest;
import com.gestorelearning.common.dto.CreateUnitRequest;
import com.gestorelearning.llmorchestrator.service.ValidationService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
public class CourseOrchestrationIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ValidationService validationService;

    @Test
    void testFullTreeValidationFailure() throws Exception {
        // 1. JSON de curso con un módulo que contiene una unidad INVALIDA (sin resourceType)
        String invalidTreeJson = """
        {
            "title": "Master IA",
            "level": "ADVANCED",
            "version": "1.0.0",
            "organizationId": "550e8400-e29b-41d4-a716-446655440000",
            "modules": [
                {
                    "title": "Modulo 1",
                    "orderIndex": 0,
                    "units": [
                        {
                            "title": "Unidad con error",
                            "orderIndex": 0
                        }
                    ]
                }
            ]
        }
        """;

        CreateCourseBulkRequest invalidTree = objectMapper.readValue(invalidTreeJson, CreateCourseBulkRequest.class);
        
        // 2. Al validar el objeto RAÍZ, el validador debe entrar en cascada hasta la unidad y fallar
        assertThrows(ConstraintViolationException.class, () -> {
            validationService.validate(invalidTree);
        });
    }

    @Test
    void testInvalidUnitValidation() throws Exception {
        // 1. JSON de unidad sin ResourceType (que es @NotNull en CreateUnitRequest)
        String invalidUnitJson = """
        {
            "title": "Unidad sin recurso",
            "contentPlaceholder": "Texto...",
            "orderIndex": 1
        }
        """;

        // 2. Jackson lo parsea (porque Jackson por sí solo no valida anotaciones de Bean Validation)
        CreateUnitRequest invalidUnit = objectMapper.readValue(invalidUnitJson, CreateUnitRequest.class);
        assertNotNull(invalidUnit);
        assertNull(invalidUnit.resourceType());

        // 3. Nuestro ValidationService DEBE lanzar la excepción
        assertThrows(ConstraintViolationException.class, () -> {
            validationService.validate(invalidUnit);
        });
    }

    @Test
    void testExportImportMapping() throws Exception {
        // 1. Simular un JSON de exportación proveniente del content-service (CourseTreeResponse)
        String exportJson = """
        {
            "courseId": "123e4567-e89b-12d3-a456-426614174000",
            "organizationId": "550e8400-e29b-41d4-a716-446655440000",
            "title": "Master en IA",
            "level": "ADVANCED",
            "version": "1.0.0",
            "createdAt": "2023-10-01T12:00:00Z",
            "active": true,
            "modules": [
                {
                    "id": "123e4567-e89b-12d3-a456-426614174001",
                    "title": "Módulo 1: Fundamentos",
                    "summary": "Bases de la IA",
                    "orderIndex": 0,
                    "units": []
                }
            ]
        }
        """;

        // 2. Validar que Jackson lo mapea correctamente al DTO de árbol
        CourseTreeResponse treeResponse = objectMapper.readValue(exportJson, CourseTreeResponse.class);
        assertNotNull(treeResponse);
        assertEquals("Master en IA", treeResponse.title());
        assertEquals(CourseLevel.ADVANCED, treeResponse.level());
        assertEquals(1, treeResponse.modules().size());

        // 3. Simular la transformación para Importación Masiva (Bulk Import)
        CreateCourseBulkRequest importRequest = new CreateCourseBulkRequest(
                treeResponse.title(),
                "Descripción generada o clonada",
                treeResponse.level(),
                "1.1.0", // Nueva versión
                treeResponse.organizationId(),
                List.of(new CreateModuleRequest(
                        treeResponse.modules().get(0).title(),
                        treeResponse.modules().get(0).summary(),
                        treeResponse.modules().get(0).orderIndex(),
                        null
                ))
        );

        // 4. Validar el payload que el orchestrator enviaría a POST /api/v1/courses/bulk
        String importJsonPayload = objectMapper.writeValueAsString(importRequest);
        assertNotNull(importJsonPayload);
        
        CreateCourseBulkRequest parsedImport = objectMapper.readValue(importJsonPayload, CreateCourseBulkRequest.class);
        assertEquals("1.1.0", parsedImport.version());
        assertEquals("Módulo 1: Fundamentos", parsedImport.modules().get(0).title());
    }
}
