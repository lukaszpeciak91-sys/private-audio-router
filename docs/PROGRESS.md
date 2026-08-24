# Progress

## Assistant early-mode generation-race correction

- The default-OFF Assistant early-route experiment now models track startup, mode-request
  in-flight, mode-ready, promotion, and cancellation explicitly. `MODE_NORMAL` callbacks
  cannot be treated as lost ownership while the current generation's own
  `MODE_IN_COMMUNICATION` request is still in flight.
- Mode completion revalidates its generation before establishing readiness. Cancellation
  invalidates the generation immediately, releases its track, and reconciles a later stale
  mode completion through the existing owned-mode relinquishment guard without mutating a
  newer generation.
- Assistant qualification, early track/mode ordering, the absence of an early device
  request, normal Assistant fallback, Communication and browser paths, the 10-second
  pre-arm timeout, the 1.5-second end confirmation, and the 7-second Assistant linger are
  unchanged.
- Supplied physical/runtime evidence from Private Audio 0.1.0 (1) on Xiaomi `2201117TY`,
  Android 13/API 33, exercised the corrected sequence: stable, unsilenced
  `VOICE_RECOGNITION`; assistant/sonification; early silent-track `PLAYING`; early
  `MODE_IN_COMMUNICATION`; assistant/speech several seconds later; one post-speech device
  request; and `ACTIVE`. No early device request occurred, and human listening reported
  that the response beginning was no longer clipped. This result is specific to the tested
  Xiaomi configuration; broader OEM and Android-release compatibility remains unknown.

## Public recording-session metadata diagnostics

- The diagnostic observer now registers a public `AudioManager.AudioRecordingCallback` for its full started lifetime, independently of the Private Audio controller setting, and reports bounded meaningful active-recording metadata transitions without microphone permission or audio capture.
- Routing-start markers correlate public recording state with the existing protected-cycle generation and PRE-POC5, silent-track, mode, routing-request, and first-earpiece observations. This is diagnostic-only; classifier, routing, prepared-track/prefill, and assistant-linger behavior are unchanged.
- Physical comparison of controller-OFF and controller-ON sessions remains required.

This document summarizes the **current** repository state. Git history preserves the
per-PR implementation narrative. Accepted decisions, technical evidence, and exact
physical test records remain in [`DECISIONS.md`](DECISIONS.md),
[`RESEARCH.md`](RESEARCH.md), and [`TEST_PLAN.md`](TEST_PLAN.md).

## Current product state

- Private Audio is an Android utility that uses public APIs to request the built-in
  earpiece for compatible communication audio. It controls Android routing and does
  not receive, record, capture, proxy, or transmit conversation audio.
- The service-owned permanent controller exposes `READY`, `WAITING`, `ACTIVE`, and
  `ERROR`. Power ON waits without owning a route until qualifying playback appears;
  every participating cycle is bounded, reversible, and returns to waiting or
  fails closed.
- The protected POC-5 path plays locally generated silence, participates in
  communication mode, makes one earpiece request, observes the result, and uses one
  ordered cleanup path. Telephony and system-priority modes retain priority.
- The application is usable through the main screen, Settings, and an optional
  floating Mini controller. Proximity screen-off behavior is service-owned,
  preference-controlled, and downstream of established `ACTIVE` evidence.

## Routing and controller state

- Automatic routing recognizes three provider-independent public-metadata classes:
  `COMMUNICATION`, exact assistant/speech `ASSISTANT`, and the established
  browser-communication signature. Ordinary media and assistant sonification alone
  are excluded. All qualifying classes share the protected POC-5 execution body.
- Physical testing on Xiaomi `2201117TY`, Android 13/API 33, confirms audible
  earpiece routing for communication-class applications (ChatGPT, Grok, Perplexity,
  and Character.AI), Gemini Live assistant-class audio, and ChatGPT Web in Chrome
  and Mi Browser. Chrome supplied the exact browser signature; the Mi Browser
  metadata was not recorded. These results do not establish other devices, Android
  releases, applications, or browser engines.
