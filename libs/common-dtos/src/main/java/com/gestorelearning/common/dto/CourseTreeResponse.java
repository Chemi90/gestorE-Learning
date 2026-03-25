package com.gestorelearning.common.dto;

import com.gestorelearning.common.domain.CourseLevel;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CourseTreeResponse(
        UUID courseId,
        UUID organizationId,
        String title,
        CourseLevel level,
        int version,
        Instant createdAt,
        boolean active,
        List<ModuleResponse> modules
) {}
