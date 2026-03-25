package com.gestorelearning.common.dto;

import com.gestorelearning.common.domain.CourseLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateCourseRequest(
        @NotBlank String title,
        String description,
        @NotNull CourseLevel level,
        int version,
        @NotNull UUID organizationId
) {}
