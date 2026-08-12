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

## CURRENT

- **Implementation state: the diagnostic observer can read communication-audio state but contains no routing request or other audio manipulation. Physical-device behavior is not yet tested.**

## NEXT

- Run the PR #4 baseline / ChatGPT Voice / recovery experiment on physical Android hardware and record the results.
- Determine from evidence whether another application's voice session produces observable communication-device, mode, speakerphone, or device-list changes.
- In a later PR, add explicit built-in-earpiece request and clear operations.

## UNKNOWN

- Whether current ChatGPT Voice respects a communication-device request from an independent application.
- Whether ChatGPT immediately overrides such a request.
- Whether Gemini behaves similarly.
- Whether browser-based voice sessions use compatible communication routing.
- OEM-specific routing differences.
- Exact behavior during incoming and outgoing real phone calls.
- Whether a foreground service will eventually be required.
- Whether communication-device callbacks are delivered while this application is backgrounded on tested Android/OEM builds, and whether process lifetime interrupts an experiment.
