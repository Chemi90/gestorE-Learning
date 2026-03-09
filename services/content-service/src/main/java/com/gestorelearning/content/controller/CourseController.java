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

    @GetMapping("/{id}")
    public CourseResponse getCourseById(@PathVariable UUID id) {
        return courseService.getCourseById(id);
    }

    @PutMapping("/{id}")
    public CourseResponse updateCourse(@PathVariable UUID id, @Valid @RequestBody CreateCourseRequest request) {
        return courseService.updateCourse(id, request);
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
