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

- **Status:** NOT TESTED
- **Device:** Not recorded
- **Android version:** Not recorded
- **Build:** PR #5 build, exact commit not yet recorded
- **ChatGPT version:** Not recorded
- **Preconditions:** Disconnect or record audio accessories. Confirm no real phone call is active. Private Audio shows the baseline and an available built-in earpiece.
- **Steps:** (1) Launch Private Audio. (2) Confirm and record baseline. (3) Press **Arm Earpiece Test**. (4) Switch to ChatGPT. (5) Start Voice. (6) Confirm ChatGPT initially uses the main speaker. (7) Remain in ChatGPT and listen for an automatic transition to the earpiece. (8) Speak with ChatGPT briefly if routing changes. (9) End ChatGPT Voice. (10) Return to Private Audio. (11) Confirm cleanup and state recovery. (12) Press **Copy Report** and retain the complete plain-text report.
- **Expected result:** The report establishes whether the trigger occurred, whether the single `setCommunicationDevice()` call returned true or false, whether Android subsequently reported the earpiece, whether the human tester audibly confirmed ChatGPT moved to the earpiece, whether ending Voice restored normal state, and whether any abnormal system-audio behavior remained.
- **Observed result:** Not recorded
- **Notes:** API acceptance, an observed Android device, and audible cross-application routing are separate evidence. Callbacks are best effort while the process lives; Android may kill the background process. Do not mark PASS without the audible and cleanup checks on physical hardware. A failure is evidence and must not trigger a retry or invasive workaround.
