CREATE SCHEMA IF NOT EXISTS auth;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'user_role') THEN
        CREATE TYPE auth.user_role AS ENUM ('ADMIN', 'TEACHER', 'STUDENT');
    END IF;
END
$$;

CREATE TABLE IF NOT EXISTS auth.organizations (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS auth.users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    role auth.user_role NOT NULL,
    organization_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_users_organization FOREIGN KEY (organization_id) REFERENCES auth.organizations (id),
    CONSTRAINT uk_email_organization UNIQUE (email, organization_id)
);