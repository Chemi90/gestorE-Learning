package com.gestorelearning.common.dto.rag;

import java.time.Instant;
import java.util.UUID;

public record UploadDocumentResponse(
        UUID id,
        String filename,
        String minioObjectName,
        String contentType,
        Long size,
        Instant createdAt
) {
}
