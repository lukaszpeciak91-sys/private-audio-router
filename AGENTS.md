# Agent guidance

This file is authoritative for agents working in this repository. Keep changes within the project charter and the current PR stage.

## Required reading

For rapid orientation, agents may read `docs/PROJECT_CONTEXT.md` first. It is a stable onboarding summary, not a substitute for inspecting the repository or completing the task-specific reading below. `docs/PROGRESS.md` remains authoritative for current state, and `docs/DECISIONS.md` remains authoritative for accepted decisions.

Before modifying implementation code, read in order:

1. `docs/PROJECT_CHARTER.md`
2. `docs/DECISIONS.md`
3. `docs/PROGRESS.md`

When touching Android audio routing, also read:

4. `docs/ARCHITECTURE.md`
5. `docs/RESEARCH.md`
6. `docs/TEST_PLAN.md`

## Repository-first behavior

Inspect the current repository before proposing new files, structures, documentation, or implementation approaches. Preserve the existing document structure, terminology, and writing style unless an explicit restructuring task says otherwise.

Before creating documentation, determine whether the information belongs in an existing authoritative document. Prefer updating the document that owns the subject:

- project purpose and boundaries → `docs/PROJECT_CHARTER.md`
- current project state → `docs/PROGRESS.md`
- architectural and safety decisions → `docs/DECISIONS.md`
- research evidence and technical knowledge → `docs/RESEARCH.md`
- architecture → `docs/ARCHITECTURE.md`
- physical-device and behavioral testing → `docs/TEST_PLAN.md`
- engineering process → `docs/WORKFLOW.md`
- stable onboarding context → `docs/PROJECT_CONTEXT.md`

This ownership model applies when updating documentation, recording a decision or progress, adding research findings or test results, explaining architecture or workflow, and recording unresolved technical questions. Create a new documentation file only when the information clearly belongs in none of these documents, a durable reason requires a separate document, and the addition improves navigation or ownership rather than fragmenting knowledge.

Unless explicitly requested and justified, do not create duplicate TODO files, timestamped project notes, ad-hoc summaries, duplicate architecture documents, parallel decision logs, temporary research documents, one-off progress reports, or repository scratch documentation. Keep the repository small, intentional, and navigable.

## Hard rules

- Use public Android APIs only unless an explicit accepted decision says otherwise.
- No root, Shizuku, Accessibility APIs, MediaProjection, or modification of third-party apps.
- No audio interception, recording, capture, or proxying by this application.
- No OpenAI/Gemini authentication and no network dependency unless explicitly approved later.
- Never silently escalate to a more invasive technique when a test fails; document the failure and require a new decision.
- Every routing request must be reversible. Real phone calls always retain system priority.
- Never claim physical-device behavior is verified from emulator or unit tests alone.
- Keep unknown Android behavior documented as unknown until a physical-device test verifies it.
- Label research statements as **FACT**, **INFERENCE**, or **UNKNOWN**.

## Documentation duties

Documentation maintenance is part of normal engineering work. Update an authoritative document when the information it owns changes:

- meaningful implementation progress → `docs/PROGRESS.md` (also update it in every PR)
- architectural or safety decision → `docs/DECISIONS.md` (append and supersede rather than rewriting history)
- newly established Android behavior → `docs/RESEARCH.md`
- architecture change → `docs/ARCHITECTURE.md`
- new physical behavior requiring validation → `docs/TEST_PLAN.md`
- workflow change → `docs/WORKFLOW.md`

Do not mechanically update every document on every PR. Update only documents whose authoritative information changed, do not repeat the same detail across documents, and prefer references between authoritative documents over copied sections.
