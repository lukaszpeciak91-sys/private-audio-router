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

The initial prototype must **not** change `AudioManager.MODE`. A future change requires evidence and a separate accepted decision. The sequence is: request a communication device first, observe system behavior, and only then decide whether additional mechanisms deserve investigation. A failed test is not authorization to escalate.

## Lifecycle expectations

1. Enable the routing request explicitly.
2. Observe and report the resulting communication device.
3. Clear the request on disable.
4. Clear or lose influence when the process is gone; unexpected persistence is a failure to investigate.
5. Never attempt to override real telephony, which retains system priority.
