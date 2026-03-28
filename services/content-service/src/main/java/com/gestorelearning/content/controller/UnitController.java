package com.gestorelearning.content.controller;

import com.gestorelearning.common.dto.CreateElementRequest;
import com.gestorelearning.common.dto.CreateObjectiveRequest;
import com.gestorelearning.common.dto.ElementResponse;
import com.gestorelearning.common.dto.ObjectiveResponse;
import com.gestorelearning.content.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/units")
public class UnitController {

    private final CourseService courseService;

    public UnitController(CourseService courseService) {
        this.courseService = courseService;
    }

    @PostMapping("/{id}/elements")
    public ElementResponse addElement(@PathVariable UUID id, @Valid @RequestBody CreateElementRequest request) {
        return courseService.addElementToUnit(id, request);
    }

    @PostMapping("/{id}/objectives")
    public ObjectiveResponse addObjective(@PathVariable UUID id, @Valid @RequestBody CreateObjectiveRequest request) {
        return courseService.addObjectiveToUnit(id, request);
    }
}
