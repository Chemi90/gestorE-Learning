package com.gestorelearning.content.controller;


import com.gestorelearning.common.dto.*;

import com.gestorelearning.content.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/modules")
public class ModuleController {

    private final CourseService courseService;

    public ModuleController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping("/{id}/units")
    public UnitResponse addUnit(@PathVariable UUID id, @Valid @RequestBody CreateUnitRequest request) {
        return courseService.addUnitToModule(id, request);
    }

    @PutMapping("/{id}")
    public ModuleResponse updateModule(@PathVariable UUID id, @Valid @RequestBody CreateModuleRequest request) {
        return courseService.updateModule(id, request);
    }

    @DeleteMapping("/{id}")
    public void deleteModule(@PathVariable UUID id) {
        courseService.deleteModule(id);
    }
}
