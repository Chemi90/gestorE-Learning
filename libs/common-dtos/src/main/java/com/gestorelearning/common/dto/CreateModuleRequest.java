package com.gestorelearning.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateModuleRequest(
        @NotBlank String title,
        String summary,
        @NotNull Integer orderIndex,
        @Valid List<CreateUnitRequest> units
) {}
