package com.gestorelearning.content.controller;

import com.gestorelearning.common.dto.ElementResponse;
import com.gestorelearning.common.dto.UpdateElementBodyRequest;
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
}
