package com.gestorelearning.rag.service;

import com.gestorelearning.common.dto.rag.UploadDocumentResponse;
import com.gestorelearning.rag.domain.DocumentEntity;
import com.gestorelearning.rag.repository.DocumentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final MinioService minioService;

    public DocumentService(DocumentRepository documentRepository, MinioService minioService) {
        this.documentRepository = documentRepository;
        this.minioService = minioService;
    }

    @Transactional
    public UploadDocumentResponse uploadDocument(MultipartFile file, UUID organizationId) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo está vacío");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El archivo no tiene nombre");
        }

        String extension = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }

        String minioObjectName = minioService.uploadFile(file, extension);

        DocumentEntity document = new DocumentEntity();
        document.setFilename(originalFilename);
        document.setMinioObjectName(minioObjectName);
        document.setContentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream");
        document.setSize(file.getSize());
        document.setOrganizationId(organizationId);

        DocumentEntity savedDocument = documentRepository.save(document);

        return new UploadDocumentResponse(
                savedDocument.getId(),
                savedDocument.getFilename(),
                savedDocument.getMinioObjectName(),
                savedDocument.getContentType(),
                savedDocument.getSize(),
                savedDocument.getCreatedAt()
        );
    }
}
