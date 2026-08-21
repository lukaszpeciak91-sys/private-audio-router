# Hardware-in-the-Loop Test Plan

Allowed status values are **NOT TESTED**, **PASS**, **FAIL**, and **BLOCKED**. Record a specific device, Android version, and application build for every execution. Emulator results are insufficient for built-in-earpiece routing or cross-application compatibility; those claims require a physical-device test.

## Current compatibility and remaining validation

Unless a row says otherwise, current PASS evidence is supplied physical testing on the primary Xiaomi product `2201117TY`, Android 13/API 33. It must not be generalized to another device, OEM, Android release, browser engine, or application version. Historical POC-1/2/3/4 failures below remain valid evidence; the later POC-5 PASS and automatic three-class controller supersede those experiments only for the current tested configuration.

### Confirmed compatibility matrix

| Surface | Trigger/evidence | Status | Confirmed scope |
| --- | --- | --- | --- |
| ChatGPT Android | `COMMUNICATION` | **PASS** | Automatic detection, audible earpiece route, proximity, Power OFF/cleanup, session end → Waiting, and a later session. Remained working after assistant and browser promotion. |
| Gemini Live | `ASSISTANT`: `USAGE_ASSISTANT` + `CONTENT_TYPE_SPEECH`, initially `MODE_NORMAL` | **PASS** | Automatic protected POC-5, audible speaker → earpiece, proximity, cleanup, Active → Waiting, re-arm, and later sessions. Remained working after browser promotion. |
| Grok Android | `COMMUNICATION` | **PASS** | Physically confirmed with the established communication-class route. |
| Perplexity Android | `COMMUNICATION` | **PASS** | Physically confirmed with the established communication-class route. |
| Character.AI Calls | `COMMUNICATION` | **PASS** | Physically confirmed with the established communication-class route. |
| ChatGPT Web in Chrome | `BROWSER_COMMUNICATION`: `MODE_IN_COMMUNICATION` + built-in speaker + `USAGE_VOICE_COMMUNICATION`/`CONTENT_TYPE_UNKNOWN` | **PASS** | Automatic single-request POC-5, audible earpiece, proximity, 1.5-second transient-recreation tolerance, cleanup, `MODE_NORMAL`, Active → Waiting, and later sessions without restarting Private Audio. Remained automatic after removal of the experimental switch. |
| ChatGPT Web in Mi Browser | Browser communication; exact metadata not supplied | **PASS** | Tester confirmed normal Private Audio behavior and audible earpiece routing. This does not prove all Chromium browsers. |
| Perplexity Web | No realtime Voice mode on the tested web surface | **BLOCKED** | Not applicable to routing: no routable realtime voice surface was available; this is not a Private Audio failure. |
| ChatGPT Web Voice in Opera | Realtime Voice session could not be started | **BLOCKED** | Voice could not be loaded during the attempted check, so Private Audio was not exercised and did not fail. Retry only if the surface becomes available. |

Every confirmed routing cycle retains one `setCommunicationDevice(earpiece)` request, reversible cleanup, and Waiting re-arm. Ordinary `USAGE_MEDIA` and assistant `CONTENT_TYPE_SONIFICATION` without speech remain outside the automatic classifiers. The user reported that local `./gradlew testDebugUnitTest lintDebug assembleDebug` passed after the earlier remote Gradle-download limitation; that supplied automated result is not physical OEM evidence and was not executed by Codex for this reconciliation.

### Remaining physical validation priorities

**HIGH — device and release safety**

1. Samsung/One UI: routing, cleanup, proximity, and all three trigger families.
2. Pixel or another AOSP-like device: separate framework behavior from Xiaomi-specific behavior.
3. A newer Android release than Android 13, preferably Android 15/16 when available.
4. Incoming real phone call while armed and while Active: telephony must win immediately.
5. Outgoing real phone call while armed and while Active: telephony must win immediately.

The two telephony cases are release-safety gates, not optional compatibility polish.

**MEDIUM — accessories and lifecycle**

1. Bluetooth headset/earbuds connected before and during use.
2. Wired/USB audio where available.
3. Service/process termination during Active routing.
4. Device reboot after prior participation.
5. Session end while proximity screen-off behavior is active/near ear.

**Browser-engine coverage**

- High value: Firefox Android (Gecko) and Samsung Internet (OEM/browser integration).
- Optional/later: retry Opera after an update if Voice becomes available; Edge, Brave, and other Chromium browsers are lower priority after Chrome and Mi Browser, unless demand or reports justify testing.
- Chrome plus Mi Browser PASS does not guarantee other Chromium browsers.

**Additional AI application coverage**

- Already confirmed: ChatGPT, Gemini Live, Grok, Perplexity Android, and Character.AI Calls.
- Future checks only when an actual realtime voice mode is available: Microsoft Copilot, Pi/hands-free conversation, DeepSeek realtime voice, Meta AI voice, and other materially popular applications. Do not buy subscriptions only to expand this matrix; unavailable, paywalled, or region-blocked surfaces are **BLOCKED** or **NOT TESTED**, not Private Audio failures.

**Physical no-trigger obligations**

- **NOT TESTED / UNKNOWN unless a separate execution below records otherwise:** ordinary music/video, YouTube, ordinary browser media, NotebookLM Audio Overview/podcast-style playback, Kimi media-style TTS, and `ASSISTANT` + `CONTENT_TYPE_SONIFICATION` without speech.
- Automated classifier contracts are useful structural evidence, not physical OEM evidence.
- T-014A's ChatGPT Voice startup-sound/startup-leak investigation remains unresolved; the bounded STARTUP AUDIO TRACE is diagnostic evidence only and normal routed speech does not establish that the startup sound is fixed.

## T-001 — Idle state

- **Status:** NOT TESTED
- **Device:** Not recorded
- **Android version:** Not recorded
- **Build:** Not recorded
- **Preconditions:** Utility is disabled; disconnect optional audio accessories.
- **Steps:** Play ordinary phone audio and inspect the current system routing before enabling the utility.
- **Expected result:** Normal phone audio behavior is unchanged and a baseline is recorded.
- **Observed result:** Not recorded
- **Notes:** Establish the baseline before every routing test session.

## T-002 — Detect built-in earpiece

- **Status:** NOT TESTED
- **Device:** Not recorded
- **Android version:** Not recorded
- **Build:** Not recorded
- **Preconditions:** Run the diagnostic build on physical hardware expected to contain an earpiece.
- **Steps:** Enumerate available communication devices and inspect their reported types.
- **Expected result:** Android exposes a communication device identified as the built-in earpiece on compatible hardware.
- **Observed result:** Not recorded
- **Notes:** Absence may be device-specific; record rather than infer.

## T-003 — Request earpiece

- **Status:** NOT TESTED
- **Device:** Not recorded
- **Android version:** Not recorded
- **Build:** Not recorded
- **Preconditions:** T-002 identifies a built-in earpiece; no real phone call is active.
- **Steps:** Issue the explicit built-in-earpiece routing request; record the API result and observed current communication device.
- **Expected result:** Android reports whether the request succeeds, and diagnostics accurately show the resulting routing state.
- **Observed result:** Not recorded
- **Notes:** API success and audible routing are separate observations.

