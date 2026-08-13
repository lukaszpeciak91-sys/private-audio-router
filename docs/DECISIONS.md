# Decision Log

This is a lightweight, append-only log. Do not rewrite accepted history; append a decision that supersedes an earlier entry.

## D-001 — Public Android APIs only

- **Date:** 2026-08-08
- **Status:** Accepted
- **Context:** The project needs the least invasive foundation for communication-audio routing.
- **Decision:** The project begins using only supported public Android APIs.
- **Consequences:** Unsupported, privileged, root, Shizuku, Accessibility, and MediaProjection mechanisms are outside the approved design. A change requires a new explicit decision.

## D-002 — No audio capture or proxying

- **Date:** 2026-08-08
- **Status:** Accepted
- **Context:** The utility should control routing without handling conversation content.
- **Decision:** The application must not place itself in the audio data path.
- **Consequences:** It will not record, intercept, capture, decode, forward, or proxy application or microphone audio.

## D-003 — No `AudioManager.MODE` manipulation in M0

- **Date:** 2026-08-08
- **Status:** Accepted
- **Context:** We want to determine whether communication-device routing alone is sufficient before touching broader system audio behavior.
- **Decision:** The first diagnostic prototype must not call `setMode()` or otherwise change `AudioManager.MODE`.
- **Consequences:** M0 tests communication-device discovery, observation, request, and clear behavior in isolation. Further investigation requires evidence and a separate decision.

## D-004 — Routing must be reversible

- **Date:** 2026-08-08
- **Status:** Accepted
- **Context:** A routing utility can disrupt normal device behavior if it leaves influence behind.
- **Decision:** All routing requests must have a clear restore/clear path.
- **Consequences:** Disable, process termination, and reboot behavior require validation; failure to restore normal behavior blocks release.

## D-005 — Real phone calls retain priority

- **Date:** 2026-08-08
- **Status:** Accepted
- **Context:** Android telephony has safety-critical, system-level routing responsibilities.
- **Decision:** The utility must never attempt to override Android telephony routing.
- **Consequences:** Incoming and outgoing phone calls require physical-device tests, and any conflict must fail in favor of telephony.

## D-006 — Provider independence

- **Date:** 2026-08-08
- **Status:** Accepted
- **Context:** Routing is an Android system concern, not an AI-provider integration.
- **Decision:** The project must not depend on ChatGPT, Gemini, OpenAI, Google, or any specific AI application.
- **Consequences:** No provider authentication, credentials, conversation processing, or provider-specific network dependency is permitted.

## D-007 — Physical-device validation is authoritative

- **Date:** 2026-08-08
- **Status:** Accepted
- **Context:** Emulators and automated tests cannot establish real earpiece and OEM routing behavior.
- **Decision:** Emulator and automated tests may validate code behavior, but real routing compatibility requires testing on physical Android hardware.
- **Consequences:** Compatibility remains unknown until recorded physical-device tests pass; emulator-only results cannot support compatibility claims.

## D-008 — Single-module Compose application foundation

- **Date:** 2026-08-10
- **Status:** Accepted
- **Context:** The project needs a maintainable Android foundation before communication-audio experiments begin.
- **Decision:** Begin with one `app` module using Kotlin, Jetpack Compose, Material 3, AndroidX, and Gradle Kotlin DSL.
- **Consequences:** The bootstrap has no navigation, dependency injection, persistence, networking, or speculative feature packages. New structure is added only when working code requires it.

## D-009 — Android SDK baseline

- **Date:** 2026-08-10
- **Status:** Accepted
- **Context:** The future M0 communication-device APIs are available from Android 12 (API 31), while public distribution requires a current target SDK.
- **Decision:** Use API 31 as `minSdk` and API 36 as both `compileSdk` and `targetSdk` for the initial application.
- **Consequences:** M0 can use the public communication-device APIs without compatibility branches. Devices below Android 12 are intentionally unsupported, and SDK levels must be reviewed as tooling and distribution requirements evolve.

## D-010 — Provisional application identity

- **Date:** 2026-08-10
- **Status:** Accepted
- **Context:** No organization-owned domain or permanent production application ID has been established.
- **Decision:** Use `app.privateaudio` as the provisional namespace and application ID.
- **Consequences:** A globally unique production ID must be selected before public distribution; changing it later may affect upgrades and published identity.

## D-011 — Controlled communication-mode participation for POC-2

- **Date:** 2026-08-13
- **Status:** Accepted; supersedes D-003 for POC-2 only
- **Context:** POC-1 on device product `2201117TY` found that a one-shot communication-device request returned `true`, but Android continued to report ChatGPT Voice on the built-in speaker. A routing-only request is therefore insufficient evidence of control on this device.
- **Decision:** POC-2 may explicitly set `AudioManager.mode` to `MODE_IN_COMMUNICATION` after an armed experiment observes an external communication session on the built-in speaker, then issue exactly one built-in-earpiece request.
- **Consequences:** The diagnostic must record the pre-change mode and post-request state, relinquish its mode participation with `MODE_NORMAL` during every cleanup path, avoid retry loops and services, and yield to telephony/system-priority modes. This authorization does not extend to production behavior or a later persistence experiment.
