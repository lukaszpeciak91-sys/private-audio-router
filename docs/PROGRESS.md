# Progress

## Android notification and Compose configuration lint corrections

- The foreground notification's optional configuration-change refresh now checks
  `POST_NOTIFICATIONS` on Android 13 and newer, while older Android versions retain
  their existing refresh. The permission is declared without runtime request UX;
  foreground-service startup and Private Audio routing remain independent of whether
  notification permission is unavailable or denied.
- Settings language presentation now observes configuration changes through
  Compose's `LocalConfiguration`, while platform locale discovery and selection
  semantics remain owned by `AppLanguagePreferences`.
- These corrections address the real `NotificationPermission` and
  `LocalContextConfigurationRead` lint errors without changing protected routing
  behavior or adding physical-device evidence. Remaining lint errors belong to the
  separate resource-contract cluster.

## Android lint CI visibility audit

- Android CI now runs the existing JVM and assembly gates separately from Android
  lint, then prints the complete generated lint text report while preserving lint's
  original failing exit status. Current lint errors therefore remain release-gating
  without a baseline, suppression, severity change, or `abortOnError` bypass.
- This audit changes no application production code, resources, permissions, or
  protected routing behavior. The exposed inventory is the input to a separate
  implementation and product-decision step; lint findings are not fixed here.

## Localization inventory contract reconciliation

- Localization architecture contracts now follow the current app-owned Android
  resource inventory, including the intentional Latin, Cyrillic, and Arabic Uzbek
  variants. Qualifier tests protect exact Android locale identities rather than
  obsolete language-wide directory counts.
- Generated platform locale configuration remains authoritative from build-time
  resource discovery through the language picker; no parallel language registry was
  introduced. Platform-derived names are tested for identity instead of incidental
  JVM capitalization.
- Lexical translation findings remain separate and have not been silently
  normalized. Translated resources, production routing, and unrelated application
  behavior are unchanged.

## Non-localization JVM baseline reconciliation

- The RTL source contract now scopes its hardcoded-language-registry protection to
  the declarations that resolve Main and Mini presentation direction through the
  platform locale-direction API. Incidental locale tokens elsewhere in UI source no
  longer produce unrelated failures, while the architecture chain remains covered.
- The remaining known JVM baseline failures are localization-contract findings;
  production code, resources, and runtime behavior are unchanged.

## Remaining protected-audio test-contract reconciliation

- Remaining protected-audio ownership assertions now distinguish executable calls
  from comments and diagnostic text. Provider independence is checked against the
  routing and public-audio-metadata classification path rather than diagnostic prose.
- Proximity isolation now verifies that its preference and synchronization paths do
  not route, clear routes, change communication mode, or start protected routing,
  without freezing unrelated historical routing-operation totals.
- Production routing behavior is unchanged. Reconciliation of the broader failing
  JVM baseline remains in progress.

## Kotlin source-contract extraction reconciliation

- Brittle raw-brace Kotlin source extraction in the affected contract tests has been
  replaced with shared, offset-preserving structural extraction that ignores braces
  in strings and comments. Declaration matching now avoids freezing irrelevant
  visibility where the behavior is the contract.
- Protected routing semantics and Mini behavior remain unchanged. Reconciliation of
  the broader failing JVM baseline remains in progress.

## JVM source-contract ownership reconciliation

- Source-contract tests now distinguish the single executable communication-device
  request owner from diagnostic prose and other harmless textual references. The
  declaration and ownership semantics remain protected without requiring a symbol
  name to appear only once across all production text.
- This test-harness correction changes no routing or product behavior. Reconciliation
  of the broader failing JVM baseline remains in progress.

## Android CI and release-engineering foundation

- GitHub Actions now validates pull requests targeting `main` and pushes to `main`
  on one Linux/JDK 17 environment using the checked-in Gradle Wrapper. The gate runs
  debug JVM unit tests, Android lint, a debug build, and an unsigned release-bundle
  build.
- These automated checks establish source/build health only. They do not run
  instrumentation or device tests and do not establish physical routing, audible
  output, OEM, accessory, telephony, or lifecycle evidence.
