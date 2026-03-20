-- 03_content_tables.sql

-- Enums para Content
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

-- Tablas
CREATE TABLE IF NOT EXISTS content.courses (
    id UUID PRIMARY KEY,
    organization_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    level content.course_level NOT NULL,
    version VARCHAR(20) NOT NULL DEFAULT '1.0.0',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_courses_organization FOREIGN KEY (organization_id) REFERENCES auth.organizations(id)
);

CREATE TABLE IF NOT EXISTS content.modules (
    id UUID PRIMARY KEY,
    course_id UUID NOT NULL REFERENCES content.courses(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    summary TEXT,
    order_index INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_module_course_order UNIQUE (course_id, order_index)
);

-- Slot de posicion en el temario (sin contenido propio)
CREATE TABLE IF NOT EXISTS content.units (
    id UUID PRIMARY KEY,
    module_id UUID NOT NULL REFERENCES content.modules(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    order_index INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_unit_module_order UNIQUE (module_id, order_index)
);

-- Contenido real de la unidad (reutilizable y versionable)
CREATE TABLE IF NOT EXISTS content.elements (
    id UUID PRIMARY KEY,
    unit_id UUID NOT NULL REFERENCES content.units(id) ON DELETE CASCADE,
    organization_id UUID NOT NULL,
    resource_type content.resource_type NOT NULL,
    title VARCHAR(255) NOT NULL,
    body TEXT,
    status content.generation_status NOT NULL DEFAULT 'PENDING',
    version INT NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_elements_organization FOREIGN KEY (organization_id) REFERENCES auth.organizations(id)
);

-- Objetivos atomicos: prompts quirurgicos para la fase de redaccion del LLM
CREATE TABLE IF NOT EXISTS content.objectives (
    id UUID PRIMARY KEY,
    element_id UUID NOT NULL REFERENCES content.elements(id) ON DELETE CASCADE,
    description TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
