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

## RECORDED PHYSICAL-DEVICE OBSERVATION

- **FACT (single recorded observation; device, Android, build, and ChatGPT versions not yet supplied):** The reported baseline was `MODE_NORMAL`, built-in earpiece, and speakerphone off. Starting ChatGPT Voice was temporally correlated with `MODE_IN_COMMUNICATION`, built-in speaker, and speakerphone on. Ending Voice was temporally correlated with return to the reported baseline values.
- **UNKNOWN:** Whether ChatGPT caused each transition. Temporal correlation does not establish ownership or causation.
- **UNKNOWN:** Whether the behavior repeats on the same device or generalizes to any other Android/OEM/application version.
- **FACT (POC-1, device product `2201117TY`; Android, Private Audio build, and ChatGPT versions not recorded):** During an active ChatGPT Voice session, Android reported `MODE_IN_COMMUNICATION` and the built-in speaker. Private Audio called `setCommunicationDevice()` for the built-in earpiece and received `true`, but Android continued to report the built-in speaker. After the communication session ended, Android reported the built-in earpiece and Private Audio cleanup completed.
- **INFERENCE:** A one-shot communication-device request without Private Audio participating in communication mode was insufficient to change the Android-reported route during this POC-1 execution.
- **UNKNOWN:** Audible routing was not recorded, so POC-1 does not establish which device the tester heard before, during, or after the request.
- **FACT (POC-2, device product `2201117TY`; Android, Private Audio build, and ChatGPT versions not recorded):** Private Audio explicitly requested `MODE_IN_COMMUNICATION`, Android reported that mode, and `setCommunicationDevice()` returned `true` for the built-in earpiece. Android nevertheless continued to report the built-in speaker during active ChatGPT Voice and returned to the earpiece after Voice ended.
- **INFERENCE:** On this POC-2 execution, explicit communication-mode participation plus one accepted route request was insufficient to change the Android-reported active-session route.
- **UNKNOWN:** POC-2 audible routing was not recorded and the result is not evidence about other devices or software versions.

## UNKNOWN

- The exact low-level audio attributes used by the current ChatGPT Android application.
- Whether ChatGPT claims or reclaims routing while active.
- Whether an independent process can retain built-in-earpiece routing during current ChatGPT Voice.
- OEM behavior across Android versions.
- Behavior in browsers and browser-based realtime voice sessions.
- Whether the recorded ChatGPT Voice-correlated state transition repeats when POC-1 is run with complete test metadata.
- Whether `setCommunicationDevice()` accepts a request made after Private Audio has been backgrounded while its process remains alive.
- Whether an accepted request produces a subsequently observed earpiece state, and separately whether a human hears ChatGPT Voice move to the earpiece.
- Whether callback delivery continues while the diagnostic activity is backgrounded on a particular physical device; Android may terminate a background process, and PR #4 uses no service or persistence.
- Whether an observed mode, device, or speakerphone change was caused by ChatGPT. Temporal correlation in the event log is not proof of causation.

## Evidence priorities

Future research should prefer:

1. official Android documentation;
2. official OpenAI or Google documentation;
3. recorded physical-device observations; and
4. community reports only as secondary evidence.

New statements must be labeled **FACT**, **INFERENCE**, or **UNKNOWN**, with sources or test records where appropriate. Do not convert an inference to fact without evidence.
