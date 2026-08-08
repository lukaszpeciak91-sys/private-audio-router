# Research Baseline

This baseline separates documented platform capabilities from hypotheses and unanswered questions. It does not establish cross-application compatibility.

## FACT

- Android exposes public communication-device routing APIs.
- Android exposes a built-in earpiece device type on compatible hardware.
- Android allows an application to request a communication device and clear that request.
- Android exposes the currently selected communication device.
- Android provides communication-device change observation.
- Communication-device routing requests are intended for communication use cases.
- Normal telephony has system-level routing responsibilities independent of this project.

**Source baseline:** Add direct references to the relevant official Android `AudioManager` and `AudioDeviceInfo` documentation during implementation research. No application-specific compatibility source has yet been established.

## INFERENCE

- Modern AI voice applications are likely to use communication-oriented audio patterns because they operate as realtime bidirectional voice sessions.
- ChatGPT Voice appears likely to use communication-style routing based on observed behavior and platform conventions.
- If the system respects an external application's communication-device request, the approach may work across multiple AI voice applications.

These inferences are hypotheses for physical-device testing, not compatibility claims.

## UNKNOWN

- The exact low-level audio attributes used by the current ChatGPT Android application.
- Whether ChatGPT claims or reclaims routing while active.
- Whether an independent process can retain built-in-earpiece routing during current ChatGPT Voice.
- OEM behavior across Android versions.
- Behavior in browsers and browser-based realtime voice sessions.

## Evidence priorities

Future research should prefer:

1. official Android documentation;
2. official OpenAI or Google documentation;
3. recorded physical-device observations; and
4. community reports only as secondary evidence.

New statements must be labeled **FACT**, **INFERENCE**, or **UNKNOWN**, with sources or test records where appropriate. Do not convert an inference to fact without evidence.