## T-004 — Clear request

- **Status:** NOT TESTED
- **Device:** Not recorded
- **Android version:** Not recorded
- **Build:** Not recorded
- **Preconditions:** A routing request from T-003 is active.
- **Steps:** Clear the request; observe the current communication device and repeat baseline audio checks.
- **Expected result:** The utility relinquishes its routing influence and normal system routing is restored.
- **Observed result:** Not recorded
- **Notes:** Any unexpected persistence is a failure requiring documentation.

## T-005 — ChatGPT Voice active

- **Status:** PASS
- **Device:** Xiaomi product `2201117TY`
- **Android version:** Android 13/API 33
- **Build:** Not recorded
- **Preconditions:** Current ChatGPT Android application is installed; an active voice session is available; baseline and privacy-safe test content are prepared.
- **Steps:** Start ChatGPT Voice, issue the routing request, observe the reported communication device, and determine the audible output device.
- **Expected result:** The test determines—without assuming—whether ChatGPT output uses the built-in earpiece after the request.
- **Observed result:** Supplied physical results confirm automatic detection, Android-reported and audible built-in-earpiece routing, proximity, cleanup, Waiting re-arm, and a subsequent session. See the current matrix and POC-5 history.
- **Notes:** Record ChatGPT version. A non-earpiece result is valid evidence, not authorization to escalate.

## T-006 — ChatGPT background voice

- **Status:** NOT TESTED
- **Device:** Not recorded
- **Android version:** Not recorded
- **Build:** Not recorded
- **Preconditions:** ChatGPT Voice can continue while another application is foregrounded.
- **Steps:** Begin a voice session, foreground the utility, issue the request, and observe reported and audible routing.
- **Expected result:** The test determines whether routing is respected while ChatGPT Voice continues in the background.
- **Observed result:** Not recorded
- **Notes:** Record all application versions and lifecycle transitions.

## T-007 — App process terminated

- **Status:** NOT TESTED
- **Device:** Not recorded
- **Android version:** Not recorded
- **Build:** Not recorded
- **Preconditions:** The utility has an active routing request.
- **Steps:** Terminate its process using a documented method; inspect routing and repeat baseline audio checks.
- **Expected result:** Routing does not remain in an unsafe or unexpected persistent state.
- **Observed result:** Not recorded
- **Notes:** Record the exact termination method.

## T-008 — Phone reboot

- **Status:** NOT TESTED
- **Device:** Not recorded
- **Android version:** Not recorded
- **Build:** Not recorded
- **Preconditions:** Exercise a routing request before reboot.
- **Steps:** Reboot the phone without manually restoring routing; after startup, inspect routing and baseline audio behavior.
- **Expected result:** No routing modification from the utility survives reboot.
- **Observed result:** Not recorded
- **Notes:** The project must not introduce boot persistence during M0.

## T-009 — Incoming real phone call

- **Status:** NOT TESTED
- **Device:** Not recorded
- **Android version:** Not recorded
- **Build:** Not recorded
- **Preconditions:** A routing request is active; a safe method to place an incoming test call is available.
- **Steps:** Receive and answer a real phone call; observe telephony routing during and after the call.
- **Expected result:** Telephony retains priority; the utility does not attempt to override call routing; post-call behavior is safe.
- **Observed result:** Not recorded
- **Notes:** Stop immediately if normal call behavior is impaired.

## T-010 — Outgoing real phone call

- **Status:** NOT TESTED
- **Device:** Not recorded
- **Android version:** Not recorded
- **Build:** Not recorded
- **Preconditions:** A routing request is active; a safe test number is available.
- **Steps:** Place an outgoing real phone call; observe telephony routing during and after the call.
- **Expected result:** Telephony retains priority; the utility does not attempt to override call routing; post-call behavior is safe.
- **Observed result:** Not recorded
- **Notes:** Stop immediately if normal call behavior is impaired.

## T-011 — Bluetooth device connected

- **Status:** NOT TESTED
- **Device:** Not recorded
- **Android version:** Not recorded
- **Build:** Not recorded
- **Preconditions:** A supported Bluetooth audio device is paired and available.
- **Steps:** Connect Bluetooth, observe communication devices, exercise request and clear operations, and record Android's selection behavior.
- **Expected result:** Behavior is observed accurately and the utility makes no unsafe priority or override assumptions.
- **Observed result:** Not recorded
- **Notes:** Record Bluetooth device type and connection profile where visible.

## T-012 — Gemini voice

- **Status:** PASS
- **Device:** Xiaomi product `2201117TY`
- **Android version:** Android 13/API 33
- **Build:** Not recorded
- **Preconditions:** Current Private Audio build and a current Gemini Live voice experience are available.
- **Steps:** With Private Audio ON, establish a Gemini Live session, observe automatic detection and reported/audible routing, end the session, and verify cleanup and re-arm.
- **Expected result:** Exact assistant/speech playback enters the protected POC-5 path, routes audibly to the earpiece, and restores safely.
- **Observed result:** Supplied physical results confirm `USAGE_ASSISTANT` + `CONTENT_TYPE_SPEECH` from initial `MODE_NORMAL`, automatic protected POC-5, audible speaker-to-earpiece routing, proximity, cleanup, Waiting re-arm, and subsequent sessions.
- **Notes:** Record the Gemini version in future executions; this result is scoped to the tested device/version.

## T-013 — Browser realtime voice

- **Status:** PASS
- **Device:** Xiaomi product `2201117TY`
- **Android version:** Android 13/API 33
- **Build:** Not recorded
- **Preconditions:** Current Private Audio build and a privacy-safe browser realtime voice session are available.
- **Steps:** With Private Audio ON, establish a browser voice session, observe automatic detection and reported/audible routing, end the session, and verify cleanup and re-arm.
- **Expected result:** A supported browser-communication signature enters the protected POC-5 path, routes audibly to the earpiece, and restores safely.
- **Observed result:** Supplied physical results confirm ChatGPT Web Voice in Chrome automatically used the browser-communication path with audible earpiece routing, proximity, cleanup, Waiting re-arm, and subsequent sessions. Mi Browser separately passed normal behavior and audible earpiece routing. Perplexity Web had no applicable realtime Voice surface, and Opera Voice could not be started; see the current matrix.
- **Notes:** Browser versions were not supplied. Do not generalize Chrome and Mi Browser to every Chromium browser or to Gecko.

## T-014 — Observe a cross-application ChatGPT Voice session

- **Status:** NOT TESTED
- **Device:** Not recorded
- **Android version:** Not recorded
- **Build:** Not recorded
- **Preconditions:** Install the PR #4 diagnostic build and record the ChatGPT Android application version. Disconnect or explicitly record attached audio accessories. Do not terminate Private Audio during the sequence.
- **Steps:** (1) Open Private Audio and press **Refresh / Record Snapshot** to record BASELINE. (2) Leave Private Audio without force-stopping it. (3) Open ChatGPT and start ChatGPT Voice. (4) Return to Private Audio and press **Refresh / Record Snapshot**; record current state and all retained callback events. (5) Leave Private Audio, end ChatGPT Voice, and return. (6) Press **Refresh / Record Snapshot** again; compare the result with BASELINE. Record whether the process/log survived each background interval.
- **Expected result:** The test records exactly which mode, current communication device, available communication devices, and speakerphone boolean Android exposes before, during, and after the voice session. It does not assume that any value changes or that temporal correlation establishes causation.
- **Observed result:** Not recorded
- **Notes:** Callback observation is best effort while the process lives. A foreground service is deliberately absent; if Android kills the process, record that lifecycle limitation rather than treating missing events as unchanged audio state. This test performs no routing, recording, or audio capture.

