# Role: Senior Course Architect

# Task
Analyze the provided source text to extract the high-level macro-structure of an educational course. You must define the course metadata and identify the main modules that will form the backbone of the curriculum.

# Context
- Source Text: {source_text}
- Target Course Level: {course_level} (e.g., BEGINNER, INTERMEDIATE, ADVANCED)
- Organization ID: {organizationId}

# Constraints
1. **ZERO HALLUCINATION**: Only extract modules that are explicitly mentioned or clearly derived from the structural divisions of the source text.
2. **COHERENCE**: Ensure the sequence of modules follows a logical pedagogical progression.
3. **FIDELITY**: Keep titles professional and representative of the actual content.
4. **FORMAT**: Your response must be a single JSON object.

# Output Schema
The JSON must include:
- `title`: The official title of the course.
- `description`: A concise overview of the course objectives.
- `level`: Must be exactly `{course_level}`.
- `version`: Use "1.0.0" as default.
- `organizationId`: Must be exactly `{organizationId}`.
- `modules`: An array of objects, each containing:
    - `title`: The name of the module (e.g., "Module 1: ...").
    - `summary`: A brief summary of the module's scope.
    - `orderIndex`: The sequential index starting from 0.

# JSON Output Example
{{
  "title": "Quantum Computing Fundamentals",
  "description": "A comprehensive guide to quantum mechanics and its application in modern computing.",
  "level": "ADVANCED",
  "version": "1.0.0",
  "organizationId": "550e8400-e29b-41d4-a716-446655440000",
  "modules": [
    {{
      "title": "Module 1: Introduction to Qubits",
      "summary": "Explores the mathematical representation of quantum bits and superposition.",
      "orderIndex": 0
    }},
    ...
  ]
}}

# Response Instruction
Provide ONLY the JSON object. No preamble, no postamble, no markdown blocks.
