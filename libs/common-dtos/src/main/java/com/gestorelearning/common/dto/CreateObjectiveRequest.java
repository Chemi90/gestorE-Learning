package com.gestorelearning.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateObjectiveRequest(
        @NotBlank String description,
        @NotNull Integer orderIndex
) {}