## T-014A — Characterize the ChatGPT Voice startup sound

- **Status:** PENDING PHYSICAL TEST / UNKNOWN
- **Device target:** Xiaomi `2201117TY`; record Android/build, ChatGPT, and Private Audio versions with the result.
- **Steps:** (1) Launch Private Audio. (2) Power ON and treat the resulting `Controller ON` trace entry as the fresh-run marker; older bounded in-memory entries may remain and must be ignored for this run. (3) Open ChatGPT. (4) Start Voice. (5) Listen specifically for the short initial sound still heard from the main speaker and note its wall-clock time as precisely as practical. (6) Continue until normal ChatGPT voice is audibly routed through the earpiece. (7) End Voice. (8) Return to Private Audio. (9) Copy the full diagnostic report. (10) In `STARTUP AUDIO TRACE`, compare millisecond timestamps around the first audible startup sound, the `MODE_IN_COMMUNICATION` observation, routing-cycle start, silent Private Audio `VOICE_COMMUNICATION` `AudioTrack` start, explicit Private Audio mode request, communication-device request, `ACTIVE`/earpiece evidence, first main ChatGPT voice playback, and cleanup. (11) Preserve every overlapping playback entry and record exact usage, content type, flags, and output device; use configuration appearance/disappearance in the active snapshot rather than claiming a per-configuration active status, and do not assign startup-chime, ChatGPT, or Private Audio ownership unless independent evidence supports it.
- **Expected diagnostic result:** The bounded callback trace shows whether Android exposes a distinct playback appearance/disappearance or attribute/device change near the audible startup sound and preserves its relation to the protected routing sequence. It may legitimately show insufficient differentiation.
- **Evidence discipline:** Playback metadata and callback times are **FACT** for this run. Candidate attribution from temporal correlation is **INFERENCE**. The startup sound's owner/category, the main-voice entry, and indistinguishable `VOICE_COMMUNICATION` entries remain **UNKNOWN** unless the runtime evidence separates them.

## POC-1 — ChatGPT Voice earpiece request

- **Status:** FAIL
- **Device:** Product `2201117TY`; other device metadata not recorded
- **Android version:** Not recorded
- **Build:** PR #5 build, exact commit not yet recorded
- **ChatGPT version:** Not recorded
- **Preconditions:** Disconnect or record audio accessories. Confirm no real phone call is active. Private Audio shows the baseline and an available built-in earpiece.
- **Steps:** (1) Launch Private Audio. (2) Confirm and record baseline. (3) Press **Arm Earpiece Test**. (4) Switch to ChatGPT. (5) Start Voice. (6) Confirm ChatGPT initially uses the main speaker. (7) Remain in ChatGPT and listen for an automatic transition to the earpiece. (8) Speak with ChatGPT briefly if routing changes. (9) End ChatGPT Voice. (10) Return to Private Audio. (11) Confirm cleanup and state recovery. (12) Press **Copy Report** and retain the complete plain-text report.
- **Expected result:** The report establishes whether the trigger occurred, whether the single `setCommunicationDevice()` call returned true or false, whether Android subsequently reported the earpiece, whether the human tester audibly confirmed ChatGPT moved to the earpiece, whether ending Voice restored normal state, and whether any abnormal system-audio behavior remained.
- **Observed result:** ChatGPT entered `MODE_IN_COMMUNICATION` and selected the built-in speaker. Private Audio called `setCommunicationDevice(Built-in earpiece)` and the API returned `true`, but Android continued to report the built-in speaker during the active session. After the session ended, Android returned to the built-in earpiece and cleanup completed. Audible routing was not recorded.
- **Notes:** The Android-reported route did not meet the expected earpiece result, so this execution is FAIL even though audible confirmation is unknown. API acceptance, an observed Android device, and audible cross-application routing remain separate evidence. This failure authorizes only the separately decided POC-2 experiment, not retries or an invasive workaround.

## POC-2 — ChatGPT Voice communication-mode participation

- **Status:** FAIL
- **Device:** Product `2201117TY`; other device metadata not recorded
- **Android version:** Not recorded
- **Build:** POC-2 build, exact commit not yet recorded
- **ChatGPT version:** Not recorded
- **Preconditions:** Disconnect or record audio accessories; confirm no real phone call is active; verify Private Audio reports an available built-in earpiece. Prepare to copy the report and manually record audible output.
- **Steps:** (1) Launch Private Audio and record baseline. (2) Press **Arm Earpiece Test**. (3) Switch to ChatGPT and start Voice. (4) Wait until Android reports `MODE_IN_COMMUNICATION` with the built-in speaker. (5) Allow the armed experiment to record pre-change state, request `MODE_IN_COMMUNICATION`, record the resulting mode/device/speakerphone state and callbacks, and issue its single earpiece request. (6) Record whether Android reports the earpiece and whether ChatGPT is audibly heard through it. (7) End Voice and return to Private Audio. (8) Confirm cleanup, copy the report, and verify normal audio behavior. (9) Repeat separate safety runs using **Disarm / Clear**, activity destruction, and an incoming or outgoing real call; stop immediately if telephony is impaired.
- **Expected result:** During active ChatGPT Voice, Android changes the reported communication device from the built-in speaker to the built-in earpiece after Private Audio requests communication-mode participation and makes one routing request. The report distinguishes the mode request, mode before/after, routing API result, reported route, speakerphone state, callback transitions, cleanup, and human-confirmed audible route.
- **Observed result:** Private Audio requested `MODE_IN_COMMUNICATION`, Android reported `MODE_IN_COMMUNICATION`, and the earpiece routing request returned `true`. Android continued to report the built-in speaker throughout active ChatGPT Voice and returned to the built-in earpiece after Voice ended.
- **Notes:** The unchanged active-session route makes this execution FAIL. Audible output was not recorded. API acceptance, an observed Android device, and audible cross-application routing remain separate evidence. This result authorizes only D-012's bounded POC-3 experiment.

## POC-3 — ChatGPT Voice bounded earpiece reassertion

