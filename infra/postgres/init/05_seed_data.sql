-- 05_seed_data.sql

-- Insertar Organización Alpha Corp
INSERT INTO auth.organizations (id, name, created_at, active)
VALUES ('550e8400-e29b-41d4-a716-446655440000', 'Alpha Corp', NOW(), TRUE)
ON CONFLICT (id) DO NOTHING;

-- Insertar Usuario Admin (Password: password123)
-- Usamos el HASH BCrypt para que el auth-service lo valide correctamente
INSERT INTO auth.users (id, email, password, role, organization_id, created_at, active)
VALUES (
    '123e4567-e89b-12d3-a456-426614174000', 
    'admin@alpha.com', 
    '$2a$10$8.UnVuG9HHgffUDAlk8qn.96SSeLhpxdm.62nlHEy5G.Zhp5SHNo2', 
    'ADMIN', 
    '550e8400-e29b-41d4-a716-446655440000', 
    NOW(), 
    TRUE
)
ON CONFLICT (email, organization_id) DO NOTHING;
