package com.gestorelearning.rag.controller;

import com.gestorelearning.common.dto.rag.UploadDocumentResponse;
import com.gestorelearning.rag.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Documents", description = "RAG Document Management")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Upload a document to MinIO")
    public UploadDocumentResponse upload(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-Organization-Id") UUID organizationId) {
        return documentService.uploadDocument(file, organizationId);
    }
}