- **Status:** FAIL
- **Device:** Not recorded
- **Android version:** Not recorded
- **Build:** POC-3 build, exact commit not yet recorded
- **ChatGPT version:** Not recorded
- **Preconditions:** Disconnect or record audio accessories; confirm no real phone call is active; verify an available built-in earpiece; prepare to retain the copied report and manually record audible output.
- **Steps:** (1) Launch Private Audio and record baseline. (2) Press **Arm Earpiece Test**. (3) Start ChatGPT Voice and wait for Android to report `MODE_IN_COMMUNICATION` with the built-in speaker. (4) Allow Private Audio to record complete pre-test state, participate in communication mode, and make its initial earpiece request. (5) If Android still reports the speaker, allow the experiment to make at most two further attempts, each only after its 750 ms controlled delay and eligibility recheck. (6) Keep ChatGPT Voice active while recording whether Android ever reports the earpiece, which attempt preceded that change, whether it reverts to speaker, and whether the tester audibly hears output move to the earpiece. (7) End Voice, return to Private Audio, confirm cleanup, and retain **Copy Report** output. (8) In separate safety executions, disarm while a retry is pending, leave communication mode, destroy the activity, and introduce a real incoming or outgoing call; verify no delayed attempt runs afterward and telephony retains priority.
- **Expected result:** While ChatGPT Voice remains active, Android reports built-in speaker → built-in earpiece and the human tester confirms ChatGPT audio moved to the phone earpiece. The copied report contains no more than three attempts and records each trigger, timestamp, mode, device before/immediately after, API result, speakerphone state, callbacks, first successful reported attempt, and any later speaker reversion.
- **Observed result:** All three bounded calls returned `true`, but Android never reported the built-in earpiece while ChatGPT Voice remained active.
- **Notes:** `setCommunicationDevice()` returning `true` is not success. Complete device/software metadata and audible confirmation were not supplied. The preserved failure motivated separately authorized POC-4; no additional retries are permitted.

## POC-4 — Explicit communication-mode ownership transition

- **Status:** FAIL
- **Device:** Xiaomi `2201117TY`
- **Android version:** Android 13 / API 33
- **Build:** POC-4 build, exact commit not yet recorded
- **ChatGPT version:** Not recorded
- **Preconditions:** Disconnect or record audio accessories; confirm no real phone call is active; verify Android reports an available built-in earpiece; prepare to retain the copied report and manually record both audible output and any temporary interruption.
- **Steps:** (1) Launch Private Audio, record baseline, and press **Arm Earpiece Test**. (2) Start ChatGPT Voice and wait for `MODE_IN_COMMUNICATION`, built-in speaker, and an available built-in earpiece. (3) Allow Private Audio to record complete state, request `MODE_NORMAL`, verify/log the reported mode, immediately request `MODE_IN_COMMUNICATION`, and verify/log the reported mode. (4) After communication mode is re-established, allow exactly one `setCommunicationDevice(Built-in earpiece)` call. (5) Keep Voice active through the short observation period and record all callbacks, mode/device/speakerphone changes, the API return, immediate device, delayed device, active playback usage/content type, whether speaker is reclaimed, audible route, and any Voice interruption. (6) While ChatGPT Voice remains active and after the POC-4 routing attempt, run `adb shell dumpsys audio > audio-poc4.txt` from the connected computer. (7) End Voice, return to Private Audio, confirm cleanup, and retain **Copy Report** output. (8) Correlate the report's Private Audio PID/UID, Android-reported route and active playback usage/content type, the dump's AudioService mode-owner information, and audible human confirmation. (9) In separate safety executions, disarm during pending observation, destroy the activity, and introduce a real incoming or outgoing call; verify pending work is cancelled and telephony retains priority.
- **Expected result:** While ChatGPT Voice remains active, Android reports built-in speaker → the explicit `MODE_NORMAL` → `MODE_IN_COMMUNICATION` transition → built-in earpiece. The report explicitly records whether the transition was attempted, whether each requested mode was observed, request acceptance, active-session earpiece observation, later speaker reclamation, and that audible outcome requires human confirmation.
- **Observed result:** The explicit mode-transition experiment produced no physical route change while ChatGPT Voice remained active.
- **Notes:** Preserve this failed historical result. It motivated D-014/POC-5; it does not authorize retries or an invasive workaround.

## POC-5 — Silent active communication requester

- **Status:** PASS (Android-reported PASS; human-audible PASS)
- **Device:** Xiaomi `2201117TY`
- **Android version:** Android 13 / API 33
- **Build:** POC-5 build, exact commit not yet recorded
- **ChatGPT version:** Not recorded
- **Preconditions:** Use physical hardware with a built-in earpiece. Disconnect or record audio accessories; confirm no real phone call is active; verify Private Audio reports an available built-in earpiece. Prepare to retain the copied report, capture `dumpsys audio`, and manually record audible output and any interruption.
- **Steps:** (1) Launch Private Audio, record the baseline, and press **Arm Earpiece Test**. (2) Start ChatGPT Voice and confirm it remains active while Android reports `MODE_IN_COMMUNICATION`, built-in speaker, and an available built-in earpiece. (3) Allow Private Audio to record PRE-POC5, create and start its silent mono PCM `USAGE_VOICE_COMMUNICATION`/`CONTENT_TYPE_SPEECH` `AudioTrack`, and record the track play state plus visible active playback configurations. (4) After the track is playing, allow the single `MODE_IN_COMMUNICATION` request and exactly one `setCommunicationDevice(Built-in earpiece)` call. (5) Keep ChatGPT Voice active for the approximately one-second observation without making further routing requests. Record the returned boolean, mode, communication device, speakerphone state, playback configurations, whether the earpiece appears, whether the speaker is reclaimed, any observable ChatGPT playback device, any temporary glitch, and whether ChatGPT is actually audible through the phone earpiece. (6) Before cleanup, while Voice and the silent track remain active and after the route request, run `adb shell dumpsys audio > audio-poc5.txt`; correlate mode-owner PID/stack, route clients, active playback configurations, and selected route with the report's Private Audio PID/UID. (7) End Voice or disarm, then verify pending observation is cancelled, the route is cleared, mode participation is relinquished, the writer stops, the track is stopped/flushed/released, and no silent playback remains active. Copy and retain the report. (8) In separate safety runs, disarm promptly, destroy the activity, allow ChatGPT communication to end, induce track failure where practical, and introduce a real incoming or outgoing call; verify no writer or route action survives cleanup and telephony retains priority.
- **Expected result:** Success requires both (a) Android reports the built-in earpiece while ChatGPT Voice remains active after the one routing request and (b) the human tester confirms ChatGPT audio is actually audible through the phone earpiece. The report records track creation and start, matching public playback visibility, mode-request ordering, route acceptance, active-session earpiece observation, any speaker reclamation, cleanup, and the human result. `setCommunicationDevice()` returning `true` alone is not success.
- **Observed result:** The application reported silent track creation and start, visible Private Audio voice-communication playback, the track active before an exception-free explicit mode request, and exactly one later routing attempt returning `true`. Before participation Android reported `MODE_IN_COMMUNICATION`, built-in speaker, and speakerphone on. After participation it reported `MODE_IN_COMMUNICATION`, built-in earpiece, speakerphone off, and active voice-communication playback configurations on built-in earpiece/device ID 2. After a full uninstall and reinstall of the current APK, repeated physical testing confirmed that active ChatGPT Voice was audible through the upper built-in earpiece.
- **Notes:** Both success criteria are satisfied: (1) Android reported the built-in earpiece while ChatGPT Voice remained active, and (2) the human tester heard ChatGPT Voice through it. API acceptance, Android-reported route, and audible route remain separate evidence. Earlier attempts included a run where Android reported the earpiece but the tester still heard the main speaker. Full uninstall and reinstall preceded the first repeatable physically audible success; causal relationship is not yet established. The track outputs locally generated zeros only and uses no microphone input, capture, proxying, audio focus, retry, service, or additional routing call.