- Repository ignore rules now protect common local Android keystores and signing
  property files. Real release signing, signed AAB generation/upload, and publishing
  remain unconfigured; branch protection remains a separate repository setting after
  the new check has run successfully.
- This completes the initial CI/release-engineering-foundation stage of the current
  release-readiness sequence without changing the V1/public-beta product baseline.

## Portrait Main compile correction

- A Compose receiver-scope compile regression in portrait Main is corrected by
  capturing the `BoxWithConstraints` width before entering its nested `Box`. The
  existing width calculation and approved portrait and landscape visual behavior
  are unchanged, and no routing behavior changed.

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

## Current V1 / public-beta release baseline

This baseline is the current intended V1/public-beta scope and release path unless
new evidence gives a concrete reason to revise it. It is a directional baseline,
not an immutable feature freeze: physical testing, audits, Play/platform
requirements, privacy constraints, architectural findings, or implementation
experience may justify changing scope or sequence. Evidence-based change is
allowed; speculative scope drift is not.

The intended beta is the existing **Private Audio** product:

- an Android-only, provider-independent utility for compatible communication-audio
  routing to the built-in earpiece through the established protected POC-5 path;
- the service-owned permanent controller and its `READY`, `WAITING`, `ACTIVE`, and
  `ERROR` states;
- Main, including its intentional compact-height landscape composition; Settings,
  including compact-height behavior; Diagnostics and user-triggered local saving of
  a diagnostic report; the optional Mini floating controller; and service-owned
  proximity screen behavior; and
- per-app language selection with the current localization coverage described below,
  within the existing local, privacy-oriented architecture.

The V1 boundary remains public Android APIs, no root, Shizuku, Accessibility
Service, or MediaProjection, no capture or proxying of third-party audio, no
provider authentication, and no network dependency unless separately approved.
Real telephony retains priority. Every routing cycle remains reversible and
fail-closed. [`PROJECT_CHARTER.md`](PROJECT_CHARTER.md) remains authoritative for
the project's scope and non-goals; this baseline does not rewrite it.

The current V1/public-beta implementation target does **not** include a new Fake
Phone product mode, speculative new routing classes, unrelated audio-routing
experiments, rebuilding the established protected POC-5 path, or universal support
for every Android sound or application before beta. Fake Phone is a possible future
product/research direction, not a beta blocker. The superseded D-030, D-034, and
D-036 experiments are historical evidence, not its future specification; any
future Fake Phone mode requires fresh research and specification in a separate,
evidence-driven development program.

**Release identity:** Private Audio remains the working V1 product name. The
current `app.privateaudio` identity remains provisional under D-010; this baseline
does not decide or change it. A final package identity and launcher/release branding
decision must be made consciously before the first Google Play release/upload,
where a later identity change would be problematic.

### Current release-readiness sequence

This is a **CURRENT PLAN**, not a promise or irreversible ordering. Evidence may
reorder, split, remove, or add work:

1. Reconcile the release baseline and current documentation.
2. Establish the CI and release-engineering foundation.
3. Complete the privacy policy and release disclosures.
4. Decide final application identity and launcher/release branding.
5. Polish permission and foreground-notification transparency.
6. Pass the current-device release-safety physical gates.
7. Prepare a release-candidate build and signed Android App Bundle.
8. Use Google Play Internal Testing as an evidence-gathering stage.
9. Run a cross-device Closed Beta.
10. Consider Production only after sufficient beta evidence.

Internal Testing may begin before every cross-device gate is `PASS` because it is
part of evidence gathering, not a Production-readiness claim. Submission-time Play
Console procedures and changing external requirements must be verified when used
rather than frozen here.

### Release-evidence boundary

