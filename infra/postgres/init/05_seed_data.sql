-- 05_seed_data.sql

-- Insertar Organización Alpha Corp
INSERT INTO auth.organizations (id, name, created_at, active)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'Alpha Corp', NOW(), TRUE)
ON CONFLICT (id) DO NOTHING;

-- Insertar Usuario Admin (Password: password123)
-- Nota: Asegúrate de que el auth-service maneje la contraseña correctamente. 
-- Si usa BCrypt, esto debería ser el hash. Si no, texto plano para desarrollo.
INSERT INTO auth.users (id, email, password, role, organization_id, created_at, active)
VALUES (
    '123e4567-e89b-12d3-a456-426614174000', 
    'admin@alpha.com', 
    'password123', 
    'ADMIN', 
    '550e8400-e29b-41d4-a716-446655440000', 
    NOW(), 
    TRUE
)
ON CONFLICT (email, organization_id) DO NOTHING;
