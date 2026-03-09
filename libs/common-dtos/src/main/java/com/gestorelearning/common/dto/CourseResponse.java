package com.gestorelearning.common.dto;

import com.gestorelearning.common.domain.CourseLevel;

import java.time.Instant;
import java.util.UUID;

public record CourseResponse(
        UUID id,
        String title,
        String description,
        CourseLevel level,
        String version,
        UUID organizationId,
        Instant createdAt,
        boolean active
) {}
