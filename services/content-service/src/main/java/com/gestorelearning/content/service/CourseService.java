package com.gestorelearning.content.service;

import com.gestorelearning.common.domain.*;
import com.gestorelearning.common.dto.*;
import com.gestorelearning.content.domain.CourseEntity;
import com.gestorelearning.content.domain.ModuleEntity;
import com.gestorelearning.content.domain.ObjectiveEntity;
import com.gestorelearning.content.domain.UnitEntity;
import com.gestorelearning.content.repository.CourseRepository;
import com.gestorelearning.content.repository.ModuleRepository;
import com.gestorelearning.content.repository.ObjectiveRepository;
import com.gestorelearning.content.repository.UnitRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;
    private final ObjectiveRepository objectiveRepository;
    private final UnitRepository unitRepository;

    public CourseService(CourseRepository courseRepository,
                         ModuleRepository moduleRepository,
                         ObjectiveRepository objectiveRepository,
                         UnitRepository unitRepository) {
        this.courseRepository = courseRepository;
        this.moduleRepository = moduleRepository;
        this.objectiveRepository = objectiveRepository;
        this.unitRepository = unitRepository;
    }

    @Transactional
    public CourseTreeResponse createFullCourse(CreateCourseBulkRequest request) {
        // 1. Crear Curso
        CourseEntity course = new CourseEntity();
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setLevel(request.level());
        course.setVersion(request.version());
        course.setOrganizationId(request.organizationId());
        course.setActive(true);
        CourseEntity savedCourse = courseRepository.save(course);

        // 2. Procesar Módulos
        if (request.modules() != null) {
            for (CreateModuleRequest modReq : request.modules()) {
                ModuleEntity module = new ModuleEntity();
                module.setCourse(savedCourse);
                module.setTitle(modReq.title());
                module.setSummary(modReq.summary());
                module.setOrderIndex(modReq.orderIndex());
                ModuleEntity savedMod = moduleRepository.save(module);

                // 3. Procesar Unidades
                if (modReq.units() != null) {
                    for (CreateUnitRequest unitReq : modReq.units()) {
                        UnitEntity unit = new UnitEntity();
                        unit.setModule(savedMod);
                        unit.setTitle(unitReq.title());
                        unit.setContentPlaceholder(unitReq.contentPlaceholder());
                        unit.setResourceType(unitReq.resourceType());
                        unit.setOrderIndex(unitReq.orderIndex());
                        UnitEntity savedUnit = unitRepository.save(unit);

                        // 4. Procesar Objetivos de la Unidad
                        if (unitReq.objectives() != null) {
                            for (CreateObjectiveRequest objReq : unitReq.objectives()) {
                                ObjectiveEntity objective = new ObjectiveEntity();
                                objective.setUnit(savedUnit);
                                objective.setDescription(objReq.description());
                                objectiveRepository.save(objective);
                            }
                        }
                    }
                }
            }
        }

        return getCourseTree(savedCourse.getId());
    }

    @Transactional
    public CourseResponse updateCourse(UUID id, CreateCourseRequest request) {
        CourseEntity course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setLevel(request.level());
        course.setVersion(request.version());
        
        CourseEntity saved = courseRepository.save(course);
        return mapToCourseResponse(saved);
    }

    @Transactional
    public void deleteCourse(UUID id) {
        CourseEntity course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        course.setActive(false); // Borrado lógico
        courseRepository.save(course);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesByOrganization(UUID organizationId) {
        return courseRepository.findByOrganizationId(organizationId).stream()
                .filter(CourseEntity::isActive) // Solo cursos activos
                .map(this::mapToCourseResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourseById(UUID id) {
        CourseEntity course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));
        return mapToCourseResponse(course);
    }

    @Transactional(readOnly = true)
    public CourseTreeResponse getCourseTree(UUID courseId) {
        CourseEntity course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        List<ModuleEntity> modules = moduleRepository.findByCourseIdOrderByOrderIndexAsc(courseId);

        List<ModuleResponse> moduleResponses = modules.stream()
                .map(this::mapToModuleResponse)
                .collect(Collectors.toList());

        return new CourseTreeResponse(
                course.getId(),
                course.getOrganizationId(),
                course.getTitle(),
                course.getLevel(),
                course.getVersion(),
                course.getCreatedAt(),
                course.isActive(),
                moduleResponses
        );
    }

    private ModuleResponse mapToModuleResponse(ModuleEntity module) {
        List<UnitEntity> units = unitRepository.findByModuleIdOrderByOrderIndexAsc(module.getId());

        List<UnitResponse> unitResponses = units.stream()
                .map(this::mapToUnitResponse)
                .collect(Collectors.toList());

        return new ModuleResponse(
                module.getId(),
                module.getTitle(),
                module.getSummary(),
                module.getOrderIndex(),
                module.getCreatedAt(),
                module.isActive(),
                unitResponses
        );
    }

    private UnitResponse mapToUnitResponse(UnitEntity unit) {
        List<ObjectiveEntity> objectives = objectiveRepository.findByUnitId(unit.getId());
        List<ObjectiveResponse> objectiveResponses = objectives.stream()
                .map(o -> new ObjectiveResponse(o.getId(), o.getDescription(), o.getCreatedAt()))
                .collect(Collectors.toList());

        return new UnitResponse(
                unit.getId(),
                unit.getTitle(),
                unit.getContentPlaceholder(),
                unit.getResourceType(),
                unit.getOrderIndex(),
                unit.getStatus(),
                unit.getCreatedAt(),
                unit.isActive(),
                objectiveResponses
        );
    }

    private CourseResponse mapToCourseResponse(CourseEntity course) {
        return new CourseResponse(
                course.getId(),
                course.getTitle(),
                course.getDescription(),
                course.getLevel(),
                course.getVersion(),
                course.getOrganizationId(),
                course.getCreatedAt(),
                course.isActive()
        );
    }
}
