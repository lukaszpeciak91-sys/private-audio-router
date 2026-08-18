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

One local, non-exported `PrivateAudioService` is the sole production owner of one `AudioDiagnosticObserver`, its callbacks, in-memory evidence, routing experiment, and indirectly its silent track and writer. `MainActivity` binds while visible and renders the service-owned observable state. Activity destruction only unbinds; it neither disarms nor destroys the observer. Recreated activities bind to the same service instance when it survives.

Binding alone permits a bound-only service and does not create a notification. The visible UI's explicit Arm action establishes started-service lifetime; `onStartCommand()` immediately promotes the service using the declared `specialUse` foreground-service type before invoking the existing arm operation. Disarm runs the existing cleanup, removes foreground state and notification, and stops the started lifetime. A remaining activity binding may keep the same observer alive for diagnostics. Genuine service destruction stops the observer and unregisters callbacks. Restart is fail-closed (`START_NOT_STICKY`), with no persistence or automatic re-arm; process death can bypass `onDestroy()` and loses in-memory evidence.

Layer 1.6 supersedes the Layer 1.5 repeated-one-shot completion callback with a permanent, service-owned controller. Power ON registers one public `AudioPlaybackCallback` and enters clean waiting: it owns no silent track, writer, mode participation, or device override. Ordinary media does not qualify. A routing cycle starts only when an active public playback configuration has `USAGE_VOICE_COMMUNICATION` plus `CONTENT_TYPE_SPEECH`, Android is already in communication mode on the built-in speaker, an earpiece is available, and no priority mode blocks it.

During a cycle, the protected POC-5 sequence is unchanged. Once the local track is playing, two or more matching public playback contributions establish the inference that external communication exists alongside the known local contribution. A later callback showing only the local contribution creates a 1.5-second candidate; cleanup occurs only if that condition remains stable and the controller/cycle generation is still current. The configurations provide neither package nor client identity. Cleanup invalidates delayed work, clears the route, relinquishes mode, stops the writer, and stops/flushes/releases the track. If still enabled, the controller creates a fresh cycle identity and returns directly to clean waiting.

Disable clears enabled intent first, invalidates delayed work, unregisters playback observation, and invokes protected cleanup before leaving foreground/stopping. Service destruction does the same fail-closed work; process death loses non-persisted intent. There is no retry, route reassertion, polling, provider detection, or automatic restoration.

The foreground notification is a minimum low-importance lifetime disclosure whose tap returns to `MainActivity`. There is no notification action, media style, runtime notification-permission flow, wake lock, audio-focus request, persistence, boot restart, or background-triggered foreground-service start. The protected POC-5 algorithm and cleanup paths remain unchanged.

## Product state projection

`PrivateAudioService.privateAudioState` is the single read-only product-state source for Main and floating-controller UI consumers. It is a pure projection of the observable enabled intent and current observer evidence, with this precedence: OFF is `READY`; a current protected block or rejected routing request is `ERROR`; a currently participating cycle with Android reporting both `MODE_IN_COMMUNICATION` and the built-in earpiece is `ACTIVE`; every other enabled condition is `WAITING`.

The enabled intent, diagnostic snapshot, and experiment evidence use Compose snapshot state. Reading `privateAudioState` from a composition therefore observes controller changes as well as current mode, device, cycle, cleanup, and failure evidence without adding Flow or a parallel lifecycle. Historical earpiece success and route reversion are not projection inputs. Consumers must not reproduce the mapping, and the projection owns no routing API or controller decision.

## Localization

English product copy in `res/values/strings.xml` is the complete default resource set and source of truth, with translated product resources supplied through standard Android locale directories. The module declares `en-US` as its unqualified-resource locale and enables Android Gradle Plugin locale-config generation, so Android's standard per-app language infrastructure discovers supported resource locales without a custom translation map or localization framework. On API 33 and later, Settings applies per-app language selection through `LocaleManager`; the scrollable language page returns to Settings root after a selection. A running floating controller refreshes its localized state presentation on configuration change without changing window or controller ownership. API 31–32 remain system-language-only. Diagnostic evidence and report formatting remain stable, English-only technical output rather than product localization content.

## Overlay lifecycle

`OverlayService` is a local, non-exported, `START_NOT_STICKY` owner of at most one `TYPE_APPLICATION_OVERLAY` window. Main checks `Settings.canDrawOverlays()` before showing it; a missing grant opens Android's package-specific overlay-permission screen, and Main checks the actual grant again on resume. Denial or cancellation creates no window. The service also checks permission at the creation boundary.

`PrivateAudioService` remains the authoritative controller and state owner. `OverlayService` owns only the floating window and its lifecycle: it binds to the existing controller service, consumes the existing `PrivateAudioState` projection, and delegates floating Power to the controller's established enable/disable actions. The overlay has no audio detection or routing APIs, no diagnostic observer, and no independent state projection.

The floating controller is draggable and presents `STATUS → POWER → EXPAND → CLOSE`. Its X closes only the overlay window and overlay service; it does not disable routing or remove Main's task. Expand restores or reuses the existing Main task and then removes the overlay, without altering controller or routing state. Main's full Close first requests overlay removal, then performs the established controller shutdown and task removal. Activity backgrounding or recreation does not own or duplicate the window while its service owner remains alive. No desired-open state is persisted, no overlay is restored automatically, and `START_NOT_STICKY` keeps process death fail-closed.

## Lifecycle expectations

The permanent controller applies these lifecycle expectations:

1. Power ON enters track-free waiting and observes active playback through the public callback.
2. Each qualifying external communication session receives one isolated POC-5 cycle with exactly one route request.
3. Stable loss of the external contribution, explicit OFF, priority blocking, failure, or service destruction performs complete reversible cleanup.
4. Confirmed normal end returns to waiting while enabled; OFF and process loss leave no controller influence.

## Layer 7A proximity-screen ownership

`PrivateAudioService` remains the lifecycle authority. The existing observer emits a minimal synchronous evidence-change notification after it refreshes authoritative experiment and Android route/mode evidence. The service then evaluates one predicate: enabled, `ACTIVE`, `MODE_IN_COMMUNICATION`, built-in earpiece, and platform support. This notification does not request routing, retry work, or introduce a second state machine.

`ProximityScreenController` isolates only `PowerManager` mechanics. It creates at most one app-owned `PROXIMITY_SCREEN_OFF_WAKE_LOCK`, makes it non-reference-counted, and provides idempotent acquire/release and transition diagnostics. It knows nothing about playback detection, providers, routing, or UI. Normal release uses no wait-for-proximity flag and no timeout; the authoritative routing-cycle lifetime bounds ownership. Power OFF and service destruction add explicit fail-safe releases after existing audio cleanup calls, without changing that cleanup's internal ordering.

Main and Floating remain consumers/delegates only. Hiding, expanding, recreating, or backgrounding either surface does not itself change proximity ownership.
