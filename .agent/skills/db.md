# Skill: Capa de Datos

## Proposito
Patrones para entidades JPA, repositorios, scripts SQL de inicializacion y configuracion H2 para tests.

## Configuracion de Base de Datos

- **Motor**: PostgreSQL 16 con extension `pgvector`
- **Base de datos unica**: `elearning`
- **Un schema por servicio**: `auth`, `content`, `rag`, `exam`, `grading`, `integrity`
- **DDL en produccion**: `ddl-auto: none` — NUNCA `create` o `update`
- **Init scripts**: `infra/postgres/init/` ejecutados en orden alfabetico al arrancar Postgres

## Reglas Obligatorias

1. Cada servicio con BD tiene su propio schema — nunca comparte tablas con otro servicio.
2. `ddl-auto: none` en produccion — las tablas se crean via scripts SQL en `infra/postgres/init/`.
3. IDs son `UUID`, nunca `Long` autoincremental.
4. `@PrePersist` genera el UUID y el timestamp si son null.
5. `schema` en `@Table` es obligatorio.
6. Relaciones entre entidades usan `FetchType.LAZY` salvo justificacion explicita.

## Entidades JPA — Patron Canonico

```java
@Entity
@Table(name = "nombre_tabla", schema = "schema_servicio")
public class NombreEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String campo;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private boolean active;

    @PrePersist
    void onCreate() {
        if (id == null) id = UUID.randomUUID();
        if (createdAt == null) createdAt = Instant.now();
    }

    // getters y setters
}
```

## Enums PostgreSQL — Gotcha Critico

Cuando una columna es un enum PostgreSQL nativo (e.g. `auth.user_role`), la entidad requiere:

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false, columnDefinition = "auth.user_role")
@JdbcType(PostgreSQLEnumJdbcType.class)
private UserRole role;
```

Imports necesarios:
- `org.hibernate.annotations.JdbcType`
- `org.hibernate.dialect.PostgreSQLEnumJdbcType`

## Repositorios — Patron Canonico

```java
public interface NombreRepository extends JpaRepository<NombreEntity, UUID> {

    // Metodos derivados de Spring Data — nunca escribas JPQL si Spring Data lo puede inferir
    Optional<NombreEntity> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCaseAndOrganizationId(String email, UUID organizationId);
}
```

## Scripts SQL de Init

Ubicacion: `infra/postgres/init/`
Convencion de nombre: `NN_descripcion.sql` donde `NN` es el numero de orden (00, 01, 02...).

Archivos existentes:
- `00_extensions.sql` — `CREATE EXTENSION IF NOT EXISTS vector;`
- `01_schemas.sql` — `CREATE SCHEMA IF NOT EXISTS <schema>;`
- `02_auth_tables.sql` — tablas del auth-service

Plantilla para nuevo servicio:

```sql
-- 03_<servicio>_tables.sql

-- Si el servicio necesita un enum PostgreSQL nativo:
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_type WHERE typname = '<nombre_enum>') THEN
        CREATE TYPE <schema>.<nombre_enum> AS ENUM ('VALOR1', 'VALOR2');
    END IF;
END
$$;

CREATE TABLE IF NOT EXISTS <schema>.<tabla> (
    id UUID PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    active BOOLEAN NOT NULL DEFAULT TRUE
);
```

## Configuracion H2 para Tests

En `src/test/resources/application-test.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:<nombre>db;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;INIT=CREATE SCHEMA IF NOT EXISTS <schema>
    username: sa
    password:
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop
    properties:
      hibernate:
        default_schema: <schema>

security:
  jwt:
    secret: 01234567890123456789012345678901
```

Si el servicio usa enums PostgreSQL nativos, el `INIT` de la URL H2 debe incluir:
```
INIT=CREATE SCHEMA IF NOT EXISTS auth\;CREATE DOMAIN IF NOT EXISTS auth.user_role AS VARCHAR
```

Nota: La barra invertida `\;` separa sentencias dentro del parametro `INIT` de H2.

## Relaciones entre Entidades

```java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "organization_id", nullable = false)
private OrganizationEntity organization;
```

## Convencion de Constraints en SQL

```sql
CONSTRAINT fk_<tabla>_<referencia> FOREIGN KEY (col) REFERENCES <schema>.<tabla_ref>(id),
CONSTRAINT uk_<campo1>_<campo2> UNIQUE (campo1, campo2)
```

## Anti-patrones a Evitar

- `@GeneratedValue(strategy = GenerationType.IDENTITY)` — usar UUID manual con `@PrePersist`
- `ddl-auto: update` en produccion — las migraciones son scripts SQL
- Queries nativas sin `schema` explicito — siempre calificar tablas
- `FetchType.EAGER` sin justificacion — causa N+1 queries

## Referencias en el Repo

- `services/auth-service/src/main/java/com/gestorelearning/auth/domain/UserEntity.java`
- `services/auth-service/src/main/java/com/gestorelearning/auth/domain/OrganizationEntity.java`
- `services/auth-service/src/main/java/com/gestorelearning/auth/repository/UserRepository.java`
- `infra/postgres/init/02_auth_tables.sql`
- `services/auth-service/src/test/resources/application-test.yml`
