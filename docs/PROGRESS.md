# Progress

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
  Close. Main and Mini consume the same service-owned state projection.
- Settings provides language selection, proximity preference, Advanced experiments,
  Diagnostics, About, and Privacy Policy. Settings-modal child pages such as
  Language, Advanced, About, and Privacy Policy reuse the existing single-dialog
  family and return to the Settings root; Diagnostics opens its dedicated
  release-facing screen.
- Mini is a service-owned overlay with status, Power, Expand, drag, and overlay-only
  Close. It normally resolves full localized states through aliases and applies one
  shared measured 16f / 15f / 14f size without locale-specific geometry. Tamil is
  the current explicit compact-state exception; Gujarati uses the natural full-state
  path after targeted review.
- The dedicated Privacy Policy page is resource-backed, scrollable within its
  bounded sheet, dismissible from outside, and has visible and Android Back paths to
  Settings. Source and Compose contracts cover its structure and navigation;
  runtime scroll interaction is not recorded.

## Localization state

- Resources currently provide **87 product locales: English plus 86 non-English
  resource sets**. This count is derived from the current `values*` resource
  inventory, excluding non-locale `values-night`; supported variants include the
  intentional Portuguese, Chinese, Serbian, Punjabi, and Azerbaijani script or
  regional distinctions and Android-compatible legacy qualifiers.
- English/default resources are authoritative. The established 28-string stable
  product surface has locale-specific review and source-contract evidence recorded
  in repository history; broad runtime, emulator, glyph, Mini-presentation, and
  physical-device validation is not implied.
- The Privacy Policy title and complete factual claim set are translated and
  semantically self-checked across all 86 non-English sets. A targeted independent
  audit found ten claim-scope or terminology defects, and reviewed replacements were
  applied. This is neither human/native-speaker validation nor legal certification.
- The current user-facing Diagnostics key inventory is localized across all 86
  supported non-English resource sets. Candidates were produced and semantically
  self-checked with the Translation Skill; an independent audit identified targeted
  Javanese earpiece and Zulu audio-route terminology defects, and corrective
  candidates were applied. A final natural-UI pass then replaced overly literal or
  implementation-heavy wording for the Mini control, required-permission status,
  and last-routing heading in every non-English set. Final independent re-audit
  remains pending. The detailed
  technical diagnostic report and SUPPORT SUMMARY remain intentionally English-only,
  as does the default-OFF Fake Phone experiment label.
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
  Check shows earpiece, proximity-sensor, and floating-control availability; Private
  Audio shows routing intent, product state, and current route; Last Routing appears
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

- **Assistant protected-session linger — implemented, not physically validated.**
  After the existing 1.5-second assistant playback-loss confirmation, an established
  cycle retains its track, mode, route, and proximity eligibility for at most 15
  seconds. Exact assistant/speech resumption reuses the cycle without another route
  attempt; expiry and safety/lifecycle overrides use protected cleanup. Focused JVM
  contract coverage exists, but execution evidence is not recorded here and the
  multi-turn physical gate remains `NOT TESTED / UNKNOWN`.
- **Fake Phone pre-arm — implemented, default OFF, not physically validated.** While
  eligible and waiting, it may prepare a silent communication context and one route
  request without changing `WAITING`; qualifying playback may promote those
  resources. It does not establish application ownership or audible success, and
  the controlled Xiaomi gate remains `NOT TESTED / UNKNOWN`.
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
