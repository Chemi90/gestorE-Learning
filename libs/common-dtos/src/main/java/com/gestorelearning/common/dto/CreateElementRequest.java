package com.gestorelearning.common.dto;

import com.gestorelearning.common.domain.ResourceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateElementRequest(
        @NotNull ResourceType resourceType,
        @NotBlank String title,
        String summary,
        String body,
        @NotNull Integer orderIndex
) {}
