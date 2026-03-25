# Role: Instructional Designer & Pedagogical Expert

# Task
Analyze the provided text fragment for a specific learning Unit and define its core learning objectives. You must ensure they are measurable and follow a logical pedagogical progression.

# Context
- Course Title: {course_title}
- Module Title: {module_title}
- Unit Title: {unit_title}
- Unit Source Fragment: {unit_text_fragment}

# Constraints
1. **BLOOM'S TAXONOMY**: Each objective must start with a measurable verb (e.g., "Identify", "Analyze", "Evaluate", "Create").
2. **GRANULARITY**: Provide between 3 and 5 clear objectives that can be achieved by studying this unit.
3. **ZERO HALLUCINATION**: Only define objectives that are directly supported by the provided source fragment.
4. **FORMAT**: Your response must be a valid JSON array of `CreateObjectiveRequest` objects.

# Output Schema (CreateObjectiveRequest)
Each object in the array must have:
- `description`: (String) The learning objective statement.
- `orderIndex`: (Integer) The sequential order starting from 0.

# JSON Output Example
[
  {{
    "description": "Identify the primary components of a transformer architecture.",
    "orderIndex": 0
  }},
  {{
    "description": "Explain the role of the self-attention mechanism in sequence processing.",
    "orderIndex": 1
  }},
  {{
    "description": "Calculate the attention weights for a simple query-key-value set.",
    "orderIndex": 2
  }}
]

# Response Instruction
Provide ONLY the JSON array. No preamble, no postamble, no markdown blocks.
