package com.gestorelearning.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateUnitRequest(
        @NotBlank String title,
        @NotNull Integer orderIndex,
        @NotNull @Valid CreateElementRequest element
) {}
