-- init-test.sql
-- Asegurar esquemas
CREATE SCHEMA IF NOT EXISTS content;
CREATE SCHEMA IF NOT EXISTS auth;

-- Crear tipos personalizados
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

-- Tabla de organizaciones (mock para auth)
CREATE TABLE IF NOT EXISTS auth.organizations (
    id UUID PRIMARY KEY
);

-- Tabla de cursos
CREATE TABLE IF NOT EXISTS content.courses (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    level content.course_level NOT NULL,
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_courses_organization FOREIGN KEY (organization_id) REFERENCES auth.organizations(id)
);

-- Tabla de modulos
CREATE TABLE IF NOT EXISTS content.modules (
    id UUID PRIMARY KEY,
    course_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    summary TEXT,
    order_index INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_modules_course FOREIGN KEY (course_id) REFERENCES content.courses(id) ON DELETE CASCADE,
    CONSTRAINT uk_module_course_order UNIQUE (course_id, order_index)
);

-- Tabla de unidades
CREATE TABLE IF NOT EXISTS content.units (
    id UUID PRIMARY KEY,
    module_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    order_index INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_units_module FOREIGN KEY (module_id) REFERENCES content.modules(id) ON DELETE CASCADE,
    CONSTRAINT uk_unit_module_order UNIQUE (module_id, order_index)
);

-- Tabla de elementos
CREATE TABLE IF NOT EXISTS content.elements (
    id UUID PRIMARY KEY,
    unit_id UUID NOT NULL,
    resource_type content.resource_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT,
    order_index INT NOT NULL,
    status content.generation_status NOT NULL DEFAULT 'PENDING',
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_elements_unit FOREIGN KEY (unit_id) REFERENCES content.units(id) ON DELETE CASCADE,
    CONSTRAINT uk_element_unit_order UNIQUE (unit_id, order_index)
);

-- Tabla de objetivos
CREATE TABLE IF NOT EXISTS content.objectives (
    id UUID PRIMARY KEY,
    unit_id UUID NOT NULL,
    description TEXT NOT NULL,
    order_index INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_objectives_unit FOREIGN KEY (unit_id) REFERENCES content.units(id) ON DELETE CASCADE,
    CONSTRAINT uk_objective_unit_order UNIQUE (unit_id, order_index)
);

-- Organizacion de prueba para satisfacer las FK
INSERT INTO auth.organizations (id) VALUES ('550e8400-e29b-41d4-a716-446655440000')
ON CONFLICT DO NOTHING;