Private Audio is not currently ready for Production. High-priority open evidence
includes incoming and outgoing real-call safety; Samsung, AOSP-like/Pixel, and newer
Android-release coverage; accessory behavior; service/process-loss and reboot
behavior; remaining overlay/Mini lifecycle checks; diagnostic-report save-picker
runtime validation; and physical portrait/landscape validation of Main and Settings.
[`TEST_PLAN.md`](TEST_PLAN.md) is authoritative for the exact gates and statuses;
this summary intentionally does not duplicate its test matrix.

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
  release-facing screen. The sheet now removes its portrait-only vertical offset
  when height is compact, remains inside safe-drawing vertical bounds, and gives
  Root, Advanced, and generic child content a scroll fallback. Normal-height
  portrait geometry remains unchanged; Language and Privacy Policy retain their
  existing bounded lazy lists, and Diagnostics retains its full-screen scroll.
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

- Resources currently provide **103 product locales: English plus 102 non-English
  resource sets**. This count is derived from the current `values*` resource
  inventory, excluding non-locale `values-night`; supported variants include the
  intentional Portuguese, Chinese, Serbian, Punjabi, Azerbaijani, and Uzbek script or
  regional distinctions and Android-compatible legacy qualifiers.
- Generated locale discovery is filtered from that app-owned resource inventory,
  preventing dependency-only translations from being advertised as product
  languages. Exact-set instrumentation coverage canonicalizes `in`/`iw`/`ji` to
  `id`/`he`/`yi`; focused Android resource contracts cover representative regional
  matching, Portuguese family fallback, legacy aliases, and intentionally missing
  script variants. Execution of those Android contracts still requires an available
  emulator or device.
- English/default resources are authoritative. The established 28-string stable
  product surface has locale-specific review and source-contract evidence recorded
  in repository history. Sundanese (`su`, `values-su`) now covers the complete
  localizable product surface with translation-production semantic self-checks,
  including controller ON versus runtime ACTIVE, built-in earpiece versus
  loudspeaker, and the five-paragraph Privacy Policy claim set. Independent audit,
  human/native-speaker review, and Sundanese runtime, glyph, Mini-presentation,
  emulator, and physical-device validation remain pending. Broad runtime and
  physical-device validation is not implied.
- Uzbek now has three distinct product identities: the existing Latin `uz` resource
  continues to serve `uz-Latn-UZ`, while `uz-Cyrl-UZ` and `uz-Arab-AF` have exact
  Cyrillic/LTR and Arabic/RTL resource trees. The two new candidates cover the same
  localizable keys as existing Uzbek and preserve the reviewed product-state,
  earpiece/loudspeaker, routing/transmission, communication-audio, Mini, and
  five-paragraph Privacy distinctions. Translation-production semantic self-checks
  include targeted Afghan Uzbek corrections for artificial intelligence and the
  Privacy Policy title. Independent audit, human/native review, runtime glyph and
  shaping, picker, Mini-presentation, emulator, and physical-device validation remain
  pending.
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
- Simplified-script Cantonese for mainland China (`yue-Hans-CN`,
  `values-b+yue+Hans+CN`) is now a separate LTR product localization rather than a
  conversion or fallback from Hong Kong Cantonese. Its independently produced
  candidate uses contemporary Simplified Written Cantonese with mainland software
  terminology and preserves controller ON `开启` versus runtime ACTIVE `使用紧`,
  built-in earpiece `听筒` versus loudspeaker `扬声器`, audio-output switching versus
  transmission, `通信音频`, local diagnostics and report-saving scope, disabled
  Android app-data backup, the five-paragraph Privacy claim set, the `Private Audio`
  brand, and localizable `迷你`. Source contracts cover keys, placeholders, NFC,
  protected semantics, script-specific locale identities, LTR direction, generated
  locale discovery, picker distinction, and independent Android resource resolution.
  Independent linguistic audit, human/native-speaker review, and runtime, glyph,
  Mini-presentation, emulator, and physical-device validation remain pending.
- Oromo (`om`, `values-om`) now covers the complete localizable product surface in
  Qubee/Latin and uses the generic LTR resource tree for Oromo regional fallback.
  Translation-production semantic self-checks preserve controller ON versus runtime
  ACTIVE, the built-in upper call earpiece versus loudspeaker, and all five Privacy
  Policy paragraphs. The natural full four-state Mini path is retained without a
  compact override. Independent audit, human/native-speaker review, and runtime,
  glyph, Mini-presentation, emulator, and physical-device validation remain pending.