## POC-5 follow-up stability matrix

These are stability and safety follow-ups for the successful POC-5, not a new routing POC. Each remains **NOT TESTED** unless a separately recorded execution establishes otherwise:

- repeated cold-start runs;
- repeated ChatGPT Voice sessions without reinstall;
- Private Audio process restart;
- activity destruction and recreation;
- explicit disarm cleanup;
- ChatGPT ending the communication session;
- incoming and outgoing real telephony, which must retain priority;
- APK update over the existing installation;
- full uninstall/reinstall as a comparison baseline;
- confirmation that no silent `AudioTrack` survives any cleanup path; and
- confirmation that ordinary audio behavior returns after cleanup.

## Layer 1 service-lifecycle validation

The following checks validate lifecycle ownership without claiming routing success from automation. Record a physical device, Android/build versions, and exact application commit for every execution:

- Bind with Main visible but do not arm; verify diagnostics populate, no foreground notification appears, and no routing mutation occurs.
- Arm from the visible Main screen; verify the foreground notification appears promptly before starting the external voice-session trigger, then verify the protected POC-5 route and evidence sequence once.
- Recreate Main while armed and while actively routed; verify the experiment continues, the recreated activity displays the retained state/events, and no second request, observer, track, or writer appears.
- Leave Main while armed/active; verify the started foreground service continues independently and notification tap returns to the normal diagnostic activity.
- Return and press **Disarm / Clear**; verify existing cleanup ordering, notification removal, foreground exit, and ended started lifetime while the bound diagnostic remains available.
- End the external communication session and verify the protected core automatically clears its run before Layer 1.5 arms one fresh waiting experiment. Verify waiting creates no silent track and issues no route request before a new qualifying session.
- Stop the service cleanly while it is genuinely unbound and verify observer callback unregistration and the existing cleanup. Separately kill the process and verify no state is restored, no automatic re-arm occurs, and `START_NOT_STICKY` does not restart the service.
- Repeat incoming and outgoing real-call safety tests while Main is absent. Telephony must retain priority and the service must not retry or reassert routing.
- Verify notification behavior on API 31–33 and API 34–36, including `specialUse` foreground type on API 34+, with notification permission both allowed and denied where the OS permits testing it.

## Layer 1.5 multi-session physical gate

- **Status:** NOT TESTED
- **Device / Android / build / voice-app version:** Not recorded
- **Steps:** (1) Launch Private Audio. (2) Enable once. (3) Start Voice #1 and confirm output through the built-in earpiece. (4) End Voice #1 and confirm route, mode, writer, and silent-track cleanup followed by a fresh armed Waiting state. (5) Do not press Power/Enable again. (6) Start Voice #2 and confirm a fresh POC-5 run moves output to the earpiece. (7) Inspect the retained report and confirm each session contains exactly one routing request and no request occurred between sessions. (8) Disable and confirm protected cleanup, foreground exit, and Ready behavior. (9) Start Voice #3 and verify Private Audio creates no track, mode request, or routing request.
- **Expected result:** One activation spans Voice #1 and Voice #2 as two distinct experiments. Each experiment performs its existing cleanup, contains at most one `setCommunicationDevice()` request, and the between-session Waiting period has no `AudioTrack`. Disable prevents any action during Voice #3. A telephony/system-priority block or genuine protected failure remains terminal and requires explicit user reset; it must not re-arm.
- **Observed result:** Not recorded. Automated tests characterize lifecycle structure but do not establish physical routing or OEM behavior.

## Layer 1.6 permanent-controller physical gate

- **Status:** PARTIAL PASS (supplied physical evidence)
- **Device / Android / build / voice-app version:** Xiaomi `2201117TY`; remaining metadata not recorded
- **Steps:** (1) Power ON and verify clean waiting has no local track, mode participation, or device override. (2) Play ordinary media and verify it remains normally routed and no cycle begins. (3) Start communication session #1; verify one POC-5 sequence and earpiece audio. (4) End it; verify an end candidate, at least 1.5 seconds of stable evidence, complete route/mode/writer/track cleanup, and return to waiting while still enabled. (5) Without another Power press, repeat for sessions #2 and #3. (6) During a separate active session transiently recreate playback and change route speaker/earpiece; verify no premature cleanup. (7) Test OFF while waiting, active, and end confirmation is pending; future sessions must be untouched. (8) Kill the process and verify fail-closed behavior. (9) Repeat with incoming/outgoing telephony; Android must retain priority.
- **Expected result:** Each communication session receives a fresh isolated cycle and exactly one route request. Waiting and OFF do not affect ordinary media. Stale callbacks never affect a later cycle. The test records Android-reported and human-audible results separately.
- **Observed result:** The first ChatGPT Voice session was audibly routed through the telephone earpiece. After ending Voice and starting it again without toggling Private Audio, the second session also routed through the earpiece. Disabling routing restored normal behavior; closing Private Audio restored normal behavior; reopening and re-enabling worked again. A diagnostic report was not copied for this run. The remaining matrix items, including telephony and exact cleanup evidence, are not established by this result.

## Layer 2 product-state observation gate

- **Status:** NOT TESTED
- **Device / Android / build / voice-app version:** Not recorded
- **Steps:** Observe the service-owned `privateAudioState` with a temporary test consumer or the later product UI. Verify OFF → `READY`; Power ON → `WAITING`; established current earpiece routing → `ACTIVE`; confirmed communication end and complete Layer 1.6 cleanup → `WAITING`; a second session can reach `ACTIVE` without another Power action; and Power OFF → `READY`. Separately induce only an existing protected block or explicit routing rejection where safely practical and verify `ERROR` → Power OFF → `READY`.
- **Expected result:** Consumers receive each current-evidence transition without independently interpreting diagnostics. Old success, failure, or route-reversion history does not survive Power OFF in the product state, normal inactivity is `WAITING`, and observation causes no routing action.
- **Observed result:** Not recorded. JVM tests verify the pure precedence and source ownership, but do not establish Compose rendering, OEM callback delivery, physical routing, or audible output.

## Layer 3 Main product UI and combined physical gate

