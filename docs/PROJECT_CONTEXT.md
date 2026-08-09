# Project Context

## Project

`private-audio-router`

## What this project is

An experimental Android utility intended to allow communication audio from compatible applications to use the phone's built-in earpiece. The intended experience is similar to a normal phone call: the user speaks naturally into the phone and hears the remote or AI voice privately through the earpiece instead of broadcasting it through the main loudspeaker.

The primary use case is a voice conversation with an AI system in a public or shared environment—for example while walking, commuting, waiting at a bus stop, or standing near other people—without forcing nearby people to hear the responses.

Potential compatibility targets include ChatGPT Voice, Gemini voice, browser-based realtime voice applications, and other Android applications using compatible communication-audio routing. Compatibility with any specific application must be physically validated and must not be assumed.

## Architectural idea

```text
External voice application
        |
        v
Android communication audio
        |
        v
Android AudioManager routing
        |
        v
Built-in earpiece
```

`private-audio-router` should act as a control surface for Android routing and remain outside the audio data path. It should not receive, decode, record, capture, proxy, or forward conversation audio.

## Hard boundaries

- Public Android APIs first.
- No root, Shizuku, Accessibility Service, or MediaProjection.
- No audio capture or proxying.
- No modification of ChatGPT, Gemini, browsers, or other third-party applications.
- No provider authentication or OpenAI or Google credentials.
- No network dependency unless a future architectural decision explicitly approves one.
- No attempt to override real phone-call routing.
- Routing changes must be reversible; failure must leave or restore normal Android behavior.

**The project should control routing, not control the application producing the audio.**

## Engineering philosophy

**Audit → implement → test → audit → iterate.** Inspect before changing and understand before guessing. Make the smallest useful experiment, test uncertain Android behavior on physical hardware, and treat failures as evidence rather than stacking speculative workarounds. Update existing authoritative documentation instead of creating documentation clutter.

## Repository state and freshness

This document provides stable project context and is intentionally not the authoritative source for current implementation status. For current state, always inspect the repository, `docs/PROGRESS.md`, `docs/DECISIONS.md`, and relevant repository history.

Do not infer completed implementation or milestones, physical test results, current blockers, or the next implementation task solely from this file. When this context was introduced, the project was pursuing **M0 — Prove the route**, but milestone status may change. `docs/PROGRESS.md` remains authoritative for current state.

## Current technical strategy

The M0 strategy, without implying implementation status, is to:

1. establish a minimal Android diagnostic environment;
2. inspect available communication devices;
3. identify the built-in earpiece;
4. observe the current communication device;
5. observe communication-device changes;
6. test explicit earpiece request and clear behavior;
7. validate behavior on physical Android hardware;
8. compare results with the hypothesis; and
9. decide the next step from evidence.

`AudioManager.MODE` manipulation is not part of the initial M0 approach unless a future explicit decision changes that.

## Critical technical question

> Can an independent Android application, using supported public Android APIs only, request and maintain the built-in earpiece as the communication device while a realtime voice application such as current ChatGPT Voice is active?

This is an experimental question, not an established capability.

## Authoritative documentation map

- `PROJECT_CHARTER.md` — mission, scope, product principles, and non-goals
- `ARCHITECTURE.md` — intended technical architecture and boundaries
- `DECISIONS.md` — accepted architectural and safety decisions
- `PROGRESS.md` — authoritative current project state
- `RESEARCH.md` — **FACT** / **INFERENCE** / **UNKNOWN** technical knowledge
- `TEST_PLAN.md` — physical-device validation matrix and results
- `WORKFLOW.md` — engineering process
- `AGENTS.md` — repository instructions for coding agents

`PROJECT_CONTEXT.md` is an onboarding summary and navigation layer. It does not replace these authoritative documents.

## For a fresh AI session

Read this file for orientation. Then inspect the current repository and the authoritative documents relevant to the task before proposing or implementing changes. Preserve existing decisions. Prefer updating existing documentation over creating parallel files. Never assume implementation state from this context file alone.
