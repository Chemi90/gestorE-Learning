package com.gestorelearning.common.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ModuleResponse(
        UUID id,
        String title,
        String summary,
        Integer orderIndex,
        Instant createdAt,
        boolean active,
        List<UnitResponse> units
) {}