- Bhojpuri (`bho`, `values-b+bho`) now covers the complete current localizable
  product surface in modern Devanagari with LTR presentation. The generic BCP-47
  resource tree serves the primary `bho-IN` locale without a duplicate regional tree.
  Translation-production semantic self-checks and the terminology evidence gate,
  including a Targeted Terminology Ledger for uncertain technical concepts, preserve
  controller ON versus runtime ACTIVE, the built-in upper phone earpiece versus
  loudspeaker, and all five Privacy Policy paragraphs. The natural full four-state
  Mini path is retained without a compact override. Independent audit,
  human/native-speaker review, and runtime, glyph, Mini-presentation, emulator, and
  physical-device validation remain pending.
- Maithili (`mai`, `values-b+mai`) now covers the complete current localizable
  product surface in contemporary professional Maithili, using Devanagari and LTR
  presentation. The generic BCP-47 tree serves the primary Android `mai-IN` identity
  without a duplicate regional tree. Translation-production semantic self-checks,
  the terminology evidence gate, and a Targeted Terminology Ledger preserve controller
  ON versus routing-cycle ACTIVE, the built-in upper call earpiece versus loudspeaker,
  audio routing versus transmission, communication audio versus conversation content,
  and all five Privacy Policy paragraphs. The natural full four-state Mini path is
  retained without a compact override. Independent audit, human/native-speaker review,
  and runtime, glyph, Mini-presentation, emulator, and physical-device validation
  remain pending.
- Kurmanji / Northern Kurdish (`ku`, canonical `ku-Latn`,
  `values-b+ku+Latn`) now covers the complete current localizable product surface in
  contemporary Latin/Hawar orthography with LTR presentation, intentionally separate
  from Sorani (`ckb`) and Arabic-script Kurdish. The region-neutral script-qualified
  tree targets Android's standard Kurdish user locale `ku-TR`; locale identity and the
  expected likely-script target are contract-checked, and host ICU maximizes `ku-TR` to
  `ku-Latn-TR`, while direct Android runtime resource resolution remains unexecuted. Translation-production
  semantic self-checks, the terminology evidence gate, and a Targeted Terminology Ledger
  preserve controller ON versus current routing-cycle ACTIVE, the built-in upper call
  receiver versus loudspeaker, audio-output routing versus transmission, communication
  audio versus conversation content, and all five Privacy Policy paragraphs. The natural
  full four-state Mini wording is retained without a compact override. Independent audit,
  human/native-speaker review, generated LocaleConfig inspection, and runtime, glyph,
  Mini-presentation, emulator, and physical-device validation remain pending.
- Cebuano (`ceb`, `values-b+ceb`) now covers the complete current localizable
  product surface in contemporary Latin-script Cebuano with LTR presentation. The
  generic BCP-47 resource tree serves the primary Android `ceb-PH` identity without
  a duplicate regional tree. Translation-production semantic self-checks preserve
  controller ON versus runtime ACTIVE, the built-in phone earpiece versus
  loudspeaker, audio routing versus data transmission, communication audio versus
  conversation content, and the complete five-paragraph Privacy Policy claim set.
  Independent audit, human/native-speaker review, and runtime, Mini-presentation,
  emulator, and physical-device validation remain pending.
- Lingala (`ln`, `values-ln`) now covers the complete current localizable product
  surface in contemporary Latin-script Lingala with LTR presentation. One generic
  resource tree serves Android's `ln-CD`, `ln-CG`, `ln-AO`, and `ln-CF` regional
  identities; the likely CLDR identity is `ln-Latn-CD`, and no duplicate regional
  tree is maintained. Translation-production semantic self-checks preserve
  controller ON versus runtime ACTIVE, the built-in phone earpiece versus the
  loudspeaker, audio-output routing versus transmission, communication audio versus
  conversation content, and all five Privacy Policy paragraphs. The natural full
  four-state Mini path is retained without a compact override. Independent audit,
  human/native-speaker review, generated LocaleConfig inspection, and runtime,
  glyph, Mini-presentation, emulator, and physical-device validation remain pending.
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
  semantically self-checked across all 97 non-English sets. A targeted independent
  audit found ten claim-scope or terminology defects, and reviewed replacements were
  applied. All 98 product resource sets now preserve its five semantic paragraphs
  with functional Android escapes; the Yoruba and Igbo corrections changed only
  paragraph encoding, not lexical content. Shared source and representative Android
  resource-resolution contracts protect that structure. This is neither
  human/native-speaker validation nor legal certification.
