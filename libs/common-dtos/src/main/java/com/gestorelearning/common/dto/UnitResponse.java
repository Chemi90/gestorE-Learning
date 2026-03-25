package com.gestorelearning.common.dto;

import com.gestorelearning.common.domain.CourseLevel;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UnitResponse(
        UUID id,
        String title,
        Integer orderIndex,
        Instant createdAt,
        boolean active,
        List<ElementResponse> elements,
        List<ObjectiveResponse> objectives
) {}
