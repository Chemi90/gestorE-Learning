package com.gestorelearning.common.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateElementBodyRequest(
        @NotBlank String body
) {}