- **Status:** PARTIAL PASS (supplied physical evidence)
- **Device / Android / build / voice-app version:** Xiaomi `2201117TY`; remaining metadata not recorded
- **Preconditions:** Install the Layer 3 debug build on the target physical phone with no attached audio accessory. Retain the four approved READY, WAITING, ACTIVE, and ERROR visual references for side-by-side comparison. Where ERROR can be induced safely using an existing protected failure or explicit routing rejection, do not introduce a new failure mechanism.
- **Steps:** (1) Launch Private Audio and verify the fixed OLED-black product screen reaches `Ready`; record a screenshot. (2) Press Power once and verify `Waiting`, the foreground notification, no local track/mode/device influence before qualifying communication, and normal routing of ordinary media; record a screenshot during the restrained amber pulse. (3) Start a qualifying Voice session and verify the existing Layer 1.6 cycle reaches `Active` only when Layer 2 has current participating communication-mode and built-in-earpiece evidence; separately record Android-reported routing and human-confirmed audible output, then capture a screenshot. (4) End Voice and verify at least 1.5 seconds of stable end evidence, complete route/mode/writer/track cleanup, and return to `Waiting` without another Power press. (5) Start Voice again and verify a fresh `Active` cycle without another Power press. (6) Press Power while active or waiting and verify full cleanup, notification removal, and `Ready`; start Voice once more and verify Private Audio takes no action. (7) If an existing protected failure can be induced safely, verify `Error`, capture a screenshot, then press Power and verify cleanup to `Ready`. (8) Enable again, press Close, and verify safe controller shutdown occurs before the Main task is removed and subsequent Voice is untouched. (9) In each captured state compare title/subtitle, status position, Power diameter/icon/border/glow, bottom icons/labels, spacing, optical stroke weight, system bars, and identical geometry against the approved reference.
- **Expected result:** The product UI follows `READY` → `WAITING` → `ACTIVE` → `WAITING` → `ACTIVE` → `READY` solely from the service projection, with `ERROR` → `READY` after Power OFF when safely testable. Ordinary media remains normally routed, every communication session retains the protected one-request/cleanup behavior, Close fails closed, and Floating/Settings perform no action. All four screenshots retain identical geometry on a pure-black surface; only the approved dot, label, Power color, and restrained glow/pulse change.
- **Observed result:** The first ChatGPT Voice session was audibly routed through the telephone earpiece, and a second session after ending/restarting Voice routed through the earpiece without toggling Private Audio. Disable and Close each restored normal behavior, and reopening/re-enabling worked again. No diagnostic report was copied. Exact screenshot parity and the remaining ERROR/telephony checks were not recorded.

## Layer 4 Settings physical gate

- **Status:** NOT TESTED
- **Device / Android / build:** Not recorded
- **Preconditions:** Install the merged Layer 4 build and retain the approved Settings reference alongside the approved Main reference.
- **Steps:** (1) Open Settings from Main and verify Main remains unchanged underneath with subtle dimming. (2) Compare the compact 88–92% width graphite sheet, soft corners, thin border, title, row typography, spacing, chevrons, and version placement to the approved reference. (3) Tap outside and verify dismissal. (4) Reopen and press system Back at root; verify dismissal. (5) Visit Language, Advanced, and About; verify each remains in the same sheet and system Back returns to root. (6) Tap Copy diagnostic report; verify the clipboard receives the existing English report and feedback reads `Diagnostic report copied.` (7) Confirm the displayed version matches installed build metadata. (8) Repeat while Private Audio is Waiting/Active and verify opening/navigation/copying Settings causes no new route request or product-state transition.
- **Expected result:** Settings matches the approved modal reference and navigation contract while closed Main remains visually identical. Clipboard content is the existing formatter output, version is build-derived, and Settings introduces no routing/state-projection behavior.
- **Observed result:** Not recorded. Automated UI/source-contract tests do not establish target-device rendering, clipboard behavior, or physical routing isolation.

## Layer 5 overlay foundation physical/emulator gate

- **Status:** NOT TESTED
- **Device / Android / build:** Not recorded
- **Preconditions:** Install the Layer 5 build with overlay permission initially absent. Keep another application available for foreground comparison. This gate validates the temporary foundation, not final-controller visuals or physical audio compatibility.
- **Steps:** (1) Tap Main → Floating, verify Android opens the package overlay-permission screen, deny or cancel, return to Main, and verify no window or false success appears. (2) Repeat, grant permission, return, and verify one temporary test surface appears. (3) Remove it with its own Close and verify Main, task, and any enabled Private Audio controller remain alive and unchanged. (4) Tap Floating with permission already granted and verify immediate display. (5) Put another application in the foreground and verify the surface remains above it while its owner is alive. (6) Reopen/recreate Main and tap Floating repeatedly; verify no duplicate window. (7) With the overlay visible and routing enabled, press Main Close; verify overlay removal, established route/mode/track cleanup, notification removal, and task removal. (8) Deny/revoke permission where the OS permits and repeat show attempts; verify no crash. (9) Kill the process with the overlay visible and relaunch; verify it is not automatically restored. (10) Confirm ordinary media and the existing Layer 1.6 multi-session/telephony safety matrix behave unchanged; record Android-reported and audible routing separately.
- **Expected result:** Permission denial/cancellation is inert; a grant enables exactly one removable application overlay; activity backgrounding/recreation does not own or duplicate it; overlay Close affects only the overlay; Main Close retains full safe shutdown; and process death restores no overlay intent. No result from this gate verifies or changes POC-5 physical routing behavior.
- **Observed result:** Not recorded. JVM source-contract tests and build/lint checks validate structural isolation but cannot establish system permission-screen behavior, cross-application z-order, OEM process lifetime, or physical audio behavior.

## Layer 6.2 floating-controller UI and task-lifecycle gate

- **Status:** NOT TESTED
- **Evidence level:** Emulator/runtime UI validation is required for window rendering, touch delivery, permission flow, and task behavior. JVM/source-contract evidence does not satisfy this gate; physical-device testing remains authoritative for routing and audible output.
- **Device / Android / build:** Not recorded
- **Preconditions:** Install the current build, retain the approved floating-controller reference, and begin with overlay permission absent. Keep another application available for foreground/task comparison.
- **Steps:** (1) Deny or cancel Main → Floating and verify Main remains foreground with no window or false success; then grant permission and verify Main backgrounds only after exactly one overlay appears. (2) Compare the 300 × 62 dp near-black controller, outline, spacing, and current `STATUS → POWER → EXPAND → CLOSE` order; verify status is left, Power centered, Expand and Close right, and no separator or extra control is present. (3) Observe Ready, Waiting, Active, and safely induced Error where practical; verify the displayed state follows `PrivateAudioService.privateAudioState` without a second projection. (4) Drag from non-control status/background space below and beyond touch slop; verify tap-sized movement does not drag, actual drag moves the overlay, and clamping keeps every edge reachable in supported portrait and landscape configurations. (5) Tap Power in applicable states and verify it delegates to the same controller enable/disable actions and cleanup as Main. (6) From another foreground application tap Expand; verify the existing Main task is restored/reused without a duplicate, the overlay is removed immediately, and controller/routing state is unchanged. (7) Create the overlay again from Main Floating, then tap X while enabled or active; verify only the overlay closes while controller/routing state and Main task remain available. (8) Show repeatedly and verify exactly one window; verify Main Floating can recreate it after either X or Expand removed it. (9) With the overlay visible, use Main Close and verify overlay removal, full controller cleanup, notification removal, and task removal. (10) Revoke permission and attempt show without a crash; kill the process with the overlay visible and verify no persistent or automatic overlay restoration. (11) Run the established multi-session and real-call safety gates separately on a physical device, recording Android-reported route and audible routing independently.
- **Expected result:** The draggable controller follows the authoritative service projection and delegates Power to the existing controller. X is overlay-only; Expand restores/reuses Main and removes the overlay without changing routing; Main Floating can create it again; and Main Close remains the full fail-closed shutdown path. Process death restores no overlay. Runtime UI evidence establishes only UI/task behavior, while physical routing evidence remains a separate requirement.
- **Observed result:** Not recorded. JVM tests cover state consumption, controller delegation, clamp calculation, and source-level lifecycle contracts, but do not establish emulator/OEM window rendering, touch delivery, Android task behavior, process lifetime, physical routing, or audible output.

