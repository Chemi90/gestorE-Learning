# Skill: Creador de Skills

## Proposito
Meta-skill para crear nuevos skills cuando ningun skill existente cubre la necesidad.

## Cuando Usarlo

- El usuario solicita un nuevo skill.
- Un skill existente necesita reescritura o actualizacion.
- Se necesita unificar estilo y estructura entre skills.

## Formato Estandar (en orden)

1. Titulo claro
2. Proposito breve (1-2 lineas)
3. `## Reglas Obligatorias`
4. `## Procedimiento` o `## Workflow`
5. `## Anti-patrones a Evitar` (si es util)
6. `## Checklist` (si es util)
7. `## Referencias en el Repo` (paths reales)

## Reglas Universales

- Escribe de forma agnostica al modelo: usa "agente" o "asistente IA" en vez de frases especificas de un tool.
- No asumas herramientas propietarias salvo que sean necesarias.
- Haz los comandos adaptables al repo actual.
- Prefiere reglas verificables (paths, naming, checklists) sobre consejos vagos.
- Incluye referencias reales del proyecto cuando existan.
- Idioma: ESPANOL (comentarios de codigo en ingles son aceptables).

## Procedimiento

1. Entiende el trigger del skill (cuando se activa realmente).
2. Lee 1-3 ejemplos reales en el repo.
3. Extrae solo las reglas repetibles.
4. Escribe el skill corto (sin teoria extensa).
5. Agrega checklist minima y referencias a archivos.
6. Verifica que no duplica skills existentes.

## Plantilla Minima

```markdown
# Skill: <Nombre>

## Proposito
<Que hace este skill en 1-2 lineas>

## Reglas Obligatorias

1. <Regla verificable 1>
2. <Regla verificable 2>

## Procedimiento

1. <Paso 1>
2. <Paso 2>

## Anti-patrones a Evitar

- <Anti-patron 1>
- <Anti-patron 2>

## Referencias en el Repo

- `path/al/archivo/referencia.java`
```

## Anti-patrones a Evitar

- Skill demasiado largo y teorico
- Reglas no verificables ("escribe buen codigo")
- Duplicar documentacion externa completa
- Instrucciones dependientes de un unico agente
- Sin ejemplo ni path real del proyecto

## Checklist Final

- [ ] Es corto y enfocado?
- [ ] Tiene condicion de activacion clara?
- [ ] Contiene reglas operativas verificables?
- [ ] Es reutilizable por diferentes agentes?
- [ ] Cita paths reales del proyecto?
- [ ] Esta en espanol?
