# Progress

State reflects evidence, not aspiration.

## DONE

- Layer 3 Main product UI implemented: the normal activity now renders one fixed-geometry OLED-black Compose screen directly from the service-owned `READY` / `WAITING` / `ACTIVE` / `ERROR` projection. Power delegates to the existing controller ON/OFF operations, Close performs existing safe shutdown before removing the task, and visually complete Floating/Settings controls deliberately introduce no later-layer behavior. Four production-layout previews and focused UI/source-contract tests were added. Diagnostic collection, report formatting/copy infrastructure, the protected Layer 1.6 controller, and Layer 2 state semantics remain unchanged. Physical visual comparison and combined state/routing validation remain required.
- Layer 2 product-state projection implemented: `PrivateAudioService` now exposes one Compose-observable, read-only `privateAudioState` with exactly `READY`, `WAITING`, `ACTIVE`, and `ERROR`. OFF has highest precedence; current protected failure precedes current confirmed participating earpiece routing; all other enabled conditions are normal waiting. The pure mapping ignores historical success/reversion and adds no routing behavior. Physical/UI consumption remains unverified until a later layer.
- Layer 1.6 permanent controller implemented: Power ON registers provider-independent public playback observation and waits without audio influence; qualifying communication starts one protected POC-5 cycle; stable inferred external-playback end cleans it fully and returns to waiting under the same enabled intent. Cycle generations protect OFF, destruction, and later sessions from stale timers. Layer 1.5 physically failed because its mode-departure completion boundary remained sticky behind the local track. Layer 1.6 physical validation is required.

