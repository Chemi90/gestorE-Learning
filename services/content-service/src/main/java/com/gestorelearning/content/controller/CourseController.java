package com.gestorelearning.content.controller;

import com.gestorelearning.common.dto.*;
import com.gestorelearning.content.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping("/bulk")
    public CourseTreeResponse createFullCourse(@Valid @RequestBody CreateCourseBulkRequest request) {
        return courseService.createFullCourse(request);
    }

    @PostMapping("/{id}/modules")
    public ModuleResponse addModule(@PathVariable UUID id, @Valid @RequestBody CreateModuleRequest request) {
        return courseService.addModuleToCourse(id, request);
    }

    @GetMapping
    @SuppressWarnings("unchecked")
    public java.util.List<CourseResponse> getCourses(org.springframework.security.core.Authentication authentication) {
        java.util.Map<String, Object> details = (java.util.Map<String, Object>) authentication.getDetails();
        String orgIdStr = (String) details.get("organizationId");
        if (orgIdStr == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "No organizationId in JWT");
        }
        return courseService.getCoursesByOrganization(UUID.fromString(orgIdStr));
    }

    @GetMapping("/{id}")
    public CourseResponse getCourseById(@PathVariable UUID id) {
        return courseService.getCourseById(id);
    }

    @PutMapping("/{id}/tree")
    public CourseResponse updateCourse(@PathVariable UUID id, @Valid @RequestBody CreateCourseBulkRequest request) {
        return courseService.updateCourseWithTree(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteCourse(@PathVariable UUID id) {
        courseService.deleteCourse(id);
    }

    @GetMapping("/{id}/tree")
    public CourseTreeResponse getCourseTree(@PathVariable UUID id) {
        return courseService.getCourseTree(id);
    }
}