- The current user-facing Diagnostics key inventory is localized across all 97
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
- Runtime checks remain pending for compact-height Settings/Privacy scrolling,
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
- **Assistant early silent-track pre-arm — implemented, default OFF, experimental and device-scoped.**
  Fake Phone was replaced rather than duplicated. Exact `VOICE_RECOGNITION` plus
  assistant/sonification may start the prepared silent track during `WAITING`, then
  establish `MODE_IN_COMMUNICATION` only after `PLAYSTATE_PLAYING`. It makes no early
  device request, attempt, `ACTIVE` transition, or proximity acquisition. Healthy
  assistant/speech reuses the playing track and established mode before the single
  protected device request; other origins cancel and use their unchanged paths.
  Cleanup is generation-safe and bounded at 10 seconds. **FACT:** supplied physical
  and runtime evidence on Xiaomi `2201117TY`, Android 13/API 33, exercised stable
  unsilenced recognition, early track `PLAYING`, early mode, speech promotion, one
  post-speech device request, and `ACTIVE`; human listening reported that the response
  beginning was no longer clipped. **UNKNOWN:** cross-device, OEM, Android-release,
  accessory, and telephony compatibility. **PRODUCT STATUS:** the feature remains
  experimental and default OFF.
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

- Reviewed the final 12 historical localization JVM findings across Cebuano,
  Lingala, Malagasy, Xhosa, Yoruba, Catalan, Galician, Malayalam, Zulu, Hausa,
  and Pashto. A follow-up delta review reverted the temporary Xhosa and Zulu
  wording candidates because it did not establish sufficient evidence to replace
  the existing translation baseline. No translation-resource change is retained:
  the remaining findings were stale or overly broad lexical source contracts,
  including inflection, capitalization, grammatical context, legitimate device
  labels, Privacy guard boundaries, and diagnostic-report occurrence counts.
  Locale qualifiers, discovery, and resource inventory remain unchanged. JVM
  baseline reconciliation is source-contract maintenance, not translation
  re-authoring or linguistic acceptance; its results remain separate from model
  linguistic self-review and do not constitute human or native-speaker validation.

- Reconciled the remaining non-localization UI, Mini, and RTL source contracts with
  the approved implementation shape. Mini control regions and RTL mirroring are now
  covered semantically through their pure helpers rather than historical inline
  implementation text; Settings language-selection navigation and Overlay reuse and
  fail-closed lifecycle semantics remain unchanged. No production routing behavior
  changed, and broader JVM baseline reconciliation remains in progress.
- Decoupled diagnostic snapshot data from Android process observation: PID and UID
  are captured explicitly at the observer boundary and preserved in real diagnostic
  evidence, while empty and synthetic snapshots remain explicitly unobserved and
  plain JVM diagnostic projection tests no longer require the Android `Process`
  runtime. Broader JVM baseline reconciliation remains in progress, and routing
  behavior is unchanged.
- Corrected the Layer41 localization placeholder contract’s Map-key filtering compile
  regression while preserving the resource-key-to-ordered-placeholder association. No
  translations, product behavior, or routing behavior changed.
- Strengthened the repository Translation Skill with technical-sense reconstruction,
  proportional terminology evidence, reverse-referent checks, uncertainty handling,
  and safeguards against self-confirming lexical regression tests.
- Added the localized Privacy Policy Settings surface and corrected targeted
  high-risk translation defects.
- Strengthened localization handling for privacy, security, permission, account,
  diagnostics, backup, and data-handling claims.
- Disabled Android app-data backup.
- Added the release-facing Diagnostics screen while retaining the separate detailed
  report and user-triggered local save flow.
- Implemented the bounded assistant-session linger experiment without promoting its
  unexecuted physical gate to established behavior.