- Confirmed cycles retain one route request, reversible cleanup, session-end return
  to `WAITING`, and later-session re-arm. The exact matrices and evidence boundaries
  are authoritative in [`TEST_PLAN.md`](TEST_PLAN.md).

## Current UI and product surfaces

- Main provides Power, current four-state status, Settings, Mini launch, and full
  Close. Portrait remains the canonical Main visual layout; compact-height landscape
  phones now use an intentional three-area composition with product information at
  logical start, the existing Power control centered, and secondary actions at
  logical end. Main and Mini consume the same service-owned state projection.
- Settings provides language selection, proximity preference, Advanced experiments,
  Diagnostics, About, and Privacy Policy. Settings-modal child pages such as
  Language, Advanced, About, and Privacy Policy reuse the existing single-dialog
  family and return to the Settings root; Diagnostics opens its dedicated
  release-facing screen.
- Mini is a service-owned overlay with status, Power, Expand, drag, and overlay-only
  Close. It normally resolves full localized states through aliases and applies one
  shared measured 16f / 15f / 14f size without locale-specific geometry. Tamil is
  an explicit compact-state exception; Malayalam now has a physical-failure-driven,
  reviewed compact Waiting override, while Gujarati uses the natural full-state path after targeted
  review. PR #173's direction resolver remained a **PHYSICAL FAIL** on Xiaomi
  `2201117TY`, Android 13/API 33: Yiddish resources resolved, but both Main and Mini
  still presented in LTR; the Malayalam compact Waiting label physically fit. The
  current fix canonicalizes Android/Java legacy locale identities in the localization
  layer and shares that effective presentation direction with the Compose root and Mini.
  Correct Yiddish RTL presentation from the new build remains physically unverified.
- The dedicated Privacy Policy page is resource-backed, scrollable within its
  bounded sheet, dismissible from outside, and has visible and Android Back paths to
  Settings. Source and Compose contracts cover its structure and navigation;
  runtime scroll interaction is not recorded.

## Localization state

- Resources currently provide **94 product locales: English plus 93 non-English
  resource sets**. This count is derived from the current `values*` resource
  inventory, excluding non-locale `values-night`; supported variants include the
  intentional Portuguese, Chinese, Serbian, Punjabi, and Azerbaijani script or
  regional distinctions and Android-compatible legacy qualifiers.
- English/default resources are authoritative. The established 28-string stable
  product surface has locale-specific review and source-contract evidence recorded
  in repository history. Sundanese (`su`, `values-su`) now covers the complete
  localizable product surface with translation-production semantic self-checks,
  including controller ON versus runtime ACTIVE, built-in earpiece versus
  loudspeaker, and the five-paragraph Privacy Policy claim set. Independent audit,
  human/native-speaker review, and Sundanese runtime, glyph, Mini-presentation,
  emulator, and physical-device validation remain pending. Broad runtime and
  physical-device validation is not implied.
- Cantonese for Hong Kong (`yue-Hant-HK`, `values-b+yue+Hant+HK`) covers the
  complete localizable product surface in professional Hong Kong Written Cantonese.
  Translation-production semantic self-checks preserve controller ON `開啟` versus
  runtime ACTIVE `使用中`, built-in earpiece `聽筒` versus loudspeaker `喇叭`, and
  all five Privacy Policy paragraphs, including `咪高風`, `互聯網`, `元數據`, and
  the distinction between App unexpected-termination reporting and a user-saved
  diagnostic report. The natural full Mini state paradigm is retained without a
  compact override. An independent Cantonese audit identified an invented `system`
  actor in the rejected-routing-request error and an extra local/on-device location
  qualifier in Privacy Policy paragraph 3; corrective candidates were applied through
  the Translation Skill and passed final independent delta re-audit. Human/native-speaker
  review and runtime, glyph, Mini-presentation, emulator, and physical-device validation
  remain pending.
