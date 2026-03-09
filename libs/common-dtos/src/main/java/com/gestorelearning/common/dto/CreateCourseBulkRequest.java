package com.gestorelearning.common.dto;

import com.gestorelearning.common.domain.CourseLevel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreateCourseBulkRequest(
        @NotBlank String title,
        String description,
        @NotNull CourseLevel level,
        @NotBlank String version,
        @NotNull UUID organizationId,
        List<CreateModuleRequest> modules
) {}
