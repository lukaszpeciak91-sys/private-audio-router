# Project Context

## Project

`private-audio-router`

## What this project is

An experimental Android utility intended to allow communication audio from compatible applications to use the phone's built-in earpiece. The intended experience is similar to a normal phone call: the user speaks naturally into the phone and hears the remote or AI voice privately through the earpiece instead of broadcasting it through the main loudspeaker.

The primary use case is a voice conversation with an AI system in a public or shared environment—for example while walking, commuting, waiting at a bus stop, or standing near other people—without forcing nearby people to hear the responses.

Potential compatibility targets include ChatGPT Voice, Gemini voice, browser-based realtime voice applications, and other Android applications using compatible communication-audio routing. On the primary Xiaomi `2201117TY` running Android 13/API 33, physical validation now covers communication-class, assistant-class, and browser-communication-class routing through the protected POC-5 path. Cross-device, OEM, Android-version, and broader application/browser compatibility is not established and must not be assumed.

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

`private-audio-router` acts as a control surface for Android routing and remains outside the conversation audio-data path. The current POC actively plays its own locally generated silent communication track to participate in Android arbitration, but does not receive, decode, record, capture, proxy, or forward conversation audio.

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

### Planning note

The current milestone order is a direction, not a rigid implementation lock. As the project develops, research, physical testing, platform or privacy constraints, release requirements, architectural discoveries, or implementation experience may show that a small part of a later feature is worth researching or establishing earlier—especially when doing so would avoid significant rework or provide useful technical understanding.

In that situation, prefer the smallest sensible foundation or research step rather than blindly following the roadmap or prematurely implementing the entire future feature. The roadmap exists to preserve direction and prevent unnecessary scope drift, not to prevent useful learning or evidence-based changes in sequencing.

## Repository state and freshness

This document provides stable project context, not current implementation status.
The early M0 observer/request sequence is completed planning history and has been
superseded by the protected POC-5 architecture and permanent controller. Current
work must be selected from the repository, `docs/PROGRESS.md`, and
`docs/TEST_PLAN.md`, subject to the accepted history in `docs/DECISIONS.md`.

The current architecture recognizes bounded, provider-independent public playback
signatures, participates in Android communication routing with a locally generated
silent track, requests the built-in earpiece once per protected cycle, observes the
result, and performs reversible cleanup. Some optional behavior remains experimental;
consult current state rather than generalizing from this stable summary.

## Critical technical question

> Can an independent Android application, using supported public Android APIs only, request and maintain the built-in earpiece as the communication device while a realtime voice application such as current ChatGPT Voice is active?

POC-5 answers this question **yes** for the tested Xiaomi `2201117TY` on Android 13/API 33 across physically exercised communication, assistant, and browser-communication signatures: Android reported the earpiece and physical listening confirmed audible routes. Compatibility beyond that device and version remains an experimental question.

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
