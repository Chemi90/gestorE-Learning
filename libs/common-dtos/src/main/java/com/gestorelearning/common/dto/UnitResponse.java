package com.gestorelearning.common.dto;

import java.time.Instant;
import java.util.UUID;

public record UnitResponse(
        UUID id,
        String title,
        Integer orderIndex,
        Instant createdAt,
        boolean active,
        ElementResponse element
) {}
