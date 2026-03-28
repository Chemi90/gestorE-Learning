package com.gestorelearning.content.service;

import com.gestorelearning.common.dto.*;
import com.gestorelearning.content.domain.CourseEntity;
import com.gestorelearning.content.domain.ElementEntity;
import com.gestorelearning.content.domain.ModuleEntity;
import com.gestorelearning.content.domain.ObjectiveEntity;
import com.gestorelearning.content.domain.UnitEntity;
import com.gestorelearning.content.repository.CourseRepository;
import com.gestorelearning.content.repository.ElementRepository;
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
    private final UnitRepository unitRepository;
    private final ElementRepository elementRepository;
    private final ObjectiveRepository objectiveRepository;

    public CourseService(CourseRepository courseRepository,
                         ModuleRepository moduleRepository,
                         UnitRepository unitRepository,
                         ElementRepository elementRepository,
                         ObjectiveRepository objectiveRepository) {
        this.courseRepository = courseRepository;
        this.moduleRepository = moduleRepository;
        this.unitRepository = unitRepository;
        this.elementRepository = elementRepository;
        this.objectiveRepository = objectiveRepository;
    }

    @Transactional
    public CourseTreeResponse createFullCourse(CreateCourseBulkRequest request) {
        CourseEntity course = new CourseEntity();
        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setLevel(request.level());
        course.setVersion(request.version());
        course.setOrganizationId(request.organizationId());
        course.setActive(true);
        CourseEntity savedCourse = courseRepository.save(course);

        saveModules(savedCourse, request.modules());

        return getCourseTree(savedCourse.getId());
    }

    @Transactional
    public CourseResponse updateCourseWithTree(UUID id, CreateCourseBulkRequest request) {
        CourseEntity course = courseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Course not found"));

        course.setTitle(request.title());
        course.setDescription(request.description());
        course.setLevel(request.level());
        course.setVersion(request.version());

        CourseEntity savedCourse = courseRepository.save(course);

        // Delete nativa: delega el CASCADE (units → elements/objectives) al motor de BD
        moduleRepository.deleteByCourseIdNative(id);

        saveModules(savedCourse, request.modules());

        return mapToCourseResponse(savedCourse);
    }

    private void saveModules(CourseEntity course, List<CreateModuleRequest> moduleRequests) {
        if (moduleRequests == null) return;
        for (CreateModuleRequest modReq : moduleRequests) {
            ModuleEntity module = new ModuleEntity();
            module.setCourse(course);
            module.setTitle(modReq.title());
            module.setSummary(modReq.summary());
            module.setOrderIndex(modReq.orderIndex());
            ModuleEntity savedMod = moduleRepository.save(module);

            if (modReq.units() != null) {
                for (CreateUnitRequest unitReq : modReq.units()) {
                    UnitEntity unit = new UnitEntity();
                    unit.setModule(savedMod);
                    unit.setTitle(unitReq.title());
                    unit.setOrderIndex(unitReq.orderIndex());
                    UnitEntity savedUnit = unitRepository.save(unit);

                    saveElements(savedUnit, unitReq.elements());
                    saveObjectives(savedUnit, unitReq.objectives());
                }
            }
        }
    }

    private void saveElements(UnitEntity unit, List<CreateElementRequest> elementRequests) {
        if (elementRequests == null) return;
        for (CreateElementRequest elementReq : elementRequests) {
            ElementEntity element = new ElementEntity();
            element.setUnit(unit);
            element.setResourceType(elementReq.resourceType());
            element.setTitle(elementReq.title());
            element.setSummary(elementReq.summary());
            element.setBody(elementReq.body());
            element.setOrderIndex(elementReq.orderIndex());
            elementRepository.save(element);
        }
    }

    private void saveObjectives(UnitEntity unit, List<CreateObjectiveRequest> objectiveRequests) {
        if (objectiveRequests == null) return;
        for (CreateObjectiveRequest objReq : objectiveRequests) {
            ObjectiveEntity objective = new ObjectiveEntity();
            objective.setUnit(unit);
            objective.setDescription(objReq.description());
            objective.setOrderIndex(objReq.orderIndex());
            objectiveRepository.save(objective);
        }
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
        course.setActive(false); // Borrado logico
        courseRepository.save(course);
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> getCoursesByOrganization(UUID organizationId) {
        return courseRepository.findByOrganizationId(organizationId).stream()
                .filter(CourseEntity::isActive)
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

    @Transactional
    public UnitResponse addUnitToModule(UUID moduleId, CreateUnitRequest request) {
        ModuleEntity module = moduleRepository.findById(moduleId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Module not found"));

        UnitEntity unit = new UnitEntity();
        unit.setModule(module);
        unit.setTitle(request.title());
        unit.setOrderIndex(request.orderIndex());
        UnitEntity savedUnit = unitRepository.save(unit);

        if (request.elements() != null) {
            saveElements(savedUnit, request.elements());
        }
        if (request.objectives() != null) {
            saveObjectives(savedUnit, request.objectives());
        }

        return mapToUnitResponse(savedUnit);
    }

    @Transactional
    public ElementResponse addElementToUnit(UUID unitId, CreateElementRequest request) {
        UnitEntity unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found"));

        ElementEntity element = new ElementEntity();
        element.setUnit(unit);
        element.setResourceType(request.resourceType());
        element.setTitle(request.title());
        element.setSummary(request.summary());
        element.setBody(request.body());
        element.setOrderIndex(request.orderIndex());
        ElementEntity saved = elementRepository.save(element);

        return mapToElementResponse(saved);
    }

    @Transactional
    public ObjectiveResponse addObjectiveToUnit(UUID unitId, CreateObjectiveRequest request) {
        UnitEntity unit = unitRepository.findById(unitId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Unit not found"));

        ObjectiveEntity objective = new ObjectiveEntity();
        objective.setUnit(unit);
        objective.setDescription(request.description());
        objective.setOrderIndex(request.orderIndex());
        ObjectiveEntity saved = objectiveRepository.save(objective);

        return mapToObjectiveResponse(saved);
    }

    @Transactional
    public ElementResponse updateElementBody(UUID elementId, UpdateElementBodyRequest request) {
        ElementEntity element = elementRepository.findById(elementId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Element not found"));

        element.setBody(request.body());
        element.setStatus(com.gestorelearning.common.domain.GenerationStatus.COMPLETED);
        ElementEntity saved = elementRepository.save(element);

        return mapToElementResponse(saved);
    }

    private ElementResponse mapToElementResponse(ElementEntity e) {
        return new ElementResponse(
                e.getId(),
                e.getResourceType(),
                e.getTitle(),
                e.getSummary(),
                e.getBody(),
                e.getStatus(),
                e.getVersion(),
                e.getOrderIndex(),
                e.isActive(),
                e.getCreatedAt()
        );
    }

    private ObjectiveResponse mapToObjectiveResponse(ObjectiveEntity o) {
        return new ObjectiveResponse(
                o.getId(),
                o.getDescription(),
                o.getOrderIndex(),
                o.isActive(),
                o.getCreatedAt()
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
        List<ElementEntity> elements = elementRepository.findByUnitIdOrderByOrderIndexAsc(unit.getId());
        List<ObjectiveEntity> objectives = objectiveRepository.findByUnitIdOrderByOrderIndexAsc(unit.getId());

        List<ElementResponse> elementResponses = elements.stream()
                .map(this::mapToElementResponse)
                .collect(Collectors.toList());

        List<ObjectiveResponse> objectiveResponses = objectives.stream()
                .map(this::mapToObjectiveResponse)
                .collect(Collectors.toList());

        return new UnitResponse(
                unit.getId(),
                unit.getTitle(),
                unit.getOrderIndex(),
                unit.getCreatedAt(),
                unit.isActive(),
                elementResponses,
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
