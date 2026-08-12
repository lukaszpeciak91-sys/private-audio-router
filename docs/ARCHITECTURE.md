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

PR #4 is an observe-only investigation. It reads `AudioManager.mode`, `communicationDevice`, `availableCommunicationDevices`, and the public (deprecated but read-only) `isSpeakerphoneOn` property. It registers `OnCommunicationDeviceChangedListener` and `AudioDeviceCallback`; it never selects a device, changes mode, or enters the audio data path. A future change requires evidence and a separate accepted decision. A failed test is not authorization to escalate.

## Diagnostic lifecycle

The callbacks are registered from activity creation until destruction. They can continue to append to the in-memory log after the activity is stopped while the application process remains alive. This is best-effort observation only: Android may kill a background process, no callback delivery is promised while it is gone, and the in-memory log then disappears. Every `onResume` records a fresh snapshot so the before/after experiment remains useful even when no background callback was delivered.

PR #4 intentionally adds no foreground service, persistent storage, background permission, wake lock, or notification. Those mechanisms would expand scope and could perturb the experiment.

## Lifecycle expectations

Future routing work, which is not implemented in PR #4, must retain these expectations:

1. Enable a routing request explicitly.
2. Observe and report the resulting communication device.
3. Clear the request on disable.
4. Clear or lose influence when the process is gone; unexpected persistence is a failure to investigate.
5. Never attempt to override real telephony, which retains system priority.
