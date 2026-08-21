# Development Workflow

```text
AUDIT / RESEARCH
        ↓
FORM A CONCRETE HYPOTHESIS
        ↓
IMPLEMENT THE SMALLEST SAFE CHANGE
        ↓
BUILD / AUTOMATED CHECKS
        ↓
PHYSICAL TEST WHEN REQUIRED
        ↓
AUDIT THE RESULT
        ↓
DOCUMENT EVIDENCE
        ↓
ITERATE
```

## Working principle

**Audit → implement → test → audit → iterate.**

The project must never knowingly implement uncertain Android behavior blindly. Scale the amount of auditing to the change's size, uncertainty, and risk: a trivial deterministic change does not need unnecessary ceremony, while uncertain Android audio behavior requires stronger research, diagnostics, and physical-device evidence.

### Before implementation

- Inspect the current repository state and relevant code.
- Read the relevant authoritative documentation and previous decisions.
- Identify what is known and what remains unknown.
- State the behavioral assumption being tested.
- Confirm that the proposed change remains within project boundaries.

### During implementation

- Change the smallest amount necessary.
- Test one important behavioral assumption at a time.
- Prefer observable diagnostic behavior over speculative workarounds.
- Do not combine experimental routing changes with unrelated UI polish.

### After implementation

- Build and run applicable automated checks.
- Test on physical hardware when behavior depends on Android hardware or routing.
- Compare the observed result with the original hypothesis.
- Audit unexpected behavior before making another change.
- Record durable evidence in the appropriate existing authoritative documentation.

## Audits

### Pre-implementation audit

Before meaningful implementation work, inspect repository state, relevant code and documentation, and previous decisions. Identify assumptions and unknowns, and ensure the proposed change does not violate project boundaries.

### Post-test audit

After a meaningful experiment or physical-device test, compare expected and observed behavior, inspect relevant diagnostics, and determine whether the hypothesis was supported. Decide whether documentation must change and whether the next step is implementation, further research, or stopping.

Audits do not require a separate document. They are part of the workflow and update existing authoritative documentation only when they produce durable information.

## No blind implementation

Do not stack speculative fixes. This workflow is prohibited:

```text
routing failed
↓
guess workaround A
↓
still failed
↓
guess workaround B
↓
add another system mechanism
↓
continue until something appears to work
```

Use this workflow instead:

```text
routing failed
↓
record exact observed behavior
↓
inspect system state / logs / diagnostics
↓
compare against expected behavior
↓
research the discrepancy
↓
make a new explicit decision
↓
run the smallest next experiment
```

A failed experiment is useful evidence. Failure alone does not authorize escalation to a more invasive Android mechanism.

## Historical PR sequence

The following M0/PR sequence is completed planning history, retained for context only. It is not the current execution order and must not be used to describe implemented foreground-service, permanent-controller, or floating-controller work as future work.

- **PR #1 — Repository foundation:** governance and documentation only.
- **PR #2 — Governance and workflow refinement:** documentation only.
- **PR #3 — Android project bootstrap:** buildable Compose application foundation with no audio behavior.
- **PR #4 — Audio Routing Diagnostic Observer:** temporary physical-device experiment UI; enumerate, identify, read, and observe only, with no routing behavior.
- **PR #5 — Next evidence-driven step:** decide only after the PR #4 physical-device experiment; actual routing is not part of PR #4.
- **PR #6 — Safe built-in-earpiece request and clear behavior:** no `AudioManager.MODE` manipulation.
- **PR #7 — Physical-device compatibility validation:** record results without assuming cross-application support.

That sequence led to the now-implemented permanent foreground controller and floating UX. Current work is stage-independent and follows `AUDIT → scoped implementation → validation → audit/iteration if needed`, using `docs/PROGRESS.md` and `docs/TEST_PLAN.md` rather than the historical PR numbers to choose the next scope.

## Pull-request ownership

- Keep one logical implementation in one PR.
- Update the existing authoritative documents that own changed information rather than creating duplicate plans, summaries, or logs.
- Codex may commit, push, and create the PR; the user reviews and merges manually.
- Physical-device evidence remains authoritative for routing behavior. Static checks, JVM tests, emulator UI checks, and cloud builds must retain their distinct evidence levels.
- Treat cloud build limitations as environment limitations, not source failures; conversely, do not use a cloud build result as proof of physical routing behavior.

## Documentation requirements

- Every PR updates `PROGRESS.md`.
- Architectural or safety changes append to `DECISIONS.md`.
- New physical behaviors to validate update `TEST_PLAN.md`.
- Research records preserve **FACT**, **INFERENCE**, and **UNKNOWN** distinctions.
- Localization work follows the authoritative policy in [`LOCALIZATION.md`](LOCALIZATION.md). Translation production and audit findings delegated for correction use `private-audio-translation`; independent verification and quality classification use the read-only-by-default `private-audio-localization-audit`. Keep the general workflow here free of duplicated localization rules.

## Stop condition

If a physical-device test fails, do not automatically introduce more invasive Android mechanisms. Document the failure first. Escalation requires a new explicit decision and must remain within the charter's safety boundaries.
