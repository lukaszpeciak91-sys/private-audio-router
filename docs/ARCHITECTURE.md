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

The armed controller recognizes three logically separate, provider-independent public-Android signatures. `COMMUNICATION` requires `MODE_IN_COMMUNICATION`, `USAGE_VOICE_COMMUNICATION`, `CONTENT_TYPE_SPEECH`, and the existing communication eligibility. `ASSISTANT` requires exact `USAGE_ASSISTANT` + `CONTENT_TYPE_SPEECH` and may begin from `MODE_NORMAL`. `BROWSER_COMMUNICATION` requires `MODE_IN_COMMUNICATION`, the built-in speaker, and exact `USAGE_VOICE_COMMUNICATION` + `CONTENT_TYPE_UNKNOWN`. No package, provider, UID, PID, browser, URL, foreground-app, or usage-history identity participates. Ordinary `USAGE_MEDIA` and assistant `CONTENT_TYPE_SONIFICATION` without speech are excluded.

All three origins converge on one protected POC-5 execution body. It records the pre-change state, creates a public `AudioTrack` with `USAGE_VOICE_COMMUNICATION`, `CONTENT_TYPE_SPEECH`, mono PCM 16-bit output and a device-supported native voice-call sample rate, and feeds only zero-valued samples using bounded non-blocking writes on one background-priority thread. After the track reports `PLAYSTATE_PLAYING`, it explicitly requests `MODE_IN_COMMUNICATION` and makes exactly one earpiece request. Observation follows without retry or reassertion, and every exit uses the same reversible cleanup.

The silent track contains no microphone, captured, proxied, forwarded, or external audio and the experiment requests no audio focus. It used 48 kHz on the tested Xiaomi device. Public playback configurations do not expose enough client identity here to distinguish two applications with matching voice-communication attributes, so a common playback device is recorded only as “where observable,” not attributed conclusively to ChatGPT. API acceptance and an Android-reported route remain distinct from audible routing; physical listening is the final routing truth. Telephony/system-priority modes block and disarm the experiment; cleanup cancels observation, clears the communication-device request, sets `MODE_NORMAL` to relinquish Private Audio's mode participation, stops the writer, and stops, flushes, and releases the `AudioTrack`.

On Xiaomi `2201117TY` running Android 13/API 33, the current POC-5 caused Android to report the built-in earpiece and a human tester repeatedly heard active ChatGPT Voice through the upper built-in earpiece. This establishes the architecture on that tested configuration, not general compatibility or production-grade stability.

The diagnostic evidence model uses public, read-only process identity/importance and active playback configuration APIs at pre-ownership, post-mode-ownership, post-request, and delayed-observation boundaries. The list from `getActivePlaybackConfigurations()` is treated as the active playback snapshot; entries describe only the public attributes, capture policy, and device Android makes visible to this application. They neither contain audio nor establish application identity. Effective mode ownership remains external evidence that must be correlated by PID/UID with `dumpsys audio`.

## Diagnostic lifecycle

One local, non-exported `PrivateAudioService` is the sole production owner of one `AudioDiagnosticObserver`, its callbacks, in-memory evidence, routing experiment, and indirectly its silent track and writer. `MainActivity` binds while visible and renders the service-owned observable state. Activity destruction only unbinds; it neither disarms nor destroys the observer. Recreated activities bind to the same service instance when it survives.

Binding alone permits a bound-only service and does not create a notification. The visible UI's explicit Arm action establishes started-service lifetime; `onStartCommand()` immediately promotes the service using the declared `specialUse` foreground-service type before invoking the existing arm operation. Disarm runs the existing cleanup, removes foreground state and notification, and stops the started lifetime. A remaining activity binding may keep the same observer alive for diagnostics. Genuine service destruction stops the observer and unregisters callbacks. Restart is fail-closed (`START_NOT_STICKY`), with no persistence or automatic re-arm; process death can bypass `onDestroy()` and loses in-memory evidence.

Layer 1.6 supersedes the Layer 1.5 repeated-one-shot completion callback with a permanent, service-owned controller. Power ON registers one public `AudioPlaybackCallback` and enters clean waiting: it owns no silent track, writer, mode participation, or device override. A routing cycle starts only when one of the three signatures above qualifies, an earpiece is available, and no telephony/system-priority mode blocks it.

During a cycle, the protected POC-5 sequence is shared and unchanged. Communication and assistant lifecycle inference distinguish external evidence from the known local contribution; browser lifecycle inference follows disappearance of its voice-communication/unknown contribution. A qualifying end must remain stable for 1.5 seconds before cleanup, tolerating transient playback recreation. The configurations provide neither package nor client identity. Cleanup invalidates delayed work, clears the route, relinquishes mode, stops the writer, and stops/flushes/releases the track. If still enabled, the controller creates a fresh cycle identity and returns directly to clean waiting.

Disable clears enabled intent first, invalidates delayed work, unregisters playback observation, and invokes protected cleanup before leaving foreground/stopping. Service destruction does the same fail-closed work; process death loses non-persisted intent. There is no retry, route reassertion, polling, provider detection, or automatic restoration.

The foreground notification is a minimum low-importance lifetime disclosure whose tap returns to `MainActivity`. There is no notification action, media style, runtime notification-permission flow, wake lock, audio-focus request, persistence, boot restart, or background-triggered foreground-service start. The protected POC-5 algorithm and cleanup paths remain unchanged.

## Product state projection

