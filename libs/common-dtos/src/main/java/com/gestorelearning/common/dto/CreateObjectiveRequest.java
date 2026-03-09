package com.gestorelearning.common.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateObjectiveRequest(
        @NotBlank String description
) {}
