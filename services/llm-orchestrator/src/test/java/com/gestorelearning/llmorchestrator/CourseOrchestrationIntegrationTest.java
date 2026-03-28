package com.gestorelearning.llmorchestrator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestorelearning.common.domain.CourseLevel;
import com.gestorelearning.common.domain.ResourceType;
import com.gestorelearning.common.dto.CourseTreeResponse;
import com.gestorelearning.common.dto.CreateCourseBulkRequest;
import com.gestorelearning.common.dto.CreateElementRequest;
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
        // JSON de curso con una unidad que no tiene elements (campo nulo o vacio depende de validacion)
        // En CreateUnitRequest no pusimos @NotEmpty sino que el flujo espera datos.
        String invalidTreeJson = """
        {
            "title": "Master IA",
            "level": "ADVANCED",
            "version": 1,
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

        // ValidationService debe validar la estructura
        assertThrows(ConstraintViolationException.class, () -> {
            validationService.validate(invalidTree);
        });
    }

    @Test
    void testInvalidElementValidation() throws Exception {
        // Unidad con elements pero uno sin resourceType (@NotNull en CreateElementRequest)
        String invalidUnitJson = """
        {
            "title": "Unidad sin recurso",
            "orderIndex": 1,
            "elements": [
                {
                    "title": "Elemento sin tipo",
                    "body": "Texto...",
                    "orderIndex": 0
                }
            ],
            "objectives": []
        }
        """;

        CreateUnitRequest invalidUnit = objectMapper.readValue(invalidUnitJson, CreateUnitRequest.class);
        assertNotNull(invalidUnit);
        assertNull(invalidUnit.elements().get(0).resourceType());

        // ValidationService DEBE lanzar la excepcion
        assertThrows(ConstraintViolationException.class, () -> {
            validationService.validate(invalidUnit);
        });
    }

    @Test
    void testExportImportMapping() throws Exception {
        // Simular un JSON de exportacion proveniente del content-service (CourseTreeResponse)
        String exportJson = """
        {
            "courseId": "123e4567-e89b-12d3-a456-426614174000",
            "organizationId": "550e8400-e29b-41d4-a716-446655440000",
            "title": "Master en IA",
            "level": "ADVANCED",
            "version": 1,
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

        CourseTreeResponse treeResponse = objectMapper.readValue(exportJson, CourseTreeResponse.class);
        assertNotNull(treeResponse);
        assertEquals("Master en IA", treeResponse.title());
        assertEquals(CourseLevel.ADVANCED, treeResponse.level());
        assertEquals(1, treeResponse.modules().size());
        assertEquals(1, treeResponse.version());

        // Simular la transformacion para Importacion Masiva (Bulk Import)
        CreateCourseBulkRequest importRequest = new CreateCourseBulkRequest(
                treeResponse.title(),
                "Descripcion generada o clonada",
                treeResponse.level(),
                2,
                treeResponse.organizationId(),
                List.of(new CreateModuleRequest(
                        treeResponse.modules().get(0).title(),
                        treeResponse.modules().get(0).summary(),
                        treeResponse.modules().get(0).orderIndex(),
                        null
                ))
        );

        String importJsonPayload = objectMapper.writeValueAsString(importRequest);
        assertNotNull(importJsonPayload);

        CreateCourseBulkRequest parsedImport = objectMapper.readValue(importJsonPayload, CreateCourseBulkRequest.class);
        assertEquals(2, parsedImport.version());
        assertEquals("Módulo 1: Fundamentos", parsedImport.modules().get(0).title());
    }

    @Test
    void testValidUnitWithElementAndObjectives() throws Exception {
        // Unidad valida con element y objectives directos
        CreateUnitRequest validUnit = new CreateUnitRequest(
                "Introduccion a los Grafos",
                0,
                List.of(new CreateElementRequest(
                        ResourceType.TEXT,
                        "Introduccion a los Grafos",
                        "Explicacion detallada sobre nodos, aristas y representacion de grafos.",
                        "Un grafo es una estructura de datos...",
                        0
                )),
                List.of(new com.gestorelearning.common.dto.CreateObjectiveRequest(
                        "Entender que es un nodo",
                        0
                ))
        );

        // No debe lanzar excepcion
        assertDoesNotThrow(() -> validationService.validate(validUnit));
    }
}