## Per-app language and existing-Mini configuration gate

- **Status:** PARTIAL PASS (user-supplied runtime evidence for Language scrolling and selection return only)
- **Evidence level:** The recorded runtime check establishes the specified Language-list interaction only. Source/JVM checks remain structural; emulator/runtime UI checks are needed for the remaining presentation and lifecycle behavior; physical testing is required for any routing-isolation claim.
- **Device / Android / build:** Not recorded
- **Steps:** (1) Open Settings → Language and verify the list is vertically scrollable, all configured locale options remain reachable, and sheet content stays within visible and system-navigation bounds. (2) On API 33 or later, select English, Polski, another configured locale, and Default in turn; verify each selection applies the existing `LocaleManager` behavior and automatically returns to root Settings, whose Language row reports the actual override. (3) Reopen Language and use explicit Back without selecting; verify it returns to Settings root without changing the locale. (4) Confirm Main and Settings use selected resources, the override survives process restart through Android's system-managed preference, and Default follows device language. (5) With one floating controller visible, repeat locale transitions in Ready, Waiting, Active, and safely induced Error; verify localized state text/accessibility updates in place without moving, duplicating, or closing the window and without changing service binding, controller state, routing, or controls. (6) Confirm a subsequently created or updated foreground notification uses selected resources. (7) On API 31–32, verify the Android 13 requirement, Default selection, safe no-op choices, and system-language behavior without an application override.
- **Expected result:** The Language list remains usable inside visible/system-navigation bounds. Selection applies the existing locale behavior and returns immediately to Settings root; explicit Back without selection also returns to root. API 33+ selection is owned by `LocaleManager` and generated locale configuration, while API 31–32 remain safely system-language-only. Locale changes do not alter floating-controller ownership or audio/controller state.
- **Observed result:** The user runtime-checked that the Language list scrolls with all locale options reachable and that successful selection returns automatically to root Settings; this behavior works. Device/build metadata and evidence for system-bound insets, explicit Back, persistence, all locale/resource transitions, running-overlay refresh, notification refresh, and routing isolation were not recorded, so those items remain unverified.

## Layer 7A call-like proximity-screen physical evidence

- **Status:** CORE BEHAVIOR PASS; remaining boundary matrix NOT TESTED
- **Device / Android:** Xiaomi `2201117TY` / Android 13/API 33
- **Preconditions:** Install the Layer 7A build, keep the diagnostic report available, and begin without Bluetooth, wired, or USB audio. Do not infer physical screen behavior from automated tests.
- **Steps and expected results:**
  1. Power ON and remain `WAITING`; cover the proximity sensor. **Expected:** Private Audio does not turn the screen off and reports no held proximity wake lock.
  2. Start ChatGPT Voice and establish `ACTIVE` on the built-in earpiece; move the phone near the ear. **Expected:** the screen turns off and diagnostics report held ownership.
  3. Move the phone away. **Expected:** Android restores the screen.
  4. End Voice while proximity is near. **Expected:** Private Audio immediately releases ownership and leaves no stuck screen-off condition.
  5. During another `ACTIVE` cycle press Private Audio Power OFF. **Expected:** the wake lock releases and existing routing cleanup is unchanged.
  6. During another `ACTIVE` cycle use Main Close. **Expected:** the wake lock releases and full existing cleanup is unchanged.
  7. Power ON once, complete one normal Voice cycle, then start another without toggling Power. **Expected:** proximity acquire/release works again.
  8. Repeat the active near/away/end sequence in Floating mode. **Expected:** behavior is identical.
  9. While active use Floating Expand → Main. **Expected:** the UI transition causes no proximity or routing ownership change and no release/reacquire.
  10. Select/use Bluetooth or another non-earpiece communication route. **Expected:** Private Audio holds no proximity wake lock.
  11. Terminate the process/service. **Expected:** no persistent Private Audio proximity ownership remains.
  12. Run several consecutive routing cycles. **Expected:** acquire/release repeats once per eligible interval with no sticky black screen.
- **OEM characterization:** While proximity is NEAR during an eligible active cycle, press the physical Power button once and record the screen and diagnostic behavior, then press it again and record recovery. This is characterization only; do not add a workaround from this gate without a new decision.
- **Observed result (2026-08-18):** During `ACTIVE` with the built-in earpiece current, moving near turned the screen off and moving away restored it automatically. Wake-lock support reported `true`; the supplied post-session report showed held `false`. This passes the core call-like screen behavior only. No other listed boundary was recorded, so those cases remain **NOT TESTED / UNKNOWN**.

## Layer 7B proximity hardening physical gate

- **Status:** GENERAL TESTER CONFIRMATION; individual cases NOT TESTED unless recorded above
- **Required tests:**
  - **A — Session end while near:** End an `ACTIVE` earpiece session while near, then move away. Expect `WAITING`, normal screen return, no sticky black screen, and held `false`.
  - **B — Successive cycles:** With Power ON, run two complete active near/off, far/on, end cycles. Expect correct acquisition and release in both without toggling Power.
  - **C — Private Audio Power OFF:** During `ACTIVE`, move away and turn Power OFF. Expect proximity release and unchanged routing cleanup; covering afterward causes no Private Audio screen-off behavior.
  - **D — Main Close:** During `ACTIVE`, move away and use Main Close. Expect full existing cleanup and no proximity ownership.
  - **E — Floating:** In valid `ACTIVE` earpiece use with Mini visible, expect the same near/off and far/on response.
  - **F — Floating Expand to Main:** Expand while `ACTIVE`. Expect the UI transition itself not to interrupt routing or proximity ownership.
  - **G — Setting OFF:** Disable “Turn screen off near ear” before or during valid `ACTIVE` earpiece communication. Expect private audio and `ACTIVE` unchanged, no proximity screen-off, and diagnostics `Feature enabled=false` and held `false`.
  - **H — Setting ON:** Re-enable during valid `ACTIVE` earpiece communication. Expect call-like proximity behavior to become eligible immediately.
  - **I — Setting persistence:** Persist OFF across application/service restart, then repeat with ON.
  - **J — Bluetooth/non-earpiece:** If available, observe a selected/current non-earpiece route. Expect no Private Audio proximity ownership; do not change routing policy for the test.
  - **K — Real call:** If practical, introduce a real call. Expect telephony priority, no competition from Private Audio proximity ownership, and safe post-call state.
  - **L — Physical Power button:** While `ACTIVE`, near, and screen off, press Power once and record Xiaomi/MIUI behavior. Characterization only; do not implement a workaround here.
  - **M — Process/service termination:** If practical, terminate while proximity ownership is active. Expect no persistent Private Audio screen-off ownership.
