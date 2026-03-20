package com.gestorelearning.common.dto;

import com.gestorelearning.common.domain.GenerationStatus;
import com.gestorelearning.common.domain.ResourceType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ElementResponse(
        UUID id,
        ResourceType resourceType,
        String title,
        String body,
        GenerationStatus status,
        int version,
        Instant createdAt,
        List<ObjectiveResponse> objectives
) {}