`PrivateAudioService.privateAudioState` is the single read-only product-state source for Main and floating-controller UI consumers. It is a pure projection of the observable enabled intent and current observer evidence, with this precedence: OFF is `READY`; a current protected block or rejected routing request is `ERROR`; a currently participating cycle with Android reporting both `MODE_IN_COMMUNICATION` and the built-in earpiece is `ACTIVE`; every other enabled condition is `WAITING`.

The enabled intent, diagnostic snapshot, and experiment evidence use Compose snapshot state. Reading `privateAudioState` from a composition therefore observes controller changes as well as current mode, device, cycle, cleanup, and failure evidence without adding Flow or a parallel lifecycle. Historical earpiece success and route reversion are not projection inputs. Consumers must not reproduce the mapping, and the projection owns no routing API or controller decision.

## Localization

English product copy in `res/values/strings.xml` is the complete default resource set and source of truth, with translated product resources supplied through standard Android locale directories. The module declares `en-US` as its unqualified-resource locale and enables Android Gradle Plugin locale-config generation, so Android's standard per-app language infrastructure discovers supported resource locales without a custom translation map or localization framework. On API 33 and later, Settings applies per-app language selection through `LocaleManager`; the scrollable language page returns to Settings root after a selection. A running floating controller refreshes its localized state presentation on configuration change without changing window or controller ownership. API 31–32 remain system-language-only. Diagnostic evidence and report formatting remain stable, English-only technical output rather than product localization content.

[`LOCALIZATION.md`](LOCALIZATION.md) is authoritative for product-language policy, locale maintenance, validation evidence levels, and the new/changed-copy workflow. This section owns only the implemented localization architecture.

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

## Layer 7 proximity-screen ownership

`PrivateAudioService` remains the lifecycle and product authority. The existing observer emits a minimal synchronous evidence-change notification after it refreshes authoritative experiment and Android route/mode evidence. The service then evaluates one predicate: persisted proximity feature enabled, controller enabled, `ACTIVE`, `MODE_IN_COMMUNICATION`, built-in earpiece, and platform support. The service owns the default-ON boolean in one private `SharedPreferences` entry and exposes only its current value and an idempotent setter to Main Settings. A changed value synchronizes that same predicate immediately; it does not change product state, session detection, or audio behavior.

`ProximityScreenController` isolates only `PowerManager` mechanics. It creates at most one app-owned `PROXIMITY_SCREEN_OFF_WAKE_LOCK`, makes it non-reference-counted, and provides idempotent acquire/release and transition diagnostics. It knows nothing about playback detection, providers, routing, or UI. Normal release uses no wait-for-proximity flag and no timeout; the authoritative routing-cycle lifetime bounds ownership. Power OFF and service destruction perform explicit fail-safe release before their existing observer cleanup, without changing that audio cleanup's internal ordering.

Main and Floating remain consumers/delegates only. Hiding, expanding, recreating, or backgrounding either surface does not itself change proximity ownership.

The Layer 7C closure audit confirmed this remains one downstream chain: `AudioDiagnosticObserver` refreshes authoritative session/mode/route evidence, `PrivateAudioService` projects `PrivateAudioState` and combines it with the persisted preference in the sole eligibility decision, and `ProximityScreenController` performs only the one public wake-lock operation. There is no `SensorManager`, periodic proximity polling, UI-owned proximity path, second proximity state machine, or proximity-triggered routing action. Proximity-mechanics failure is contained in the helper and does not project product `ERROR` or alter the permanent routing controller.

## Automatic assistant-class trigger

Assistant detection is an independent input branch into the existing protected POC-5 execution body, not a loose generic trigger or a second routing algorithm. The standard communication branch still requires `MODE_IN_COMMUNICATION`, public `USAGE_VOICE_COMMUNICATION` + `CONTENT_TYPE_SPEECH` playback, and the built-in speaker as the reported communication device. Independently, the assistant branch requires an armed controller, public `USAGE_ASSISTANT` + `CONTENT_TYPE_SPEECH` playback, no telephony/system-priority mode, and an untouched current-cycle attempt guard. It intentionally permits `MODE_NORMAL` before participation. `USAGE_MEDIA`, unknown content, and assistant sonification do not qualify.

Both branches select a small diagnostic trigger origin, capture pre-change evidence, and then share the same ordered silent-track, playing-state confirmation, explicit communication-mode request, mode confirmation, single earpiece request, observation, and cleanup mechanics. Assistant session-end inference follows disappearance of the assistant/speech contribution rather than changing the established voice-communication contribution semantics. Proximity remains downstream of the same ACTIVE evidence and contains no assistant-specific behavior.

## Automatic browser communication trigger

Browser communication detection is a third independent origin available whenever the controller is armed. It requires `MODE_IN_COMMUNICATION`, a built-in-speaker communication device, and at least one exact public `USAGE_VOICE_COMMUNICATION` + `CONTENT_TYPE_UNKNOWN` playback configuration, while retaining the shared telephony and one-attempt guards. It does not identify a browser or provider. Ordinary media, unknown content alone, and communication mode alone cannot qualify.

`BROWSER_COMMUNICATION` enters the same protected POC-5 body. Because Private Audio's local silent contribution remains voice-communication/speech, browser session presence and the delayed end confirmation count only voice-communication/unknown configurations. Communication continues to use its established two-speech-contribution lifecycle, and assistant continues to use assistant/speech disappearance.
