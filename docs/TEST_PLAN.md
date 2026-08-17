# Hardware-in-the-Loop Test Plan

Allowed status values are **NOT TESTED**, **PASS**, **FAIL**, and **BLOCKED**. Record a specific device, Android version, and application build for every execution. Emulator results are insufficient for built-in-earpiece routing or cross-application compatibility; those claims require a physical-device test.

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

- **Status:** NOT TESTED
- **Device:** Not recorded
- **Android version:** Not recorded
- **Build:** Not recorded
- **Preconditions:** Current ChatGPT Android application is installed; an active voice session is available; baseline and privacy-safe test content are prepared.
- **Steps:** Start ChatGPT Voice, issue the routing request, observe the reported communication device, and determine the audible output device.
- **Expected result:** The test determines—without assuming—whether ChatGPT output uses the built-in earpiece after the request.
- **Observed result:** Not recorded
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

- **Status:** NOT TESTED
- **Device:** Not recorded
- **Android version:** Not recorded
- **Build:** Not recorded
- **Preconditions:** Future M0-capable utility build and current Gemini voice experience are available.
- **Steps:** Establish a voice session, issue and clear the request, and observe reported and audible routing.
- **Expected result:** The future compatibility test determines whether Gemini voice respects the request and restores safely.
- **Observed result:** Not recorded
- **Notes:** Compatibility is currently unknown; record Gemini version.

## T-013 — Browser realtime voice

- **Status:** NOT TESTED
- **Device:** Not recorded
- **Android version:** Not recorded
- **Build:** Not recorded
- **Preconditions:** Future M0-capable utility build and a privacy-safe browser realtime voice session are available.
- **Steps:** Establish a browser voice session, issue and clear the request, and observe reported and audible routing.
- **Expected result:** The future compatibility test determines whether the browser session respects the request and restores safely.
- **Observed result:** Not recorded
- **Notes:** Record browser, browser version, and tested site; do not generalize one result to all browsers.

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
