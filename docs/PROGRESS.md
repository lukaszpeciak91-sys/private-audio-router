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

## CURRENT

- **Implementation state: application foundation exists; no communication-audio functionality has been implemented.**

## NEXT

- Create the call UI foundation without audio behavior (PR #4).
- Enumerate available communication devices.
- Identify the built-in earpiece.
- Observe the current communication device.
- Add communication-device change diagnostics.
- In a later PR, add explicit built-in-earpiece request and clear operations.

## UNKNOWN

- Whether current ChatGPT Voice respects a communication-device request from an independent application.
- Whether ChatGPT immediately overrides such a request.
- Whether Gemini behaves similarly.
- Whether browser-based voice sessions use compatible communication routing.
- OEM-specific routing differences.
- Exact behavior during incoming and outgoing real phone calls.
- Whether a foreground service will eventually be required.