- The reviewed Mini label now uses established local-script or semantic forms in
  Bulgarian, Bengali, Greek, Gujarati, Hebrew, Yiddish, Khmer, Kannada, Macedonian,
  Marathi, Serbian Cyrillic, Tamil, and Telugu. Other locales retain their independently
  appropriate strategies; this wording maintenance does not add runtime or device evidence.
- Sinhala (`si`) now has a complete natural-script product localization, including
  reviewed distinctions for controller ON versus runtime ACTIVE, built-in earpiece
  versus loudspeaker, and the high-risk Privacy Policy claims. The translation was
  semantically self-checked; independent audit, human/native review, and runtime
  Sinhala rendering validation remain pending.
- The Privacy Policy title and complete factual claim set are translated and
  semantically self-checked across all 93 non-English sets. A targeted independent
  audit found ten claim-scope or terminology defects, and reviewed replacements were
  applied. All 94 product resource sets now preserve its five semantic paragraphs
  with functional Android escapes; the Yoruba and Igbo corrections changed only
  paragraph encoding, not lexical content. Shared source and representative Android
  resource-resolution contracts protect that structure. This is neither
  human/native-speaker validation nor legal certification.
- The current user-facing Diagnostics key inventory is localized across all 93
  supported non-English resource sets. Candidates were produced and semantically
  self-checked with the Translation Skill. Malagasy received an independent
  linguistic audit that identified targeted corrections for the `ACTIVE` state,
  the About description's voice-audio scope, and the Privacy Policy title; corrective
  candidates were applied, with final independent delta re-audit still pending and
  no human/native-speaker validation claimed. The prior independent audit of the 86
  previously supported non-English sets identified targeted defects in Polish,
  Indonesian, Khmer, Swahili, and Afrikaans, as well as a locale-unsafe shared
  grammatical-status contract; those corrective candidates and context-specific
  status resources were applied through the Translation Skill, using Android
  terminology as evidence where semantically appropriate. The previously corrected
  Javanese and Zulu distinctions remain protected. The detailed technical diagnostic
  report and SUPPORT SUMMARY remain intentionally English-only, as does the
  default-OFF Assistant early route experiment copy.
- Durable terminology, evidence semantics, Mini fitting, RTL behavior, factual-claim
  safeguards, and locale maintenance rules belong to
  [`LOCALIZATION.md`](LOCALIZATION.md); this document records only current coverage
  and unresolved localization work.

## Privacy and diagnostics state

- No account or sign-in is required. The manifest requests neither microphone nor
  Android Internet permission, app-data backup is disabled, and the application
  uses no analytics, advertising, or crash-reporting services.
- Private Audio does not collect, record, or transmit conversation/audio content.
  It locally observes technical Android audio-system metadata and state needed for
  routing and diagnostics; that metadata is distinct from conversation content.
- The user-facing Diagnostics screen is a concise product-health summary: System
  Check shows earpiece, proximity-sensor, and Mini availability; Private Audio shows
  routing intent, product state, and current audio output; Last Routing Attempt appears
  only after a completed cycle and shows a result plus a concise error only on
  failure. Device/environment and classifier details are intentionally omitted.
- The English-only saved technical report begins with a stable support summary and
  retains the complete device/environment, Android/API/version, TriggerOrigin,
  playback, routing-attempt, snapshot, completed-cycle, and proximity evidence.
- Diagnostic data is generated and processed locally. Saving the detailed UTF-8
  report occurs only after a user action through Android's Storage Access Framework,
  requires no storage permission, and uses a frozen snapshot so service unbinding
  cannot change the selected report. Physical save-picker validation remains
  pending.

## Validation status

- Physical evidence establishes the three routing classes only on the tested Xiaomi
  configuration and applications listed above. It also establishes core proximity
  near/off and far/on behavior. Emulator, JVM, static, and instrumentation contracts
  are not treated as physical routing evidence.
- High-priority release-safety gaps are incoming and outgoing real-call priority,
  plus routing/cleanup/proximity coverage on Samsung, an AOSP-like device, and a
  newer Android release. Accessory, service/process-loss, reboot, and remaining UI
  lifecycle cases are also pending.