- Layer 1.5 multi-session lifecycle implemented: `PrivateAudioService` now owns a non-persisted enabled intent distinct from each one-shot observer experiment. Normal completed-run cleanup reaches `CLEARED` before exactly one fresh waiting arm; Disable clears intent before cleanup, while blocked/telephony failures, shutdown, and process death do not auto-rearm. Protected POC-5 routing, one-request, observation, and cleanup semantics remain unchanged. Physical multi-session validation is still required.
- Layer 1 lifecycle ownership migration completed: one local non-exported started/bindable `PrivateAudioService` now solely owns the existing observer, diagnostic evidence, and POC-5 experiment. Explicit visible Arm promotes it to a `specialUse` foreground service before arming; Disarm/Clear cleans up and ends foreground/started lifetime; activity recreation only rebinds; restart remains fail-closed with `START_NOT_STICKY` and no persistence or automatic re-arm. The diagnostic UI and protected POC-5 routing semantics remain in place.
- POC-5 Layer 0 characterization protection added before lifecycle migration: JVM source-contract tests now lock down trigger gating, telephony/system-priority blocking, silent-track and mode/request ordering, the single-request cap, reentrant/delayed no-retry behavior, failure and session-exit cleanup paths, cleanup ordering/idempotence, and the existing report-copy formatter path without changing production routing behavior.
- Project concept established.
- Primary use case defined.
- Preliminary research completed.
- Android communication-device APIs identified conceptually.
- Safety boundaries established.
- Repository foundation established.
- Repository-first documentation ownership and evidence-driven workflow established.
- Stable AI-session onboarding context added.
- Android application bootstrap completed with Kotlin, Compose, Material 3, and a reproducible Gradle build.
- Observe-only audio diagnostic implemented using public Android APIs, with current-state snapshots, device callbacks, an in-memory event log, and plain-text clipboard report copying.
- Observe-only audio diagnostic compile failure fixed by removing the unavailable `TYPE_ECHO_REFERENCE` device-type label.
- A physical-device observation (device/build metadata not yet recorded) found that ChatGPT Voice start was temporally correlated with `MODE_NORMAL` changing to `MODE_IN_COMMUNICATION`, the communication device changing from the built-in earpiece to the built-in speaker, and speakerphone changing from off to on; ending Voice correlated with the reverse changes. This is an observation, not universal behavior or proof of causation.
- Safe one-shot earpiece-routing PoC implemented: an explicitly armed run can react once to the observed communication state, request the built-in earpiece with the public API, retain diagnostic evidence, and clear its request on session exit, user action, priority modes, or clean activity destruction.
- POC-1 executed on device product `2201117TY`: `setCommunicationDevice(Built-in earpiece)` returned `true`, but Android continued to report the built-in speaker during ChatGPT Voice; after the session Android returned to the earpiece and cleanup completed. Audible routing and software-version metadata were not recorded.
- POC-2 communication-mode participation implemented as a bounded experiment: after explicit arming and the qualifying external state, Private Audio records pre-change state, requests `MODE_IN_COMMUNICATION`, makes one earpiece request, and relinquishes both route and mode participation during cleanup.
- POC-2 executed on device product `2201117TY`: Private Audio successfully requested `MODE_IN_COMMUNICATION` and its single earpiece request returned `true`, but Android continued to report the built-in speaker during ChatGPT Voice; Android returned to the earpiece only after Voice ended.
- POC-3 bounded route reassertion implemented: an armed run makes no more than three earpiece requests, spaces additional attempts by 750 ms, revalidates eligibility before each attempt, records per-attempt evidence and route outcomes, and cancels delayed work during cleanup.
- POC-3 executed: all three bounded earpiece requests returned `true`, but Android never reported the built-in earpiece while ChatGPT Voice remained active.
- POC-4 explicit mode-ownership experiment implemented: an armed run records the qualifying state, briefly transitions through and verifies `MODE_NORMAL`, immediately re-establishes and verifies `MODE_IN_COMMUNICATION`, makes exactly one earpiece request, and records immediate, callback, and one-second observations.
- POC-4 diagnostics enriched without changing its experiment sequence: the copied report now correlates Private Audio PID/UID/package and process importance with four compact state snapshots, including public active-playback usage/content/capture-policy/device fields, plus an explicit external `dumpsys audio` correlation section. An immediate corrective iteration removed unavailable playback-state and session-ID members and treats `getActivePlaybackConfigurations()` as the active snapshot.
- POC-4 executed on the tested Xiaomi device: its explicit communication-mode transition did not physically move active ChatGPT Voice from the built-in speaker to the earpiece.
- POC-5 active-requester experiment implemented in place of the unexecuted POC-4 sequence: after explicit arming and the qualifying external state, Private Audio starts a mono PCM silent `USAGE_VOICE_COMMUNICATION`/`CONTENT_TYPE_SPEECH` `AudioTrack`, confirms `PLAYSTATE_PLAYING`, records visible active playback, requests `MODE_IN_COMMUNICATION`, and makes exactly one earpiece request. Cleanup stops the writer, clears routing and mode participation, and stops, flushes, and releases the track.
- POC-5 mode-request path audited after physical evidence contained no successful Private Audio `setMode` AudioService event. The implementation already explicitly called `setMode(MODE_IN_COMMUNICATION)` after confirming the silent track was playing and before its single earpiece request; diagnostics now record the invocation timestamp and thread, local track state, modes immediately before/after, exact exception if any, and ordering of the route request. This application-side evidence does not establish actual mode ownership.
- A physical POC-5 run on Xiaomi `2201117TY` (Android 13/API 33) confirmed the instrumented application-side sequence and active Private Audio communication playback. A simultaneous live audio dump showed Private Audio's earpiece route client but no Private Audio mode requester; ChatGPT remained the sole active mode owner and its speaker route won. Repository and Android 13 framework audit found that the source manifest omits the documented `MODIFY_AUDIO_SETTINGS` permission and that AOSP silently returns from `setMode()` when this permission check fails, but the observed accepted route client prevents treating the omission as the proven cause until the installed package permission state and focused AudioService log are captured.
- Follow-up physical diagnostics confirmed that the installed Private Audio APK did not request `android.permission.MODIFY_AUDIO_SETTINGS`; focused system logcat captured AudioService receiving Private Audio's `setMode(MODE_IN_COMMUNICATION)` call and rejecting it with `Audio Settings Permission Denial: setMode()`. The manifest now requests the required public permission without changing POC-5 routing behavior.
- POC-5 passed on Xiaomi `2201117TY` running Android 13/API 33 with ChatGPT package `com.openai.chatgpt` and Private Audio package `app.privateaudio`. Diagnostics confirmed the silent communication track was created and playing, visible as active `VOICE_COMMUNICATION` playback, active before the explicit mode request, followed by exactly one accepted earpiece request. Android changed from built-in speaker/speakerphone on to built-in earpiece/speakerphone off while ChatGPT Voice remained active, and active communication playback configurations reported built-in earpiece/device ID 2.
- Repeated physical listening after a full uninstall and reinstall confirmed active ChatGPT Voice audio through the phone's upper built-in earpiece. POC-5 is Android-reported PASS, human-audible PASS, and overall PASS—the project's first confirmed physical success. The reinstall preceded the first repeatable audible success, but causality is not established.

## CURRENT

- **Layer 3 now consumes Layer 2's four-state service projection in the normal product UI. Layer 1.6 and the combined product state/routing flow remain implemented but not physically validated. POC-5 remains physically successful on the tested Xiaomi configuration and remains a bounded public-API experiment per voice session; no POC-6, retry, or invasive alternative is authorized.**

## NEXT

- Run the Layer 3 product-screen physical/visual gate in `TEST_PLAN.md` and tune isolated visual constants from screenshots without changing controller or state architecture.
- Run the Layer 1.6 permanent-controller physical gate in `TEST_PLAN.md`, including two sequential Voice sessions from one Enable and a third session after Disable.
- Verify no silent `AudioTrack` survives cleanup and ordinary audio behavior returns afterward.

## UNKNOWN

- Why full uninstall and reinstall preceded the first repeatable physically audible success; no causal explanation is established.
- Whether the confirmed POC-5 result remains stable across repeated sessions, lifecycle transitions, APK updates, and other devices or software versions.
- Whether ChatGPT immediately overrides such a request.
- Whether Gemini behaves similarly.
- Whether browser-based voice sessions use compatible communication routing.
- OEM-specific routing differences.
- Exact behavior during incoming and outgoing real phone calls.
- Whether communication-device callbacks are delivered while this application is backgrounded on tested Android/OEM builds, and whether process lifetime interrupts an experiment.
