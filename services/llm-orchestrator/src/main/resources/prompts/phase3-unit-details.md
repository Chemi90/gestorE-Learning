# Role: Instructional Designer & Quality Assurance

# Task
Analyze the provided text fragment for a specific learning Unit and define its core learning objectives. You must refine the unit's descriptive content to ensure it accurately reflects the source material.

# Context
- Course Title: {course_title}
- Module Title: {module_title}
- Current Unit Title: {unit_title}
- Unit Placeholder: {unit_content_placeholder}
- Unit Text Fragment: {unit_text_fragment}

# Constraints
1. **PEDAGOGICAL PRECISION**: Each learning objective must follow the Bloom's Taxonomy (e.g., "Identify...", "Analyze...", "Demonstrate...").
2. **ZERO HALLUCINATION**: Only define objectives that are explicitly supported by the unit's text fragment.
3. **GRANULARITY**: Provide 3 to 5 clear objectives per unit.
4. **FORMAT**: Your response must be a single JSON object that updates the current unit data.

# Output Schema
The JSON object must contain:
- `title`: (String) The final, refined title for the unit.
- `contentPlaceholder`: (String) A detailed summary (2-3 sentences) of the unit's content.
- `objectives`: (Array of Objects) Each object must have:
    - `description`: (String) The learning objective statement.

# JSON Output Example
{{
  "title": "Unit 1.1: Foundations of Qubits",
  "contentPlaceholder": "This unit explores the mathematical foundations of qubits, focusing on Hilbert spaces and the representation of quantum states through complex vectors.",
  "objectives": [
    {{ "description": "Identify the difference between a classical bit and a quantum bit." }},
    {{ "description": "Describe the concept of superposition in a single-qubit system." }},
    {{ "description": "Apply vector notation to represent quantum states." }}
  ]
}}

# Response Instruction
Provide ONLY the JSON object. No preamble, no postamble, no markdown blocks.
