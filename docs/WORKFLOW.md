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

### Android lint policy

- Android lint errors block CI, and correctness, platform, resource, accessibility,
  and other actionable warnings are expected to be fixed.
- `UseKtx` is not enforced because it is a style preference rather than a
  correctness rule. Code may still use KTX when it naturally improves an
  implementation.
- Only explicitly accepted toolchain-freshness advisories may remain visible. CI
  compares each warning's path, issue ID, and exact message with that accepted set;
  every unexpected warning blocks the build.

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

## Historical M0 sequence

The completed early sequence established repository governance, bootstrapped the
Compose application, progressed from read-only routing observation through bounded
earpiece experiments, and required physical validation before broader claims. It led
to the protected POC-5 path, permanent foreground controller, and floating UX now in
the repository. Historical PR numbers and superseded experiments remain available in
Git and the decision/evidence documents; they do not define current execution order.

Choose current work from `PROGRESS.md` and `TEST_PLAN.md`, then follow
`AUDIT → scoped implementation → validation → audit/iteration if needed`.

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
