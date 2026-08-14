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
- **FACT (POC-3; complete device and software metadata not recorded):** Private Audio made three bounded `setCommunicationDevice()` requests for the built-in earpiece. All returned `true`, but Android never reported the built-in earpiece while ChatGPT Voice remained active.
- **INFERENCE:** Repeating the accepted routing request was insufficient to change the Android-reported route in this POC-3 execution; POC-4 therefore isolates explicit mode-ownership transition rather than adding retries.
- **UNKNOWN:** Whether selecting `MODE_NORMAL` and then becoming the latest application to select `MODE_IN_COMMUNICATION` transfers effective routing priority to Private Audio.
- **FACT (POC-5, Xiaomi `2201117TY`, Android 13/API 33):** Private Audio PID 15176/UID 10322 had a started `USAGE_VOICE_COMMUNICATION`/`CONTENT_TYPE_SPEECH` `AudioTrack`, invoked `setMode(MODE_IN_COMMUNICATION)` without an application-side exception, and then received `true` from its single earpiece request. A live `dumpsys audio` taken before cleanup showed its earpiece route client but no Private Audio mode requester; ChatGPT PID 14241/UID 10325 was the sole active mode requester and its speaker request remained computed, applied, and active.
- **FACT (repository audit):** The source manifest does not declare `android.permission.MODIFY_AUDIO_SETTINGS`, although both `AudioManager.setMode()` and `setCommunicationDevice()` document that permission. No flavors or manifest overlays exist in the repository. The effective installed permission state remains unverified because the merged manifest could not be regenerated in the audit environment.
- **FACT (Android 13 AOSP framework semantics):** `AudioService.setMode()` checks `MODIFY_AUDIO_SETTINGS` and returns without changing requester state when the check fails. For an allowed non-`MODE_NORMAL` request, `setModeInt()` creates or updates a per-PID `SetModeDeathHandler`; it does not skip registration merely because the numeric global mode already matches. `MODE_NORMAL`, binder death, and server-side requester cleanup remove entries. Audio focus, target SDK, foreground status, and UID importance are not acceptance conditions in this Android 13 path; active playback/recording affects requester activity and owner selection rather than initial requester creation. **Source:** Android 13 AOSP [`AudioService`](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-13.0.0_r1/services/core/java/com/android/server/audio/AudioService.java) and public Android [`AudioManager`](https://developer.android.com/reference/android/media/AudioManager) reference.
- **INFERENCE:** The missing source-manifest permission is the leading standard-framework explanation for a silent `setMode()` no-op, but the simultaneously observed successful Private Audio communication-route client means the installed APK/runtime permission state must be captured before attributing this run to that cause. If the installed package held the permission, the observation differs from the audited Android 13 AOSP requester path and requires focused system logging before considering an OEM change.

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
- Whether the POC-5 installed APK declared and was granted `MODIFY_AUDIO_SETTINGS`, and which `AudioService` branch handled its mode request on the Xiaomi Android 13 build.

## Evidence priorities

**FACT:** Public `AudioManager.getActivePlaybackConfigurations()` observations expose an active playback snapshot and public configuration metadata without capturing audio. For this project's public compile-SDK surface, the diagnostic serializes audio usage, content type, allowed capture policy, and device; it does not depend on playback state or session ID. Visibility may be limited by Android, so an empty or incomplete list is not evidence that no playback exists.

**UNKNOWN:** Whether ChatGPT Voice's active playback configuration is visible to Private Audio on the POC-4 device, and which usage/content type Android reports if it is visible.

Future research should prefer:

1. official Android documentation;
2. official OpenAI or Google documentation;
3. recorded physical-device observations; and
4. community reports only as secondary evidence.

New statements must be labeled **FACT**, **INFERENCE**, or **UNKNOWN**, with sources or test records where appropriate. Do not convert an inference to fact without evidence.
