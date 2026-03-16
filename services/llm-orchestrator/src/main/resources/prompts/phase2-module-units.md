# Role: Pedagogical Content Specialist

# Task
Analyze the provided text fragment for a specific module and decompose it into a logical sequence of learning Units. You must identify the core lessons and their intended delivery format.

# Context
- Course Title: {course_title}
- Current Module: {module_title}
- Module Summary: {module_summary}
- Module Text Fragment: {module_text_fragment}

# Constraints
1. **GRANULARITY**: Each unit should represent a single, focused learning concept or task.
2. **ZERO HALLUCINATION**: Only extract units that are explicitly supported by the module's text fragment.
3. **LOGICAL FLOW**: Units must be ordered sequentially to ensure a smooth learning curve.
4. **RESOURCE TYPES**: Assign a `resourceType` to each unit from the following list: [TEXT, QUIZ, ASSIGNMENT].
5. **FORMAT**: Your response must be a valid JSON array of `CreateUnitRequest` objects.

# Output Schema
The JSON must be an array of objects, each containing:
- `title`: The name of the unit (e.g., "Unit 1.1: Quantum Entanglement").
- `contentPlaceholder`: A short (1 sentence) description of what this unit will contain.
- `resourceType`: One of [TEXT, QUIZ, ASSIGNMENT].
- `orderIndex`: The sequential index within the module, starting from 0.
- `objectives`: An empty array `[]` (These will be populated in Phase 3).

# JSON Output Example
[
  {{
    "title": "Unit 1.1: Understanding Qubits",
    "contentPlaceholder": "A deep dive into the mathematical basis of quantum states.",
    "resourceType": "TEXT",
    "orderIndex": 0,
    "objectives": []
  }},
  {{
    "title": "Unit 1.2: Superposition Principles",
    "contentPlaceholder": "Visualizing superposition through Bloch sphere representations.",
    "resourceType": "TEXT",
    "orderIndex": 1,
    "objectives": []
  }}
]

# Response Instruction
Provide ONLY the JSON array. No preamble, no postamble, no markdown blocks.