- **Observed result (2026-08-18):** After Layer 7B and its compile fix, the tester reported that the implemented behavior worked as intended. Because no case-by-case observations were supplied, A–M remain **NOT TESTED / UNKNOWN** as individual physical cases; in particular this statement is not Bluetooth, telephony, process-death, or Power-button evidence.
- **Evidence rule:** Record Android-reported state and human-observed screen behavior separately. Layer 7 implementation closure does not convert an unrecorded physical case into PASS.

## Layer 7C final audit and regression closure

- **Status:** IMPLEMENTATION PASS; extended physical matrix remains NOT TESTED / UNKNOWN
- **Date:** 2026-08-18
- **Automated scope:** The final repository audit verifies the sole observer → service evidence/state/preference decision → mechanics-only controller chain; all eligibility inputs; immediate synchronization; centralized evidence-departure release; explicit Power OFF and destruction boundaries; successive-cycle re-arming; UI ownership isolation; diagnostic fields; default/persistence wiring; localization parity; and unchanged protected route/mode request counts. Automated evidence remains structural/JVM evidence, not OEM screen behavior.
- **Physical scope:** Xiaomi `2201117TY` on Android 13/API 33 establishes `ACTIVE` + built-in earpiece + near → screen off, followed by far → screen on. Support was reported `true`, and the post-session diagnostic reported held `false`. The later general Layer 7B confirmation is recorded without assigning it to individual cases.
- **Remaining regression/characterization:** `WAITING` near behavior, session end while near, successive proximity cycles, preference OFF/ON and recreation persistence, Power OFF, Main Close, Mini/Expand transitions, Bluetooth/wired/USB/other routes, real telephony, process/service termination, and physical Power-button behavior require separately recorded physical execution before any is called PASS.

## Automatic assistant-class routing regression gate

- **Status:** CORE ASSISTANT AND COMMUNICATION PATHS PASS FROM SUPPLIED PHYSICAL RESULTS; ORDINARY-MEDIA MATRIX REMAINS PARTIAL
- **Evidence rule:** Record public Android metadata, Private Audio state, and human-confirmed audible output separately. `setCommunicationDevice(...)=true`, `MODE_IN_COMMUNICATION`, or a reported built-in earpiece alone does not prove Gemini playback changed output.
- **Communication regression:** With Private Audio ON, run ChatGPT Voice through Waiting → Active → built-in earpiece → session end → Waiting. Confirm exactly one request and unchanged Power OFF cleanup.
- **Assistant path:** With Private Audio ON, start Gemini Live from initial `MODE_NORMAL` with `USAGE_ASSISTANT` + `CONTENT_TYPE_SPEECH`. Verify Active, audible speaker-to-earpiece routing, proximity, session-end cleanup, and Waiting re-arm.
- **Exclusions:** Verify assistant sonification alone and `USAGE_MEDIA` do not trigger. Exercise YouTube/music/video, NotebookLM Audio Overview, and Kimi TTS/media responses and record each physical result separately.
- **Observed result (supplied 2026-08-19):** Communication-class ChatGPT Voice, Grok, Perplexity, and Character.AI passed. Gemini Live assistant/speech audibly moved speaker → earpiece; proximity, session cleanup, Active → Waiting, and re-arm passed. The prior experimental-OFF regression confirmed ChatGPT remained correct. No supplied case-by-case result establishes the listed ordinary-media applications, so those remain **NOT TESTED / UNKNOWN** physically.

## Automatic browser communication routing regression gate

- **Status:** CORE BROWSER, COMMUNICATION, AND ASSISTANT PATHS PASS FROM SUPPLIED PHYSICAL RESULTS
- **Automatic browser path:** With Private Audio ON, Chrome ChatGPT Voice's exact voice-communication/unknown count selects `BROWSER_COMMUNICATION` and enters the protected POC-5 sequence with exactly one routing request.
- **Session end:** The unknown-content count reaching zero remains absent for 1.5 seconds, then cleanup clears the communication device, relinquishes mode participation, releases the silent track and proximity ownership, and returns to Waiting/armed.
- **Isolation regressions:** ChatGPT Android continues to select `COMMUNICATION`; Gemini Live continues to select `ASSISTANT`; ordinary `USAGE_MEDIA` and assistant/sonification remain ignored. Power OFF cleanup is unchanged.
- **Observed result (supplied 2026-08-19):** ChatGPT Web in Chrome passed audible earpiece routing and cleanup. ChatGPT Android and Gemini Live passed regression. Each routing cycle issued exactly one request, and subsequent sessions routed without restarting Private Audio.
- **Evidence rule:** Continue recording Android metadata, request count, and human-confirmed audible output separately in future device runs.

## Fake Phone pre-arm controlled physical gate

- **Status:** NOT TESTED / UNKNOWN
- **Device:** Xiaomi Android 13.
- **Experiment ON:** Run at least three ChatGPT Voice starts and three Gemini Voice starts. For each, record whether the startup ping was audible and from speaker or earpiece, confirm Private Audio remained `WAITING` before qualifying speech, then record normal `ACTIVE` transition, audible voice routing, and end cleanup back to `WAITING`.
- **Control OFF:** Repeat starts with Fake Phone pre-arm disabled and verify the established clean waiting architecture and protected POC-5 lifecycle are unchanged; the expected current observation is assistant/sonification on the speaker.
- **Evidence rule:** Correlate the neutral `ASSISTANT/SONIFICATION observed during pre-arm` entry with human listening. A reported earpiece, communication mode, or accepted route request alone is not audible success, and public metadata must not be used to claim application ownership.

## Bounded assistant protected-session linger physical gate

- **Status:** NOT TESTED / UNKNOWN
- **Device / control:** Xiaomi Android 13; Fake Phone pre-arm OFF.
- **Gemini acceptance:** Complete at least five consecutive conversational turns. Across ordinary inter-turn gaps verify `ACTIVE`, proximity ownership, `MODE_IN_COMMUNICATION`, and the built-in earpiece remain established with the same routing-cycle ID, silent track, and single routing-attempt count. When exact assistant/speech returns within linger, verify diagnostics record resumption/cancellation/reuse without another mode or device request. Abandon a conversation and verify cleanup occurs exactly once after the 1.5-second confirmation plus bounded 15-second linger, then returns to `WAITING` and releases proximity.
- **ChatGPT regression:** Complete at least five conversational turns and record audible earpiece output, `ACTIVE`, proximity behavior, and final cleanup independently. Its communication-origin end timing must remain the existing 1.5-second confirmation followed by immediate cleanup, with no assistant linger.
- **Boundary regression:** Verify browser communication retains its existing end behavior. During an assistant linger separately exercise Private Audio Power OFF and Main Close and verify immediate cleanup with no later timer effect. Where practical, exercise telephony/system priority and required-earpiece/route loss and verify immediate fail-closed cleanup.
- **Evidence rule:** Record public metadata, diagnostic timestamps/cycle IDs/request counts, and human-observed audible output separately. Passing automated tests or seeing a reported earpiece does not establish multi-turn physical stability or prove that prior teardown caused Gemini failure. If Gemini still fails while the context remains continuously active, capture diagnostics and reassess rather than extending linger.
