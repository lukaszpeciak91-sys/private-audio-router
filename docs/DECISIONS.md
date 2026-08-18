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

## D-012 — Bounded route reassertion for POC-3

- **Date:** 2026-08-13
- **Status:** Accepted; extends D-011 for POC-3 only
- **Context:** POC-2 on device product `2201117TY` requested `MODE_IN_COMMUNICATION` and made one accepted earpiece request, but Android continued to report the built-in speaker until ChatGPT Voice ended.
- **Decision:** One explicitly armed POC-3 run may retain POC-2 mode participation and make at most three `setCommunicationDevice()` calls. After the initial qualifying-state request, each of at most two additional requests must wait for a controlled 750 ms delay and revalidate that the session remains in `MODE_IN_COMMUNICATION` with the built-in speaker reported.
- **Consequences:** Pending work must be cancelled on every cleanup path; callbacks and delayed work cannot exceed the three-call cap or act after disarm/session exit. This is a bounded diagnostic experiment, not authorization for continuous reassertion, polling, a foreground service, or POC-4.

## D-013 — Explicit communication-mode ownership transition for POC-4

- **Date:** 2026-08-13
- **Status:** Accepted; supersedes D-012 for POC-4 only
- **Context:** All three bounded POC-3 earpiece requests returned `true`, but Android continued to report the built-in speaker. POC-2 and POC-3 requested `MODE_IN_COMMUNICATION` while that mode was already reported, so neither established whether Private Audio became the latest application to select communication mode.
- **Decision:** One explicitly armed POC-4 run may perform the shortest practical observable `MODE_IN_COMMUNICATION` → `MODE_NORMAL` → `MODE_IN_COMMUNICATION` sequence, verify each reported mode, and then make exactly one built-in-earpiece request.
- **Consequences:** The diagnostic records the transition, the single request, callbacks, immediate and delayed observations, and later speaker reclamation. It cancels pending observation on cleanup, clears its device request, returns to `MODE_NORMAL` when its experimental participation ends, and yields immediately to telephony/system-priority modes. This is not authorization for retries, a foreground service, or POC-5.

## D-014 — Silent communication playback participation for POC-5

- **Date:** 2026-08-14
- **Status:** Accepted; supersedes D-013 for the current diagnostic experiment
- **Context:** External `dumpsys audio` evidence showed ChatGPT as the effective communication-mode owner while Private Audio's accepted earpiece request remained a losing route-client request. Private Audio requested communication mode but had no active communication playback or recording. The explicit POC-4 mode transition did not physically change the route, so active communication participation can be tested without another forced transition.
- **Decision:** One explicitly armed POC-5 run may create and continuously feed silence to a minimal public `AudioTrack` configured for `USAGE_VOICE_COMMUNICATION`, `CONTENT_TYPE_SPEECH`, mono PCM 16-bit output. Only after the track reports `PLAYSTATE_PLAYING`, it requests `MODE_IN_COMMUNICATION` and then makes exactly one built-in-earpiece request.
- **Consequences:** The track contains no captured, forwarded, or externally sourced audio; no microphone or audio-focus request is permitted. The diagnostic records public playback observations and one approximately one-second route observation. Every cleanup path cancels pending work, clears the route, relinquishes mode participation, and stops, flushes, and releases the track. Telephony/system-priority modes remain blocking. This is not authorization for retries, a foreground service, or POC-6.

## D-015 — Harden the physically successful public-API POC-5

- **Date:** 2026-08-14
- **Status:** Accepted
- **Context:** On Xiaomi `2201117TY` running Android 13/API 33, POC-5 both produced an Android-reported built-in-earpiece route and made active ChatGPT Voice physically audible through the upper earpiece. This is the project's first confirmed physical routing success.
- **Decision:** Keep the current public-API POC-5 architecture and prioritize reproducibility, cleanup, lifecycle, update, and telephony-safety testing. Do not pivot to audio capture, an owned AI client, root, Shizuku, privileged APIs, or another routing architecture while this approach is physically working.
- **Consequences:** The silent local communication track remains outside ChatGPT's audio-data path. No retry loop or POC-6 is authorized. Full uninstall and reinstall preceded the first repeatable physically audible success; its causal relationship is not established and must be investigated through stability testing rather than assumed.

## D-016 — Sole service ownership for POC-5 lifecycle

