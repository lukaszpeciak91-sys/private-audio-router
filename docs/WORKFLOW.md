# Development Workflow

```text
RESEARCH
   ↓
DECISION
   ↓
IMPLEMENT SMALL
   ↓
BUILD
   ↓
AUTOMATED CHECKS
   ↓
PHYSICAL PHONE TEST
   ↓
DOCUMENT RESULT
   ↓
NEXT DECISION
```

## Working principle

Test one behavioral uncertainty at a time. Do not combine routing experiments with UI polish. Prefer small, reviewable changes whose results can update the research and decision record.

## PR sequence

- **PR #1 — Repository foundation:** governance and documentation only.
- **PR #2 — Minimal Android diagnostic shell:** no routing changes.
- **PR #3 — Communication-device discovery and observation:** enumerate, identify, read, and observe only.
- **PR #4 — Safe built-in-earpiece request and clear behavior:** no `AudioManager.MODE` manipulation.
- **PR #5 — Physical-device compatibility validation:** record results without assuming cross-application support.

Only after M0 succeeds may later PRs consider a persistent controller, foreground service if needed, floating toggle, cross-app validation, and UX polish.

## Documentation requirements

- Every PR updates `PROGRESS.md`.
- Architectural or safety changes append to `DECISIONS.md`.
- New physical behaviors to validate update `TEST_PLAN.md`.
- Research records preserve **FACT**, **INFERENCE**, and **UNKNOWN** distinctions.

## Stop condition

If a physical-device test fails, do not automatically introduce more invasive Android mechanisms. Document the failure first. Escalation requires a new explicit decision and must remain within the charter's safety boundaries.
