package com.gestorelearning.common.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CreateUnitRequest(
        @NotBlank String title,
        @NotNull Integer orderIndex,
        @NotNull @NotEmpty @Valid List<CreateElementRequest> elements,
        @Valid List<CreateObjectiveRequest> objectives
) {}
