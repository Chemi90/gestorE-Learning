# Skill: Coherencia de Datos (SQL-Entity-Test)

## Proposito
Garantizar la sincronía absoluta entre las Entidades Java (JPA), los esquemas SQL de producción/infraestructura y los esquemas SQL de tests de integración para evitar fallos de persistencia en tiempo de ejecución.

## Reglas Obligatorias

1. **La Tríada Sagrada**: Cualquier cambio estructural (añadir/quitar/renombrar columnas o tipos) DEBE aplicarse simultáneamente en:
   - **Entidad**: `services/**/domain/*.java`
   - **SQL Infra**: `infra/postgres/init/*.sql`
   - **SQL Test**: `services/**/src/test/resources/init-test.sql`
2. **Naming Strict**: Los nombres de columnas en SQL (`snake_case`) deben mapear exactamente con los campos en Java (`camelCase`) según las convenciones de Hibernate configuradas en el proyecto.
3. **Sincronía de Tipos**: Los Enums definidos en Postgres (`CREATE TYPE ... AS ENUM`) deben coincidir 1:1 con los Enums de Java.
4. **Verificación Innegociable**: Tras un cambio en el modelo, se DEBE ejecutar `mvn clean compile` en el microservicio afectado para validar el mapeo de Hibernate antes de realizar cualquier commit.

## Procedimiento

1. **Detección de Impacto**: Al recibir una instrucción de cambio en el modelo, localizar los tres archivos afectados (Entidad, SQL Infra, SQL Test).
2. **Edición en Espejo**:
   - Actualizar la Entidad Java (campos, getters/setters, anotaciones `@Column`).
   - Actualizar el script de creación en `infra/postgres/init/`.
   - Replicar exactamente la tabla en el `init-test.sql` del microservicio.
3. **Validación Cruzada**: Comparar línea a línea que no falte ningún campo nuevo en los SQLs y que los tipos (`TEXT`, `UUID`, `INT`, `TIMESTAMPTZ`) sean coherentes.
4. **Prueba de Humo**: Ejecutar el build de Maven para confirmar que la aplicación levanta correctamente el contexto de persistencia.

## Anti-patrones a Evitar

- Olvidar el `init-test.sql`: Provoca que los tests pasen localmente pero fallen en CI/CD.
- Desajuste de tipos: Usar `VARCHAR` en un SQL y `TEXT` en otro, o diferentes precisiones.
- Omitir restricciones: No replicar `NOT NULL` o `DEFAULT` en los tres mundos.

## Checklist de Coherencia

- [ ] ¿El campo existe en la Entidad Java?
- [ ] ¿La columna existe en el SQL de infraestructura (`infra/postgres/init`)?
- [ ] ¿La tabla está actualizada en el `init-test.sql` del servicio?
- [ ] ¿Los Enums/Tipos coinciden exactamente?

## Referencias en el Repo (Universal)

- **Entidades**: `services/*/src/main/java/**/domain/*.java`
- **SQL Infra (Todos los dominios)**: `infra/postgres/init/*.sql`
- **SQL Test (Todos los servicios)**: `services/*/src/test/resources/init-test.sql`
- **Contratos/DTOs**: `libs/common-dtos/src/main/java/**/dto/*.java`
