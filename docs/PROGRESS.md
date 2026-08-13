# Progress

State reflects evidence, not aspiration.

## DONE

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

## CURRENT

- **Implementation state: the diagnostic now contains the deliberately temporary POC-2 mode-participation experiment. POC-1 showed that the routing-only request did not change Android's reported route on the tested device; POC-2 physical-device behavior remains NOT TESTED.**

## NEXT

- Run POC-2 on physical Android hardware and record complete device, Android, Private Audio build, and ChatGPT version metadata with the copied report.
- Determine separately whether explicit mode participation is observed, whether Android reports the earpiece after the one-shot request, and whether a human hears ChatGPT Voice move to it.
- Validate explicit disarm, session-end, activity destruction, and real-telephony cleanup paths leave no abnormal system-audio behavior.

## UNKNOWN

- Whether current ChatGPT Voice respects a communication-device request from an independent application.
- Whether ChatGPT immediately overrides such a request.
- Whether Gemini behaves similarly.
- Whether browser-based voice sessions use compatible communication routing.
- OEM-specific routing differences.
- Exact behavior during incoming and outgoing real phone calls.
- Whether a foreground service will eventually be required.
- Whether communication-device callbacks are delivered while this application is backgrounded on tested Android/OEM builds, and whether process lifetime interrupts an experiment.
