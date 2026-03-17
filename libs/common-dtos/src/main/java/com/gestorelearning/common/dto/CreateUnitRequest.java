package com.gestorelearning.common.dto;

import com.gestorelearning.common.domain.ResourceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateUnitRequest(
        @NotBlank String title,
        String contentPlaceholder,
        @NotNull ResourceType resourceType,
        @NotNull Integer orderIndex,
        @Valid List<CreateObjectiveRequest> objectives
) {}
