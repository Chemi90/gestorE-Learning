-- 04_rag_tables.sql

CREATE SCHEMA IF NOT EXISTS rag;

CREATE TABLE IF NOT EXISTS rag.document (
    id UUID PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    minio_object_name VARCHAR(255) NOT NULL UNIQUE,
    content_type VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,
    organization_id UUID NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_document_organization FOREIGN KEY (organization_id) REFERENCES auth.organizations(id)
);
