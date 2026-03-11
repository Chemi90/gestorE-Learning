-- Asegurar esquemas
CREATE SCHEMA IF NOT EXISTS content;
CREATE SCHEMA IF NOT EXISTS auth;

-- Crear tipos personalizados (lo que Hibernate NO sabe hacer solo)
DO $$ BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'course_level') THEN
        CREATE TYPE content.course_level AS ENUM ('BEGINNER', 'INTERMEDIATE', 'ADVANCED');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'resource_type') THEN
        CREATE TYPE content.resource_type AS ENUM ('TEXT', 'VIDEO', 'QUIZ', 'ASSIGNMENT');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = 'generation_status') THEN
        CREATE TYPE content.generation_status AS ENUM ('PENDING', 'GENERATING', 'COMPLETED', 'FAILED');
    END IF;
END $$;

-- Tabla dummy para evitar fallos de FK si Hibernate las intenta crear
CREATE TABLE IF NOT EXISTS auth.organizations (
    id UUID PRIMARY KEY
);
