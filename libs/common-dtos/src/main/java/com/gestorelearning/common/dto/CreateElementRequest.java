package com.gestorelearning.common.dto;

import com.gestorelearning.common.domain.ResourceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateElementRequest(
        @NotNull ResourceType resourceType,
        @NotBlank String title,
        String body,
        @Valid List<CreateObjectiveRequest> objectives
) {}
