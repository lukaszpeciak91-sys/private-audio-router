# Architecture

```text
External voice application
        |
        v
Android communication audio
        |
        v
AudioManager
        |
        v
Built-in earpiece
```

## Boundary

The application is intended to be a small system control surface outside the audio data path. It should not receive, decode, or forward AI audio; record microphone input; or capture another application's playback. External applications continue to own their conversations while Android owns communication-audio routing.

## APIs of interest

The initial investigation concerns public Android concepts for:

- enumerating available communication devices and identifying a built-in earpiece;
- reading the current communication device;
- observing communication-device changes;
- requesting a communication device; and
- clearing a communication-device request.

This is an investigation boundary, not a claim that an independent application's routing request controls another application's output.

## Current approach and safety decision

POC-2 minimally extends the process-lifetime one-shot experiment after POC-1's routing-only request failed to change Android's reported route on the tested device. It reads `AudioManager.mode`, `communicationDevice`, `availableCommunicationDevices`, and the public (deprecated but read-only) `isSpeakerphoneOn` property. Once armed, it acts only when Android reports `MODE_IN_COMMUNICATION`, the current device is the built-in speaker, and an available built-in earpiece exists. It records the complete pre-change state, explicitly requests `MODE_IN_COMMUNICATION`, records the resulting state, and then calls `setCommunicationDevice()` exactly once. Guards are set before either mutation, so synchronous callbacks, rejection, or later route changes cannot cause a retry.

The experiment does not identify which application caused an observed transition, enter the audio data path, or claim that mode participation or API acceptance proves audible routing. Telephony/system-priority modes block and disarm the experiment; cleanup clears the communication-device request and sets `MODE_NORMAL` to relinquish Private Audio's mode ownership rather than defending it.

## Diagnostic lifecycle

The callbacks are registered from activity creation until destruction. They can continue to append to the in-memory log after the activity is stopped while the application process remains alive. This is best-effort observation only: Android may kill a background process, no callback delivery is promised while it is gone, and the in-memory log then disappears. Every `onResume` records a fresh snapshot so the before/after experiment remains useful even when no background callback was delivered.

POC-2 intentionally adds no foreground service, persistent storage, background permission, wake lock, or notification. Callbacks may continue after Private Audio is backgrounded only while its process remains alive. Android may kill that process, so this PoC does not establish production-grade background reliability. A background-state API rejection is evidence to record, not authorization for a workaround in this experiment.

## Lifecycle expectations

The one-shot routing experiment applies these lifecycle expectations:

1. Explicitly arm the possibility of one mode-participation request followed by one routing request.
2. Snapshot before mutation and separately report the mode request, resulting mode, routing API return value, Android-reported device, speakerphone state, callbacks, and still-unknown audible route.
3. Clear the device request and relinquish Private Audio's mode participation on explicit disarm, observed exit from `MODE_IN_COMMUNICATION`, telephony/system-priority modes, and clean activity destruction.
4. Clear or lose influence when the process is gone; unexpected persistence is a failure to investigate.
5. Never attempt to override real telephony, which retains system priority.