- **Date:** 2026-08-15
- **Status:** Accepted
- **Context:** The physically successful POC-5 was owned by `MainActivity`, so activity destruction necessarily stopped the experiment and could not provide an independent armed lifetime.
- **Decision:** Exactly one local, non-exported `PrivateAudioService` owns the diagnostic observer and POC-5 state. The service is started and promoted to a `specialUse` foreground service only by the visible activity's explicit Arm action, while binding alone supports the visible diagnostic UI without requiring foreground state. Disarm clears through the existing observer before leaving foreground and stopping the started lifetime. Process restart is fail-closed with `START_NOT_STICKY`, no persistence, and no automatic re-arm.
- **Consequences:** Activity recreation binds back to the existing service and cannot create a second observer. Unbinding does not disarm a started experiment; genuine service destruction performs existing observer cleanup. Android process death may bypass `onDestroy()` and loses all in-memory state. This lifecycle migration does not alter POC-5 trigger, playback, routing, observation, cleanup, evidence, or safety semantics, and does not introduce the future product-state adapter.

## D-017 — Product-enabled intent spans one-shot POC-5 runs

- **Date:** 2026-08-16
- **Status:** Accepted; extends D-016 for Layer 1.5
- **Context:** A product activation should remain useful across separate external voice sessions, while the protected POC-5 experiment must remain one-shot and must clean up at the end of every communication session.
- **Decision:** `PrivateAudioService` owns an in-memory product-enabled intent distinct from each observer-owned POC-5 run. After a completed run reaches `CLEARED` and establishes its existing cleanup state, the observer provides a structural completion notification; while intent remains enabled and shutdown is not underway, the service arms exactly one fresh waiting experiment. Disable clears intent before protected cleanup. `BLOCKED` runs, telephony/system-priority blocks, shutdown, and process death never auto-rearm.
- **Consequences:** One user activation can cover multiple sequential voice sessions without making POC-5 continuous. Waiting owns no `AudioTrack` and makes no routing request. Every session still receives a fresh experiment, the existing one-request cap and cleanup ordering remain unchanged, failures do not retry, `START_NOT_STICKY` remains in force, and enabled intent is not persisted across process death. Layer 2 still owns the final product-state presentation.

## D-018 — Permanent communication controller with playback-evidence session boundaries

- **Date:** 2026-08-16
- **Status:** Accepted; supersedes D-017's completion/re-arm mechanism
- **Context:** Physical Layer 1.5 testing found that Private Audio's silent communication track kept `MODE_IN_COMMUNICATION` reported after the first external session, so mode departure never completed the one-shot run and later sessions could not route.
- **Decision:** While explicitly enabled, the service registers one public `AudioPlaybackCallback` and waits without routing influence. Matching active `USAGE_VOICE_COMMUNICATION`/`CONTENT_TYPE_SPEECH` playback starts an isolated POC-5 cycle subject to existing mode, route, device, and priority eligibility. After the local matching track starts, an observed additional matching contribution establishes external-playback evidence; a fall to only the known local contribution must remain stable for 1.5 seconds before cleanup. Cleanup returns directly to a fresh clean waiting cycle while enabled. Generation identity invalidates stale delayed work.
- **Consequences:** Playback count is an inference, not package, UID, provider, or client identity. Ordinary media does not qualify. OFF unregisters controller observation and fully cleans up. Ambiguous evidence fails conservatively, no polling/retry/reassertion is added, and physical multi-session, transient-recreation, OEM callback, media-isolation, and telephony validation remains required.

## D-019 — Service-owned four-state product projection

- **Date:** 2026-08-16
- **Status:** Accepted
- **Context:** Future product surfaces need one stable interpretation of the permanent controller's current evidence without controlling audio or independently interpreting diagnostic history.
- **Decision:** `PrivateAudioService` exposes one Compose-observable `privateAudioState` projected as exactly `READY`, `WAITING`, `ACTIVE`, or `ERROR`. Precedence is controller OFF → `READY`, current protected failure → `ERROR`, current participating cycle plus Android-reported `MODE_IN_COMMUNICATION` and current built-in earpiece → `ACTIVE`, otherwise enabled → `WAITING`.
- **Consequences:** `ACTIVE` is a current-evidence claim, never historical success. `WAITING` is the normal enabled state before, between, and after communication sessions. Historical success, failure, or speaker reversion cannot override current evidence, and UI consumers must consume the service-owned state rather than duplicate this mapping. The projection is read-only and adds no routing behavior or API.

## D-020 — Service-owned call-like proximity-screen participation

- **Date:** 2026-08-18
- **Status:** Accepted; pending physical Layer 7A validation
- **Context:** Call-like earpiece use needs screen behavior that follows each authoritative routing cycle without UI ownership, polling, sensor interpretation, or changes to the protected routing controller.
- **Decision:** `PrivateAudioService` owns one non-reference-counted public `PROXIMITY_SCREEN_OFF_WAKE_LOCK` through a mechanics-only helper. It holds the lock only while the controller is enabled, product state is `ACTIVE`, Android reports `MODE_IN_COMMUNICATION`, and the current communication device is the built-in earpiece. Observer evidence changes synchronously notify the service to re-evaluate this predicate idempotently.
- **Consequences:** Every departure from eligibility releases ownership; Power OFF and service destruction also release fail-safe. Unsupported devices remain routing-capable with no screen influence. Main and Floating own no proximity behavior. OEM screen response, near-release behavior, and manual Power-button interaction remain **UNKNOWN** until the Layer 7A physical gate passes.

