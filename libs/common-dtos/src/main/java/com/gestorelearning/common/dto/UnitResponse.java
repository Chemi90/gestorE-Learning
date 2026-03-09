package com.gestorelearning.common.dto;

import com.gestorelearning.common.domain.GenerationStatus;
import com.gestorelearning.common.domain.ResourceType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record UnitResponse(
        UUID id,
        String title,
        String contentPlaceholder,
        ResourceType resourceType,
        Integer orderIndex,
        GenerationStatus status,
        Instant createdAt,
        boolean active,
        List<ObjectiveResponse> objectives
) {}
