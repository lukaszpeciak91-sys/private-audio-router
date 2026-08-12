# Research Baseline

This baseline separates documented platform capabilities from hypotheses and unanswered questions. It does not establish cross-application compatibility.

## FACT

- Android exposes public communication-device routing APIs.
- Android exposes a built-in earpiece device type on compatible hardware.
- Android allows an application to request a communication device and clear that request.
- Android exposes the currently selected communication device.
- Android provides communication-device change observation.
- `AudioManager.getMode()` publicly exposes the mode value visible to this process without changing it.
- `AudioManager.isSpeakerphoneOn()` is a public read-only observation available across the supported SDK range, although deprecated from API 34; it reports a boolean and does not identify why that state was selected.
- `AudioDeviceCallback` reports audio-device additions and removals while the callback remains registered and the process remains alive. The diagnostic uses these events only as a signal to take another communication-device snapshot.
- Communication-device routing requests are intended for communication use cases.
- Normal telephony has system-level routing responsibilities independent of this project.
- `AudioManager.setCommunicationDevice()`, `getCommunicationDevice()`, `getAvailableCommunicationDevices()`, and `clearCommunicationDevice()` were added in API level 31.
- New Google Play submissions and updates must target Android 16 (API level 36) or higher starting August 31, 2026.

**Source baseline:** Official Android [`AudioManager`](https://developer.android.com/reference/android/media/AudioManager) API reference and [Google Play target API requirements](https://developer.android.com/google/play/requirements/target-sdk). No application-specific compatibility source has yet been established.

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
- Whether a current ChatGPT Voice session changes any state an independent application can observe through these APIs.
- Whether callback delivery continues while the diagnostic activity is backgrounded on a particular physical device; Android may terminate a background process, and PR #4 uses no service or persistence.
- Whether an observed mode, device, or speakerphone change was caused by ChatGPT. Temporal correlation in the event log is not proof of causation.

## Evidence priorities

Future research should prefer:

1. official Android documentation;
2. official OpenAI or Google documentation;
3. recorded physical-device observations; and
4. community reports only as secondary evidence.

New statements must be labeled **FACT**, **INFERENCE**, or **UNKNOWN**, with sources or test records where appropriate. Do not convert an inference to fact without evidence.
