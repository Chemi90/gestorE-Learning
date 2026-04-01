package com.gestorelearning.content.controller;

import com.gestorelearning.common.dto.*;
import com.gestorelearning.content.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/elements")
public class ElementController {

    private final CourseService courseService;

    public ElementController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PatchMapping("/{id}/body")
    public ElementResponse updateBody(@PathVariable UUID id, @Valid @RequestBody UpdateElementBodyRequest request) {
        return courseService.updateElementBody(id, request);
    }

    @PutMapping("/{id}")
    public ElementResponse updateElement(@PathVariable UUID id, @Valid @RequestBody CreateElementRequest request) {
        return courseService.updateElement(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteElement(@PathVariable UUID id) {
        courseService.deleteElement(id);
    }
}
