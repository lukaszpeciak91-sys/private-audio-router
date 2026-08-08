# Agent guidance

This file is authoritative for agents working in this repository. Keep changes within the project charter and the current PR stage.

## Required reading

Before modifying implementation code, read in order:

1. `docs/PROJECT_CHARTER.md`
2. `docs/DECISIONS.md`
3. `docs/PROGRESS.md`

When touching Android audio routing, also read:

4. `docs/ARCHITECTURE.md`
5. `docs/RESEARCH.md`
6. `docs/TEST_PLAN.md`

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

- Update `docs/PROGRESS.md` after meaningful work and in every PR.
- Append to `docs/DECISIONS.md` when an architectural or safety decision changes; supersede rather than rewrite history.
- Update `docs/TEST_PLAN.md` when new device behavior needs validation.
