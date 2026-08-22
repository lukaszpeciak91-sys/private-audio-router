# Research Baseline

This baseline separates documented platform capabilities from hypotheses and unanswered questions. It does not establish cross-application compatibility.

## FACT

- `AudioManager.registerAudioPlaybackCallback()` provides callback-based public observation, and `getActivePlaybackConfigurations()` provides a public active snapshot. For each visible configuration the application can read `AudioAttributes` usage, content type, flags and allowed-capture policy, and an `AudioDeviceInfo` (including ID, type, and product name) when Android supplies one. `AudioPlaybackConfiguration` does not publicly expose a per-configuration `isActive` member; configuration presence in the active snapshot is the available observation. These metadata contain no PCM audio. The ordinary public API available to this application does not expose a safe client package/session identity or exact player-state value, so the diagnostic records those fields as unavailable rather than inferring ownership.
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
- A playback configuration that appears only around the audible startup sound may be a candidate for that sound, but attribution requires physical timestamp correlation and cannot be established from usage or timing alone.

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
- **FACT (POC-4, Xiaomi `2201117TY`):** The explicit communication-mode transition experiment did not physically move active ChatGPT Voice from the built-in speaker to the earpiece.
- **FACT (POC-5, Xiaomi `2201117TY`, Android 13/API 33):** Private Audio PID 15176/UID 10322 had a started `USAGE_VOICE_COMMUNICATION`/`CONTENT_TYPE_SPEECH` `AudioTrack`, invoked `setMode(MODE_IN_COMMUNICATION)` without an application-side exception, and then received `true` from its single earpiece request. A live `dumpsys audio` taken before cleanup showed its earpiece route client but no Private Audio mode requester; ChatGPT PID 14241/UID 10325 was the sole active mode requester and its speaker request remained computed, applied, and active.
- **FACT (physical permission and system-log diagnostics, Xiaomi `2201117TY`, Android 13/API 33):** The installed Private Audio APK did not request `android.permission.MODIFY_AUDIO_SETTINGS`. Focused system logcat captured AudioService receiving Private Audio's `setMode(MODE_IN_COMMUNICATION)` call and rejecting it with `Audio Settings Permission Denial: setMode()`. Private Audio therefore did not enter Android's mode-owner stack in the audited POC-5 run.
- **FACT (configuration fix):** The application manifest now requests the public `android.permission.MODIFY_AUDIO_SETTINGS` permission required by the existing `AudioManager` calls. The POC-5 AudioTrack, mode-request, route-request, and cleanup behavior is otherwise unchanged.
- **FACT (Android 13 AOSP framework semantics):** `AudioService.setMode()` checks `MODIFY_AUDIO_SETTINGS` and returns without changing requester state when the check fails. For an allowed non-`MODE_NORMAL` request, `setModeInt()` creates or updates a per-PID `SetModeDeathHandler`; it does not skip registration merely because the numeric global mode already matches. `MODE_NORMAL`, binder death, and server-side requester cleanup remove entries. Audio focus, target SDK, foreground status, and UID importance are not acceptance conditions in this Android 13 path; active playback/recording affects requester activity and owner selection rather than initial requester creation. **Source:** Android 13 AOSP [`AudioService`](https://android.googlesource.com/platform/frameworks/base/+/refs/tags/android-13.0.0_r1/services/core/java/com/android/server/audio/AudioService.java) and public Android [`AudioManager`](https://developer.android.com/reference/android/media/AudioManager) reference.
- **INFERENCE:** AudioService's explicit permission denial explains why the audited POC-5 run never registered Private Audio as a mode requester; that run cannot establish whether an otherwise unchanged, permission-corrected POC-5 can become the actual mode owner or change the cross-application communication route.
- **FACT (later POC-5 success, Xiaomi `2201117TY`, Android 13/API 33):** With ChatGPT package `com.openai.chatgpt` active and Private Audio package `app.privateaudio`, application diagnostics recorded a created and started silent communication `AudioTrack`, visible active `USAGE_VOICE_COMMUNICATION`/`CONTENT_TYPE_SPEECH` playback, the track active before the explicit `MODE_IN_COMMUNICATION` request, no mode-request exception, exactly one subsequent earpiece request, and `setCommunicationDevice()` returning `true`. Android then reported built-in earpiece, speakerphone off, and active voice-communication playback configurations on built-in earpiece/device ID 2 while ChatGPT Voice remained active.
- **FACT (physical audible result):** After a full uninstall and reinstall of the current Private Audio APK, repeated physical tests confirmed that ChatGPT Voice was audible through the phone's upper built-in earpiece. POC-5 therefore has Android-reported PASS, human-audible PASS, and overall PASS on this tested configuration. Earlier failed and inconsistent runs remain valid historical evidence but are superseded for the current configuration by this later result.
- **UNKNOWN:** Full uninstall and reinstall preceded the first repeatable physically audible success; causal relationship is not yet established. In particular, the evidence does not establish stale app state, an old APK or process, Android cache, an old mode requester, or an OEM defect as the cause.

## UNKNOWN

- The exact low-level audio attributes used by the current ChatGPT Android application.
- Whether ChatGPT claims or reclaims routing while active.
- Whether an independent process can retain built-in-earpiece routing during current ChatGPT Voice across repeated sessions, lifecycle events, APK updates, and configurations beyond the successful test.
- OEM behavior across Android versions.
- Whether the same classifiers and POC-5 behavior generalize across OEMs and Android versions, including newer releases.
- Whether all Chromium browsers expose the Chrome-tested signature; Chrome and Mi Browser evidence does not establish this.
- Whether Gecko/Firefox exposes any already supported signature.
- Whether the recorded ChatGPT Voice-correlated state transition repeats when POC-1 is run with complete test metadata.
- Whether `setCommunicationDevice()` accepts a request made after Private Audio has been backgrounded while its process remains alive.
- Why uninstall/reinstall preceded the first reliable audible POC-5 success.
- Whether callback delivery continues while the diagnostic activity is backgrounded on a particular physical device; Android may terminate a background process, and PR #4 uses no service or persistence.
- Whether an observed mode, device, or speakerphone change was caused by ChatGPT. Temporal correlation in the event log is not proof of causation.

## Evidence priorities

**FACT:** Public `AudioManager.getActivePlaybackConfigurations()` observations expose an active playback snapshot and public configuration metadata without capturing audio. For this project's public compile-SDK surface, the diagnostic serializes audio usage, content type, allowed capture policy, and device; it does not depend on playback state or session ID. Visibility may be limited by Android, so an empty or incomplete list is not evidence that no playback exists.

**UNKNOWN:** Whether ChatGPT Voice's active playback configuration is visible to Private Audio on the POC-4 device, and which usage/content type Android reports if it is visible.

Future research should prefer:

1. official Android documentation;
2. official OpenAI or Google documentation;
3. recorded physical-device observations; and
4. community reports only as secondary evidence.

New statements must be labeled **FACT**, **INFERENCE**, or **UNKNOWN**, with sources or test records where appropriate. Do not convert an inference to fact without evidence.

## Layer 1.6 playback-evidence boundary

- **FACT:** Public `AudioManager.AudioPlaybackCallback` reports changes to the active playback-configuration list, including public audio attributes for configurations present in that list; it does not expose a public per-configuration active-state member, and this implementation does not obtain or use a client package or provider identity.
- **INFERENCE:** After an external matching communication playback is visible, Private Audio starts its one known matching silent track. Two matching active contributions then establish external-plus-local evidence; a stable fall to one while the known local track remains playing is treated as external communication ending.
- **UNKNOWN:** OEM callback timing, transient player recreation, whether all compatible providers expose matching configurations, and the reliability of this count boundary across devices remain unverified until physical Layer 1.6 testing. Ambiguous evidence intentionally leaves the active cycle in place rather than escalating.

## Layer 7A public proximity-screen API boundary

- **FACT:** Android exposes `PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK`, `PowerManager.isWakeLockLevelSupported(int)`, non-reference-counted `WakeLock` ownership, and ordinary immediate `WakeLock.release()` through public APIs. The application declares `android.permission.WAKE_LOCK` and does not use a proximity sensor listener.
- **INFERENCE:** Bounding one proximity wake lock to the service's current `ACTIVE`/communication-mode/built-in-earpiece evidence should provide call-like repeated acquire/release ownership without coupling behavior to Main or Floating visibility.
- **FACT (2026-08-18):** On Xiaomi `2201117TY`, Android 13/API 33, while Private Audio was `ACTIVE` on the built-in earpiece, moving the phone near turned the screen off and moving it away restored the screen automatically. `PROXIMITY_SCREEN_OFF_WAKE_LOCK` support reported `true`; a supplied post-session report reported the wake lock currently held as `false`.
- **FACT:** Layer 7B adds one service-owned, persisted, default-ON preference to the same eligibility decision. It is a user opt-out for Private Audio's proximity wake-lock ownership only; it does not participate in routing, session inference, audio mode, or product-state projection.
- **FACT (tester report, 2026-08-18):** After Layer 7B and its compile fix, the tester reported that the implemented behavior worked as intended. No per-case observations accompanied that statement, so it does not establish PASS evidence for any individual boundary beyond the explicitly recorded near/far result.
- **UNKNOWN:** That post-session held value does not identify or verify a particular release path. Session-end-while-near, successive cycles, Power OFF, Main Close, Floating transitions, preference changes/persistence, non-earpiece routes, telephony, physical Power-button behavior, and process/service termination remain unverified. Automated tests cannot establish these OEM behaviors.

## Assistant-class playback evidence

- **FACT (supplied physical result, 2026-08-19):** Gemini Live exposed `USAGE_ASSISTANT` + `CONTENT_TYPE_SPEECH` from initial `MODE_NORMAL`; the protected POC-5 sequence produced human-confirmed audible speaker-to-earpiece routing, proximity behavior, session-end cleanup, `ACTIVE` → `WAITING`, and controller re-arm on the tested device.
- **FACT (supplied physical result, 2026-08-19):** The established communication path passed with ChatGPT Voice, Grok, Perplexity, and Character.AI. A regression with the assistant experimental option disabled confirmed the existing ChatGPT path still worked before automatic promotion.
- **INFERENCE:** Exact public usage/content classification provides a provider-independent boundary for the two trigger classes without package, UID, PID, or foreground identity.
- **UNKNOWN:** The supplied results do not establish physical no-trigger outcomes for YouTube/music/video, NotebookLM Audio Overview, Kimi media-style TTS, or assistant sonification alone; implementation contracts preserve those exclusions pending recorded device runs.
- **FACT (supplied physical diagnostic, 2026-08-21):** With Fake Phone pre-arm OFF, one Gemini assistant/speech cycle established `MODE_IN_COMMUNICATION`, the built-in earpiece, `ACTIVE`, and proximity. After assistant/speech disappeared, the 1.5-second confirmation completed and Private Audio cleared the device, relinquished mode, released its silent track, returned to `WAITING`, and released proximity. No later qualifying assistant/speech playback appeared before the report was captured during the failed next turn; the post-cleanup state was otherwise clean.
- **INFERENCE:** Retaining the already-established protected communication context across a bounded conversational gap may avoid destabilization associated with teardown and reacquisition.
- **FACT (supplied physical result, 2026-08-21):** A Gemini assistant/speech contribution resumed about 1.4 seconds into the 5-second linger. Private Audio cancelled the linger and reused the same protected cycle with `MODE_IN_COMMUNICATION`, the built-in earpiece, a playing silent track, `ACTIVE`, and one routing attempt.
- **FACT (supplied physical result, 2026-08-21):** In several earlier turns, the 5-second linger expired and completed cleanup only about 0.6–0.7 seconds before the next assistant/speech contribution appeared. This is evidence that 5 seconds was slightly too short on the tested Gemini configuration.
- **INFERENCE:** A 7-second linger provides margin over the observed misses while remaining substantially shorter than the original 15-second experiment.
- **UNKNOWN:** The observed timing does not prove cleanup caused any failed Gemini turn. The tuned 7-second Gemini gate plus unchanged ChatGPT, browser, and immediate-cleanup checks remain required.

## Browser-communication playback evidence

- **FACT (supplied physical result, 2026-08-19):** ChatGPT Web in Chrome exposed the validated `MODE_IN_COMMUNICATION` + built-in speaker + `USAGE_VOICE_COMMUNICATION`/`CONTENT_TYPE_UNKNOWN` signature, and the protected POC-5 sequence produced human-confirmed audible earpiece routing on the tested device.
- **FACT (supplied physical result, 2026-08-19):** Browser session end completed the existing 1.5-second confirmation and reversible cleanup, and subsequent browser sessions routed without restarting Private Audio. ChatGPT Android and Gemini Live regressions passed; each cycle made exactly one routing request, ordinary media and assistant sonification remained ignored, and Power OFF cleanup remained unchanged.
- **FACT (supplied physical result, 2026-08-19):** ChatGPT Web in Mi Browser also produced normal Private Audio behavior and human-confirmed earpiece routing on the same Xiaomi Android 13/API 33 device. Exact Mi Browser metadata was not supplied.
- **INFERENCE:** Exact public mode, route, usage, and content classification provides a provider-independent third trigger boundary without identifying Chrome, a URL, a package, or a client.
- **UNKNOWN:** These Chrome and Mi Browser results do not establish that every Chromium browser exposes identical metadata, that Gecko/Firefox follows a supported signature, or that another OEM or Android release behaves identically.

## Fake Phone pre-arm experimental basis

- **FACT:** Repeated supplied Xiaomi Android 13 diagnostics report `USAGE_ASSISTANT` + `CONTENT_TYPE_SONIFICATION` on the built-in speaker before later `USAGE_ASSISTANT` + `CONTENT_TYPE_SPEECH` evidence begins protected routing.
- **FACT:** Public `AudioPlaybackConfiguration` metadata used here does not identify that playback as owned by ChatGPT, Gemini, or another particular application.
- **INFERENCE:** Preparing the proven public communication context and earpiece route while `WAITING` may influence the later sonification route; the default-OFF experiment records the public device/mode observations without muting or changing global volume.
- **UNKNOWN:** Whether the observed event is the audible startup ping and whether it is physically heard from the earpiece while pre-armed. Android metadata and `setCommunicationDevice(true)` are insufficient; Xiaomi physical listening confirmation is required.

## Assistant early silent-track experimental basis

- **FACT (supplied physical diagnostic, 2026-08-22):** On the Xiaomi test device, `VOICE_RECOGNITION` appeared at 09:37:41.093, assistant/sonification at 09:37:41.302, assistant/speech at 09:37:48.568, the protected trigger at 09:37:48.664, `play()` at 09:37:48.673, and `PLAYSTATE_PLAYING` at 09:37:49.023.
- **FACT (supplied physical diagnostic, 2026-08-22):** After the local track became active, public post-track-start playback already reported assistant/speech on the earpiece while `AudioManager` still reported `MODE_NORMAL`.
- **INFERENCE:** On this OEM, active local voice-communication/speech playback may influence output policy before Private Audio explicitly requests mode or a communication device. That policy response is not itself a Private Audio route request.
- **UNKNOWN:** Whether starting only the local silent track during the earlier recording-plus-sonification window preserves recognition stability and materially reduces first-response clipping. The default-OFF experiment requires the controlled physical gate in `TEST_PLAN.md`.
