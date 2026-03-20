# Spec: Frontend Angular — Sync con DTO actual de content-service

**Estado:** BORRADOR — pendiente aprobacion
**Fecha:** 2026-03-20
**Motivacion:** El frontend Angular sigue usando el contrato viejo de `Unit` (`contentPlaceholder`, `resourceType`, `status`, `objectives` directos), pero el backend actual ya implementa el modelo `unit -> element -> objectives`.

---

## 1. Objetivo

Actualizar el frontend Angular para que consuma y envie el contrato real actual del `content-service`, manteniendo la UX existente del editor de cursos lo mas estable posible.

Objetivos concretos:

- Alinear los modelos TypeScript con los DTO Java actuales.
- Ajustar el formulario reactivo del editor para usar `element` anidado dentro de cada `unit`.
- Corregir el `CourseService.updateCourse()` para enviar el payload correcto al `PUT /api/v1/courses/{id}`.
- Mantener las rutas y el flujo general del frontend sin cambios estructurales.

No incluye:

- Cambios de backend
- Cambios de esquema SQL
- Rediseño visual mayor

---

## 2. Contrato backend actual

Fuente real del contrato:

- `libs/common-dtos/.../CreateUnitRequest.java`
- `libs/common-dtos/.../CreateElementRequest.java`
- `libs/common-dtos/.../UnitResponse.java`
- `libs/common-dtos/.../ElementResponse.java`
- `services/content-service/.../CourseController.java`

### Request actual

```ts
type CreateCourseBulkRequest = {
  title: string;
  description: string;
  level: CourseLevel;
  version: string;
  organizationId: string;
  modules: Array<{
    title: string;
    summary?: string;
    orderIndex: number;
    units: Array<{
      title: string;
      orderIndex: number;
      element: {
        resourceType: ResourceType;
        title: string;
        body?: string;
        objectives?: Array<{ description: string }>;
      };
    }>;
  }>;
};
```

### Response actual del arbol

```ts
type CourseTreeResponse = {
  courseId: string;
  organizationId: string;
  title: string;
  level: CourseLevel;
  version: string;
  createdAt: string;
  active: boolean;
  modules: Array<{
    id: string;
    title: string;
    summary?: string;
    orderIndex: number;
    createdAt: string;
    active: boolean;
    units: Array<{
      id: string;
      title: string;
      orderIndex: number;
      createdAt: string;
      active: boolean;
      element: {
        id: string;
        resourceType: ResourceType;
        title: string;
        body?: string;
        status: GenerationStatus;
        version: number;
        createdAt: string;
        objectives: Array<{
          id: string;
          description: string;
          createdAt: string;
        }>;
      };
    }>;
  }>;
};
```

### Nota sobre `PUT`

El backend actual recibe `CreateCourseBulkRequest` tambien en `PUT /api/v1/courses/{id}` y no `CreateCourseRequest`.

---

## 3. Archivos a modificar

### Frontend

1. `apps/frontend-angular/src/app/core/models/course.model.ts`
   - Sincronizar interfaces TS con DTOs Java actuales.
   - Crear `ElementResponse` y `CreateElementRequest`.
   - Actualizar `UnitResponse`, `CreateUnitRequest`, `CreateModuleRequest`.
   - Corregir tipos de `ModuleResponse` y `ObjectiveResponse` para incluir campos reales del backend.

2. `apps/frontend-angular/src/app/services/course.service.ts`
   - Cambiar `updateCourse()` para aceptar `CreateCourseBulkRequest`.
   - Limpiar imports no usados.

3. `apps/frontend-angular/src/app/components/course-tree.component.ts`
   - Reestructurar el `FormArray` de unidades para que cada `unit` tenga un `FormGroup` anidado `element`.
   - Cambiar template editable para capturar `resourceType`, `body` y objetivos dentro de `element`.
   - Cambiar template readonly para leer `unit.element.*`.

4. `apps/frontend-angular/src/app/pages/course-editor-page.component.ts`
   - Rehidratar el formulario desde `CourseTreeResponse` usando `unit.element`.
   - En modo creacion, precargar `organizationId` desde `AuthService` cuando exista.
   - Asegurar que el payload enviado a create/update coincide con el contrato `CreateCourseBulkRequest`.
   - Evitar enviar la forma vieja (`contentPlaceholder`, `resourceType`, `objectives` planos).

### Opcional segun necesidad detectada durante implementacion

5. `apps/frontend-angular/src/app/pages/course-list-page.component.ts`
   - Solo si aparece algun ajuste de tipos por cambios en `CourseResponse`.

---

## 4. Decision de UX

Para no rehacer toda la pantalla:

- Se mantiene el campo visible `Unit Title`.
- Se agrega/usa un bloque de `Element` dentro de cada unidad.
- `Element Title` se mostrara explicitamente en el formulario para respetar el contrato real del backend.
- `Element Body` reemplaza semanticamente al antiguo `contentPlaceholder`.
- `Objectives` se mantienen visibles debajo del `element`, porque conceptualmente ahora pertenecen al contenido y no al nodo de posicion.

Razon:

- Es la forma mas clara de reflejar el backend actual sin meter logica implicita de duplicacion entre `unit.title` y `element.title`.

---

## 5. Riesgos y compatibilidad

- El frontend actual rompe contra el backend actual en editor/visualizacion de curso por contrato desalineado.
- `CreateCourseRequest` queda potencialmente sin uso en el editor; no necesariamente se elimina si no estorba.
- Si hay datos viejos cacheados en navegador, no deberian romper porque el estado vive en memoria/localStorage solo para auth.

---

## 6. Verificacion

Comandos previstos:

```bash
cd apps/frontend-angular && npm run build
cd apps/frontend-angular && npm test -- --watch=false
```

Si `node_modules` no esta instalado, el comando de instalacion seria:

```bash
cd apps/frontend-angular && npm ci
```

---

## 7. Criterios de aceptacion

- El frontend compila con el modelo DTO actual.
- El editor carga correctamente un `CourseTreeResponse` con `unit.element`.
- Crear curso envia `CreateCourseBulkRequest` valido.
- Editar curso envia `CreateCourseBulkRequest` valido en `PUT`.
- La vista readonly muestra `resourceType`, `body` y `objectives` desde `element`.
- No quedan referencias activas al contrato viejo (`contentPlaceholder`, `resourceType` plano, `status` plano, `objectives` planos en `Unit`).
