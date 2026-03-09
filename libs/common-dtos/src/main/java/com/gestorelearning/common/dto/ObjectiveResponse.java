package com.gestorelearning.common.dto;

import java.time.Instant;
import java.util.UUID;

public record ObjectiveResponse(
        UUID id,
        String description,
        Instant createdAt
) {}
