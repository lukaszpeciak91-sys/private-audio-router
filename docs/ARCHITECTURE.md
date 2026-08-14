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

The application is a small system control surface outside ChatGPT's audio-data path. It does not receive, decode, capture, proxy, or forward ChatGPT audio and does not record microphone input. POC-5 is nevertheless an active participant in Android's audio subsystem: it intentionally generates and plays its own local silence through a `USAGE_VOICE_COMMUNICATION` `AudioTrack` so it can participate in communication-mode arbitration. External applications continue to own their conversations while Android owns communication-audio routing.

## APIs of interest

The initial investigation concerns public Android concepts for:

- enumerating available communication devices and identifying a built-in earpiece;
- reading the current communication device;
- observing communication-device changes;
- requesting a communication device; and
- clearing a communication-device request.

This is an investigation boundary, not a claim that an independent application's routing request controls another application's output.

## Current approach and safety decision

POC-5 follows the unsuccessful POC-4 ownership-transition experiment. It reads `AudioManager.mode`, `communicationDevice`, `availableCommunicationDevices`, and the public (deprecated but read-only) `isSpeakerphoneOn` property. Once armed, it acts only when Android reports `MODE_IN_COMMUNICATION`, the current device is the built-in speaker, and an available built-in earpiece exists. It records the complete pre-change state, creates a public `AudioTrack` with `USAGE_VOICE_COMMUNICATION`, `CONTENT_TYPE_SPEECH`, mono PCM 16-bit output and a device-supported native voice-call sample rate, and feeds only zero-valued samples using bounded non-blocking writes on one background-priority thread. After the track reports `PLAYSTATE_PLAYING`, it requests `MODE_IN_COMMUNICATION` without first requesting `MODE_NORMAL`, then makes exactly one earpiece request. A single delayed snapshot observes the route after one second; it never makes another request.

The silent track contains no microphone, captured, proxied, forwarded, or external audio and the experiment requests no audio focus. It used 48 kHz on the tested Xiaomi device. Public playback configurations do not expose enough client identity here to distinguish two applications with matching voice-communication attributes, so a common playback device is recorded only as “where observable,” not attributed conclusively to ChatGPT. API acceptance and an Android-reported route remain distinct from audible routing; physical listening is the final routing truth. Telephony/system-priority modes block and disarm the experiment; cleanup cancels observation, clears the communication-device request, sets `MODE_NORMAL` to relinquish Private Audio's mode participation, stops the writer, and stops, flushes, and releases the `AudioTrack`.

On Xiaomi `2201117TY` running Android 13/API 33, the current POC-5 caused Android to report the built-in earpiece and a human tester repeatedly heard active ChatGPT Voice through the upper built-in earpiece. This establishes the architecture on that tested configuration, not general compatibility or production-grade stability.

The diagnostic evidence model uses public, read-only process identity/importance and active playback configuration APIs at pre-ownership, post-mode-ownership, post-request, and delayed-observation boundaries. The list from `getActivePlaybackConfigurations()` is treated as the active playback snapshot; entries describe only the public attributes, capture policy, and device Android makes visible to this application. They neither contain audio nor establish application identity. Effective mode ownership remains external evidence that must be correlated by PID/UID with `dumpsys audio`.

## Diagnostic lifecycle

The callbacks are registered from activity creation until destruction. They can continue to append to the in-memory log after the activity is stopped while the application process remains alive. This is best-effort observation only: Android may kill a background process, no callback delivery is promised while it is gone, and the in-memory log then disappears. Every `onResume` records a fresh snapshot so the before/after experiment remains useful even when no background callback was delivered.

POC-5 intentionally adds no foreground service, persistent storage, background permission, wake lock, notification, or audio-focus request. Its writer and pending delayed observation are process-local and are stopped on disarm, observed session exit, priority mode, experiment failure, and activity destruction. Android may kill the process, so this PoC does not establish production-grade background reliability.

## Lifecycle expectations

The one-shot routing experiment applies these lifecycle expectations:

1. Explicitly arm one silent active-communication playback run followed by one mode request and exactly one routing request.
2. Snapshot before mutation and after track start, mode request, route request, and the short observation; report track creation/configuration/play state, visible playback configurations, request results, callbacks, and the separately recorded human-audible route.
3. Cancel pending observation, clear the device request, relinquish Private Audio's mode participation, and stop/release silent playback on explicit disarm, observed exit from `MODE_IN_COMMUNICATION`, telephony/system-priority modes, experiment failure, and clean activity destruction.
4. Clear or lose influence when the process is gone; unexpected persistence is a failure to investigate.
5. Never attempt to override real telephony, which retains system priority.