- Runtime checks remain pending for portions of Settings/Privacy scrolling,
  diagnostic report saving, overlay permission and recreation, Mini drag/bounds and
  task reuse, Close boundaries, and process death. See
  [`TEST_PLAN.md`](TEST_PLAN.md) for exact statuses rather than inferring completion
  from implementation or automated coverage.

## Active experiments and unresolved items

- **Prepared silent-track prefill and startup timing — implemented; physical gate pending.**
  Clean enabled `WAITING` now gives its one initialized, stopped POC-5 track a
  bounded one-shot non-blocking zero-PCM prefill. It still has no continuous writer,
  playback, mode, route, `ACTIVE`, or proximity influence. Genuine routing calls
  `play()` before starting the maintenance writer and preserves the unchanged
  PLAYING → mode → single-earpiece sequence; prefill failure remains fail-open.
  Generation-scoped monotonic diagnostics cover trigger through the first observed
  earpiece and report unavailable cross-generation values. The 7-second assistant
  linger was not redesigned. Startup improvement and absence of OEM-visible active
  playback from prefill remain physically unverified.
- **Assistant protected-session linger — implemented and physically exercised; final gate pending.**
  After the existing 1.5-second assistant playback-loss confirmation, an established
  cycle retains its track, mode, route, and proximity eligibility for at most 7
  seconds. Exact assistant/speech resumption reuses the cycle without another route
  attempt; expiry and safety/lifecycle overrides use protected cleanup. Focused JVM
  contract coverage exists. Physical Gemini evidence confirms same-cycle resource
  reuse within the linger and shows that 5 seconds narrowly missed some later turns;
  the tuned 7-second duration and full multi-turn regression gate remain pending.
- **Assistant early silent-track pre-arm — implemented, default OFF, not physically validated.**
  Fake Phone was replaced rather than duplicated. Exact `VOICE_RECOGNITION` plus
  assistant/sonification may start the prepared silent track during `WAITING`, then
  establish `MODE_IN_COMMUNICATION` only after `PLAYSTATE_PLAYING`. It makes no early
  device request, attempt, `ACTIVE` transition, or proximity acquisition. Healthy
  assistant/speech reuses the playing track and established mode before the single
  protected device request; other origins cancel and use their unchanged paths.
  Cleanup is generation-safe and bounded at 10 seconds. Physical clipping reduction
  and recording stability with the early mode remain `NOT TESTED / UNKNOWN`.
- The ChatGPT startup-sound signature and route remain unresolved. Diagnostics can
  record bounded playback metadata, but the trace neither classifies nor reroutes
  the sound by itself.
- Stability across untested devices, OEMs, Android/software updates, lifecycle
  transitions, accessories, and real calls remains unknown. Firefox/Gecko and other
  browser coverage must not be inferred from Chrome and Mi Browser.

## Next meaningful questions

1. Does telephony immediately retain priority while Private Audio is waiting,
   active, or retaining an assistant linger context?
2. Does protected POC-5 remain reversible and audibly effective on Samsung, an
   AOSP-like device, and newer Android releases?
3. Does assistant linger improve multi-turn Gemini stability without changing
   communication/browser end behavior or adding routing attempts?
4. Do accessory, service/process-loss, reboot, overlay, Close, and report-saving
   boundaries fail closed under physical/runtime testing?
5. Which public playback configuration, if any, corresponds to the audible ChatGPT
   startup sound on the tested Xiaomi device?

## Recently completed significant changes

- Added the localized Privacy Policy Settings surface and corrected targeted
  high-risk translation defects.
- Strengthened localization handling for privacy, security, permission, account,
  diagnostics, backup, and data-handling claims.
- Disabled Android app-data backup.
- Added the release-facing Diagnostics screen while retaining the separate detailed
  report and user-triggered local save flow.
- Implemented the bounded assistant-session linger experiment without promoting its
  unexecuted physical gate to established behavior.