## D-021 — Service-owned proximity preference

- **Date:** 2026-08-18
- **Status:** Accepted; extends D-020 for Layer 7B
- **Context:** Call-like screen behavior needs a user opt-out without changing Private Audio routing, state, session detection, or UI ownership boundaries.
- **Decision:** `PrivateAudioService` owns one default-ON boolean in private `SharedPreferences` and includes it in D-020's single eligibility predicate. Main Settings may read and change the value through the service; the mechanics-only controller and Floating UI do not read or store it.
- **Consequences:** OFF immediately releases eligible proximity ownership and prevents future acquisition while leaving the audio cycle untouched; ON immediately reacquires when all existing evidence qualifies. Repeated writes are inert. Physical Layer 7B lifecycle, accessory, telephony, persistence, and termination validation remains required.

## D-022 — Close Layer 7 implementation while retaining physical evidence boundaries

- **Date:** 2026-08-18
- **Status:** Accepted; closes Layer 7 implementation, extends D-020 and D-021
- **Context:** Physical Xiaomi `2201117TY` evidence now establishes the core call-like near/off and far/on behavior during `ACTIVE` built-in-earpiece routing. A final repository audit found the eligibility, lifecycle fail-safes, persisted default-ON opt-out, diagnostics, and downstream-only architecture implemented without changing the protected audio controller. The later statement that Layer 7B works as intended is useful overall confirmation but does not identify which individual physical matrix cases were exercised.
- **Decision:** Treat Layer 7 implementation as complete based on the physically proven core behavior plus the implemented and automated fail-safe contracts. Continue to require separately recorded physical evidence before claiming any individual OEM, accessory, telephony, UI-transition, setting-persistence, or termination case passed.
- **Consequences:** The Xiaomi near/off and far/on result is **FACT**. Bluetooth and other non-earpiece routes, real telephony, session-end-while-near, successive proximity cycles, individual Power OFF/Main Close/Mini transitions, preference changes and recreation persistence, process/service termination, and physical Power-button behavior remain **UNKNOWN / NOT TESTED** as physical behavior. These are retained as regression and characterization coverage, not silently converted into PASS results.

## D-023 — Direction-driven Mini internal layout

- **Date:** 2026-08-18
- **Status:** Accepted
- **Context:** The floating Mini controller must support bidirectional product locales without conflating the overlay window's physical screen position with the reading direction of the content inside it.
- **Decision:** The overlay window continues to use physical screen coordinates and physical left-origin positioning, while its internal Mini composition follows Android `layoutDirection`. In physical left-to-right screen order, the internal composition is `STATUS → POWER → EXPAND → CLOSE` in LTR and mirrors to `CLOSE → EXPAND → POWER → STATUS` in RTL. The RTL status group also mirrors its status-indicator position and text direction. Visible controls and their hit regions mirror together, and Power remains centered.
- **Consequences:** The implementation is direction-driven, not locale-name-driven, so every current and future RTL locale inherits the behavior automatically. This decision does not authorize changes to external overlay positioning or drag-coordinate behavior.

## D-024 — Measured, component-local fitting for localized UI text

- **Date:** 2026-08-18
- **Status:** Accepted
- **Context:** Natural localized action labels can be substantially wider than their English equivalents; Portuguese BR `Configurações` and Ukrainian `Налаштування` are known real-device examples, but the fitting policy must serve every locale rather than encode language-specific exceptions.
- **Decision:** Correct, natural native-language meaning has priority, and a valid translation must not be replaced merely because English is shorter. A single-word action label must not intentionally break arbitrarily inside the word. When a label does not fit, first verify that its component uses a reasonable available width. Where wrapping is permitted, multi-word labels may wrap at natural word boundaries. If a single word still does not fit, use a small, component-local, bounded text-size fallback where appropriate. Consider a native compact translation only when the component genuinely cannot accommodate the normal wording. Ellipsis is a final safety mechanism, not the preferred presentation for primary action labels.
- **Consequences:** One locale must not cause a global typography reduction. Runtime fitting uses actual rendered or measured width rather than raw character count. Any compact visible wording preserves the same semantics; accessibility text may be fuller but must not contradict the visible control. These rules apply to all current and future locales.
