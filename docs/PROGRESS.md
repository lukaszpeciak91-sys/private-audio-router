# Progress

## Assistant session continuity experiment

- Added a service-owned, persisted, default-OFF Advanced preference that can retain an established `ASSISTANT` protected cycle for one additional fixed 20-second window after the unchanged 7-second linger. Entry requires the same public unsilenced `VOICE_RECOGNITION` configuration plus a healthy silent track, owned communication mode, current built-in earpiece, and no telephony/system-priority condition.
- Continuity keeps the existing cycle `ACTIVE`, so the established proximity projection remains downstream without another state machine. Assistant/speech resumption cancels the timeout and reuses the same resources; no new track, mode request, communication-device request, routing attempt, retry, polling, or reassertion is added. Recording loss/change/silencing or preference OFF performs reversible cleanup and returns the enabled controller to clean `WAITING`; genuine protected mode, route, earpiece, or silent-track failures retain existing `BLOCKED`/`ERROR` semantics. Lifecycle shutdown, safety-context loss, and expiry remain generation-safe.
- Diagnostics record enablement, activity, timing, the public recording baseline and match result, resume/timeout/abort outcome, and same-context reuse. Deterministic JVM contracts cover default/persistence, OFF behavior, Assistant-only extension, resource reuse, one-request invariants, aborts, stale work, unchanged other origins, and unchanged proximity ownership. Physical validation of the new experiment remains **NOT TESTED / UNKNOWN**.
- The 2026-09-10 Xiaomi evidence associates the observed healthy public recording configuration with Gemini only through the controlled human test. Public Android metadata itself exposes no provider ownership, and implementation and diagnostics remain provider-independent.

## Privacy Summary migration

- The app retains one canonical full English Privacy Policy, kept equal to the public GitHub Pages policy. Each of the 102 non-English resource sets now provides a compact in-app Privacy Summary assembled only from complete semantic units in its previously reviewed policy copy; localized full-policy bodies no longer ship in Android resources.
- The existing localized online-policy action now identifies its unchanged public destination with a small non-translatable `EN` indication. Superseded localized full policies are retained only in `docs/localization-reference/privacy-policy-legacy-2026-09-07.md` as non-authoritative historical terminology and style references, without any native-speaker-validation claim.

## Continuous-WAITING inactivity safeguard

- `PrivateAudioService` now owns a single in-memory delayed deadline for each uninterrupted product-state `WAITING` period. Transition tracking, rather than observer callback frequency, starts the fixed 30-minute period, so snapshots, callbacks, binds, foregrounding, configuration changes, and other same-state evidence cannot restart or extend it.
- Leaving `WAITING` for `ACTIVE`, `READY`, or `ERROR` cancels pending work; every later transition back to `WAITING` starts a fresh full period. Generation validation makes an already-dispatched callback from an older period harmless to a later active or newly enabled session.
- Expiry invokes the established `disarmAndStopStartedLifetime()` Power-OFF path. Explicit Power OFF and service destruction cancel the deadline. The safeguard adds no persistence, polling, retry, worker, alarm, receiver, permission, scheduler service, UI, or localized copy, and preserves fail-closed `START_NOT_STICKY` process restart behavior.
- Deterministic JVM coverage exercises initial scheduling, same-state evidence, WAITING/ACTIVE cycles, explicit and destruction cancellation, stale callbacks, expiry cleanup delegation, and ACTIVE duration beyond 30 minutes. Physical-device validation of timeout expiry and lifecycle cleanup remains pending; no audio-routing behavior is claimed from automated tests.

## Diagnostic email attachment handoff

- Diagnostics now presents separate **Send diagnostic report** and **Save diagnostic report** actions. Send captures the connected service's observationally read-only format-3 report exactly once, retains that frozen string, and writes it unchanged as UTF-8 to a timestamped `.txt` in `cacheDir/diagnostic-share/`. Prior regular files in that dedicated cache directory are removed before the next preparation so retention remains bounded.
- A non-exported AndroidX `FileProvider` with authority `${applicationId}.fileprovider` exposes only `diagnostic-share/` as a temporary `content://` attachment. The one-attachment `ACTION_SEND` message grants read permission only, addresses the existing Napahu Studios support resource, and supplies a universal English support subject and short editable prompt body. The report is not placed in the body. Puzru has no Internet permission, backend, SMTP, automatic upload, or delivery tracking; an external app transmits only if the user explicitly sends there.
- The established manual Save path remains independent and continues to use `ACTION_CREATE_DOCUMENT`. Routing classifiers, requests, prepared tracks, Assistant behavior, proximity, cleanup, retained diagnostic evidence, and report format 3 are unchanged. JVM/source and Compose coverage protects the attachment boundary; physical email-client composition and actual delivery remain **NOT TESTED**.
- The diagnostic email subject and body are deliberately non-translatable support/debugging artifacts and are defined only in the default English resources. The four UI-facing action and feedback strings remain in their current locale-resource state for a separate reviewed translation iteration.
- The canonical English Privacy Policy now covers this user-triggered handoff: local report preparation, temporary app-private cache storage, chooser-based delivery to the selected external app, possible prefilling of the Napahu Studios support address, destination-controlled transmission and copies, and the bounded best-effort cleanup of prior regular share-cache files. Google Play data-disclosure review remains a separate release task.

## Final English About Puzru source

- About localization test maintenance now separates the centralized finalized-copy
  contract from deliberate locale-specific terminology guards. The contract covers
  the authoritative English product semantics, six-section/seven-bullet structure,
  identity tokens, and English fallback. With the rollout complete, the temporary
  finalized-versus-legacy classification has been removed; locale-specific assertions
  remain only for reviewed durable invariants, and obsolete one-sentence About
  assertions remain removed.
- The default, unqualified English `settings_about_body` resource now establishes
  the finalized About Puzru source, including the product purpose, name meaning,
  compatibility boundaries, and explicit limits on third-party service control and
  provider-side privacy. About presents Napahu Studios as Puzru's
  publisher/developer brand.
- The existing Settings About child page and navigation are preserved. Its longer,
  structured body now uses explicit runtime paragraph, heading, and bullet breaks,
  start-aligned text, scrolling, and the shared direction-driven layout behavior;
  the page is also bounded by the existing portrait safe-inset treatment.
- The finalized About rollout is complete across all 102 supported non-English
  locales. Every localized `settings_about_body` now uses the six-section,
  seven-bullet source structure; no legacy one-line About remains, and the
  centralized contract applies uniformly without a legacy exception mechanism.
- The final 17-locale batch was produced and self-checked with the Translation Skill,
  independently audited, corrected where required, and successfully independently
  re-audited. CI is green. With zero legacy resources and centralized contract
  coverage, the rollout is accepted at the **Independently audited** evidence level.
- This acceptance records independent agent audit and source-contract evidence only.
  It does not claim human/native-speaker validation, linguistic review by a human for
  every locale, or runtime/emulator/physical-device validation of every translation.

## English Privacy Policy disclosure alignment

- The canonical English in-app and public policies are aligned with the current Save and Send implementations. They distinguish a user-selected persistent document copy from a temporary app-private cache attachment, Puzru’s local chooser handoff from transmission by a selected external app or service, and prior-share-file cleanup from any unsupported fixed lifetime guarantee for the current cache attachment.
- Following an explicit user-initiated send to Napahu Studios, the policies now disclose that Napahu Studios and its email provider may receive the technical report, sender email address, and user-added message. They state the support-response and Puzru investigation/troubleshooting purposes, retention of Napahu-controlled information only as reasonably necessary for those purposes or applicable legal obligations, deletion requests through `napahustudios@gmail.com`, and the separate privacy practices and controls governing externally controlled copies. The public page is dated September 10, 2026.
- This recipient-side disclosure clarification changes no application behavior, routing, report generation or format, Save/Send implementation, permission, networking, or Data Safety-relevant transmission behavior. It resolves the Privacy Policy disclosure blocker identified during release-compliance review; Play Console form submission remains an external release activity and is not completed by this repository change.
- The authoritative English in-app Privacy Policy now describes the verified current
  implementation at its actual boundaries: no account or microphone permission; no
  microphone or conversation-audio capture; local use of public Android playback and
  recording-session metadata for routing and diagnostics; app-private preferences and
  transient diagnostic state; explicit Android document-provider report export; and
  destination-controlled handling, retention, and deletion after export.
- The disclosure also bounds current network behavior without making a permanent
  architectural promise: this version has no Internet permission, Puzru
  backend or network transmission path, analytics, advertising, or crash-reporting
  service/SDK, and therefore has no server-side diagnostic retention. App-owned data
  remains excluded from cloud backup and device-to-device transfer.
- The existing semantic Settings contract now protects the microphone-audio versus
  public recording-session-metadata distinction, local generation versus persistent
  export, user-controlled destination boundary, current network/service behavior, and
  backup/transfer statement. Production routing and diagnostic behavior are unchanged.
- This change establishes and validates the high-risk English source before translation.
  The first four controlled Translation Skill batches now propagate that source to
  Polish, German, Spanish, Japanese, Arabic, French, Italian, Brazilian and European
  Portuguese, Dutch, Ukrainian, Turkish, Korean, Hindi, Hebrew, Czech, Slovak,
  Slovenian, Croatian, Romanian, Hungarian, Greek, Bulgarian, Lithuanian, Latvian,
  Estonian, Finnish, Swedish, Danish, Norwegian Bokmål, Indonesian, Vietnamese, Thai,
  Simplified Chinese, Traditional Chinese, Malay, Icelandic, Faroese, Kannada,
  Gujarati, Marathi, Telugu, Tamil, Belarusian, Russian, Basque, Albanian, Afrikaans,
  Luxembourgish, Serbian Cyrillic, Macedonian, Serbian Latin, Serbian Latin for
  Montenegro, Bosnian, and Maltese. Privacy Policy localization Batch 5 now also
  propagates that source to Catalan, Galician, Filipino, Swahili, Zulu, Xhosa,
  Javanese, Sundanese, Azerbaijani (Latin), Kazakh, Uzbek (Latin), Armenian,
  Georgian, Persian, Urdu, Bengali, Malayalam, Nepali, Sinhala, and Mongolian.
  Privacy Policy localization Batch 6 propagates that source to Amharic, Assamese,
  Azerbaijani (Arabic, Iran), Cebuano, Kurdish (Latin), Bhojpuri, Maithili,
  Punjabi (Arabic, Pakistan), Punjabi (Gurmukhi, India), Uzbek (Arabic,
  Afghanistan), Uzbek (Cyrillic, Uzbekistan), Cantonese (Simplified, China),
  Cantonese (Traditional, Hong Kong), Hausa, Igbo, Yiddish, Khmer, Lao, Malagasy,
  and Burmese. Privacy Policy localization Batch 7 completes propagation to
  Lingala, Oromo, Odia, Nigerian Pidgin, Pashto, Somali, and Yoruba.
  Each batch preserves the five-paragraph claim structure after high-risk semantic
  and reverse self-checks. The approved Privacy Policy source is now propagated to
  all supported locales; these translations are not presented as independently
  validated. The public publisher/developer and privacy/support contact are now
  defined as non-translatable identity data: Napahu Studios —
  `napahustudios@gmail.com`. Settings presents them separately from the unchanged
  five-paragraph localized policy bodies, and About provides the same actionable
  email contact separately from its finalized descriptive copy.
  The canonical framework-free public Privacy Policy site and a dedicated GitHub
  Pages deployment workflow publish only the public site directory. The verified
  public URL is now exposed as a localized action only in the in-app Privacy Policy
  panel; it opens through Android's external-browser flow, with a safe no-target
  fallback and no app Internet permission.
  Play Console Data Safety and foreground-service declarations, and any future
  user-initiated Share Sheet remain outside this work.

## Dead Settings resource contract reconciliation

- Five frozen-localization JVM contracts no longer expect Serbian, Bosnian,
  Montenegrin, or Zulu phrases that existed only in the intentionally deleted
  `settings_language_body` or `settings_advanced_body` resources. All assertions for
  still-live locale identity, terminology, state, routing, and UI semantics remain;
  no localized resource wording or production behavior changed.

## Final current-lint cleanup and warning non-regression gate

- Android app-data backup remains disabled, and modern data-extraction rules now
  exclude every credential- and device-protected app-owned storage domain from both
  cloud backup and device-to-device transfer.
- The Mini controller reuses its unchanged surface rectangle, and its physical
  left-origin overlay gravity has a declaration-scoped lint suppression documenting
  the separation from direction-driven RTL content.
- The obsolete `settings_language_body` and `settings_advanced_body` resources were
  mechanically removed from the default and every localized inventory without
  rewriting translations. The Compose screen API now places `modifier` first among
  optional parameters without changing named call semantics.
- Module lint policy no longer enforces the stylistic `UseKtx` recommendation.
  Correctness warnings remain actionable, while CI admits only the three exact,
  visible target SDK, Gradle, and compile SDK freshness advisories and rejects every
  other warning by normalized path, issue ID, and message.

## Final Android lint localization-metadata corrections

- The four default Mini state aliases retain locale-selected full-state resolution,
  with `MissingTranslation` suppressed only on those alias declarations. Tamil's
  reviewed compact paradigm and Malayalam's reviewed compact Waiting override remain
  unchanged and continue to take precedence.
- The two experimental Assistant early-route resources are now explicitly
  non-translatable, matching their existing intentional English-fallback scope.
  No English or translated wording, locale inventory, production Kotlin, or routing
  behavior changed.

## Android notification and Compose configuration lint corrections

- The foreground notification's optional configuration-change refresh checks
  `POST_NOTIFICATIONS` on Android 13 and newer, while older Android versions retain
  their existing refresh. The UI now offers a contextual, one-time explanation on
  the first API 33+ Power ON without the grant and requests permission only after
  the primary action. Denial, dismissal, and continuing without notifications all
  preserve foreground-service startup and routing.
- Settings language presentation now observes configuration changes through
  Compose's `LocalConfiguration`, while platform locale discovery and selection
  semantics remain owned by `AppLanguagePreferences`.
- These corrections address the real `NotificationPermission` and
  `LocalContextConfigurationRead` lint errors without changing protected routing
  behavior or adding physical-device evidence. Remaining lint errors belong to the
  separate resource-contract cluster.

## Contextual permission explanations

- Permission UX localization Batch 1 adds the complete atomic eight-string
  notification and Mini explanation bundle to 20 resource directories: Spanish,
  French, German, Russian, Ukrainian, Polish, Italian, Simplified and Traditional
  Chinese, Hindi, Arabic, Persian, European and Brazilian Portuguese, Swahili,
  Urdu, Japanese, Korean, Indonesian, and Tamil. The remaining 82 supported
  non-English locales continue to use intentional English fallback. An independent
  audit requested targeted corrections for Swahili, Korean, and European Portuguese;
  final Batch 1 acceptance remains pending correction checks and targeted independent
  re-audit. No native-speaker validation is claimed.
- Notification and Mini access use separate app-private, false-by-default resolved
  flags. Neither panel appears at startup; each is persisted only after an action,
  Back, or backdrop dismissal, and a visible panel survives activity recreation.
- The first missing-permission Mini attempt explains optional access before the
  existing package Settings handoff. A later deliberate attempt opens Settings
  directly, and return still rechecks the grant before requesting one existing Mini.
- The finalized English permission source copy is implemented as ordinary localizable
  default resources. Supported non-English locales temporarily receive that canonical
  English copy only through Android's normal resource fallback; no locale contains an
  explicit English or mixed-language duplicate. No translation or independent
  localization validation is claimed, and a controlled permission-copy localization
  rollout remains pending. Its test architecture now uses one centralized completed
  locale-directory classification (currently empty) and enforces atomic all-eight-or-none
  bundles, structural and placeholder integrity, and intentional fallback without
  freezing target-language prose. Runtime permission UX implementation is independent
  of that translation status. Each default permission string has an intentional,
  element-scoped `MissingTranslation` suppression; remove the temporary classification
  and all eight suppressions when the controlled rollout reaches every supported locale.
- Automated decision, persistence-boundary, UI-contract, and fallback-resource checks
  cover the implementation. Runtime/device checks remain pending for first use,
  grant/denial/dismissal, repeat attempts, recreation, compact landscape, RTL,
  notification visibility, and unchanged routing. No routing, service,
  notification-channel/lifecycle, or overlay-window behavior changed.

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

- The diagnostic observer now registers a public `AudioManager.AudioRecordingCallback` for its full started lifetime, independently of the Puzru controller setting, and reports bounded meaningful active-recording metadata transitions without microphone permission or audio capture.
- Routing-start markers correlate public recording state with the existing protected-cycle generation and PRE-POC5, silent-track, mode, routing-request, and first-earpiece observations. This is diagnostic-only; classifier, routing, prepared-track/prefill, and assistant-linger behavior are unchanged.
- Physical comparison of controller-OFF and controller-ON sessions remains required.

This document summarizes the **current** repository state. Git history preserves the
per-PR implementation narrative. Accepted decisions, technical evidence, and exact
physical test records remain in [`DECISIONS.md`](DECISIONS.md),
[`RESEARCH.md`](RESEARCH.md), and [`TEST_PLAN.md`](TEST_PLAN.md).

## Current product state

- Puzru is an Android utility that uses public APIs to request the built-in
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

The intended beta is the existing **Puzru** product:

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

**Release identity:** Puzru is the public product name, and Napahu Studios is the
accepted publisher/umbrella brand under D-039. This first public-brand migration
changes release-facing product identity only and does not change routing or
architecture behavior. D-040 establishes `com.napahu.puzru` as the production
`applicationId` before the first Google Play distribution. The Android namespace,
Kotlin packages, and established internal technical naming intentionally remain
`app.privateaudio`; no routing or product behavior changed with the release identity.
The public publisher/developer and privacy/support contact is Napahu Studios —
`napahustudios@gmail.com`.

### Current release-readiness sequence

This is a **CURRENT PLAN**, not a promise or irreversible ordering. Evidence may
reorder, split, remove, or add work:

1. Reconcile the release baseline and current documentation.
2. Establish the CI and release-engineering foundation.
3. Complete the privacy policy and release disclosures.
4. Complete launcher and remaining release branding.
5. Polish permission and foreground-notification transparency.
6. Pass the current-device release-safety physical gates.
7. Prepare a release-candidate build and signed Android App Bundle.
8. Use Google Play Internal Testing as an evidence-gathering stage.
9. Run a cross-device Closed Beta.
10. Consider Production only after sufficient beta evidence.

Before release-candidate freeze, review the remaining Kotlin compiler warnings as a
small engineering-cleanup gate. Resolve low-risk deprecations, annotation-target, and
test-nullability warnings where safe; explicitly classify any behavior-sensitive
Android diagnostics/routing deprecations rather than modernizing them mechanically.
A zero-warning compiler log is not itself a publication requirement, and this review
does not block the current Privacy work merely because warnings remain.

Internal Testing may begin before every cross-device gate is `PASS` because it is
part of evidence gathering, not a Production-readiness claim. Submission-time Play
Console procedures and changing external requirements must be verified when used
rather than frozen here.

### Release-evidence boundary

Puzru is not currently ready for Production. High-priority open evidence
includes outgoing real-call safety and incoming-call boundaries outside the recorded
Active case; Samsung, AOSP-like/Pixel, and newer Android-release coverage; accessory
behavior beyond the recorded already-active Bluetooth preservation scenario;
service/process-loss and reboot behavior; remaining overlay/Mini lifecycle checks;
diagnostic-report save-picker runtime validation; and the unrecorded portions of the
physical portrait/landscape gates for Main and Settings.
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
  Android app-data backup, the five-paragraph Privacy claim set, the `Puzru`
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
- Puzru does not collect, record, or transmit conversation/audio content.
  It locally observes technical Android audio-system metadata and state needed for
  routing and diagnostics; that metadata is distinct from conversation content.
- The user-facing Diagnostics screen is a concise product-health summary: System
  Check shows earpiece, proximity-sensor, and Mini availability; Puzru shows
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
- Detailed diagnostic capture is observationally read-only: it collects a fresh
  current public Android state without invoking the mutating observer path or any
  routing, mode, silent-track, controller, or proximity transition. Format 3 retains
  the latest three completed protected cycles in service/process memory, newest
  first, with stable existing routing-generation IDs; records the latest meaningful
  public playback observation for no-trigger investigation; generalizes established
  earpiece route-loss evidence to any Android-reported replacement; and includes
  public build ID, display, fingerprint, and security-patch evidence. Retention is
  bounded and non-persistent, and no send/share, networking, analytics, permission,
  client attribution, or audio/content capture path was added.
- The latest meaningful public playback evidence now specifically retains the most
  recent non-empty Android playback observation. A later empty playback callback
  cannot erase the no-trigger evidence before a report is generated; a subsequent
  non-empty observation replaces it. This is one bounded in-memory observation and
  does not change playback classification or routing behavior.

## Validation status

- Physical evidence establishes the three routing classes only on the tested Xiaomi
  configuration and applications listed above. It also establishes core proximity
  near/off and far/on behavior. On 2026-09-08, the same Xiaomi `2201117TY`, Android
  13/API 33 scope also passed preservation of already-active Bluetooth routing across
  ChatGPT, Gemini, Grok, and Perplexity, Active incoming-call telephony priority, and
  physical orientation/layout stability. Emulator, JVM, static, and instrumentation
  contracts are not treated as physical routing evidence.
- High-priority release-safety gaps are outgoing real-call priority and incoming-call
  boundaries outside the recorded Active case, plus routing/cleanup/proximity coverage
  on Samsung, an AOSP-like device, and a newer Android release. Bluetooth lifecycle
  cases beyond preservation of an already-active route, other accessories,
  service/process loss, reboot, and remaining UI lifecycle cases are also pending.
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
  transitions, accessory scenarios beyond the recorded Bluetooth-preservation case,
  and real-call scenarios beyond the recorded Active incoming call remains unknown.
  Firefox/Gecko and other browser coverage must not be inferred from Chrome and Mi
  Browser.

## Next meaningful questions

1. Does telephony immediately retain priority for an outgoing call, or for an incoming
   call while Puzru is waiting or retaining an assistant linger context? The Active
   incoming-call case passed on the tested Xiaomi configuration.
2. Does protected POC-5 remain reversible and audibly effective on Samsung, an
   AOSP-like device, and newer Android releases?
3. Does assistant linger improve multi-turn Gemini stability without changing
   communication/browser end behavior or adding routing attempts?
4. Do accessory, service/process-loss, reboot, overlay, Close, and report-saving
   boundaries fail closed under physical/runtime testing?
5. Which public playback configuration, if any, corresponds to the audible ChatGPT
   startup sound on the tested Xiaomi device?

## Recently completed significant changes

- Reconciled supplied 2026-09-08 physical evidence from Xiaomi product `2201117TY`,
  Android 13/API 33, with Private Audio `0.1.0 (1)` where diagnostics confirmed the
  build. Already-active Bluetooth routing was preserved and remained audible across
  controlled ChatGPT, Gemini, Grok, and Perplexity sessions; application versions and
  Bluetooth accessory/profile details were not recorded, and public playback metadata
  did not establish player/package ownership. An Active incoming real call passed with
  telephony priority; its ringtone was noticeable through the active earpiece and was
  accepted without a requested routing change. Physical orientation changes showed a
  stable layout. A Gemini Bluetooth execution also exposed a known semantic/UI
  false-negative: `ERROR` / `ROUTING_NOT_COMPLETED` can follow correct preserved
  Bluetooth behavior because built-in-earpiece confirmation was not reached. No fix
  or broader device, accessory, lifecycle, application-version, or outgoing-call
  validation is claimed.

- Hardened Privacy Policy localization contracts so negative factual guarantees
  require the associated negation, and replaced the obsolete Igbo whole-text digest
  with explicit semantic guards. This test-only maintenance adds no linguistic,
  native-speaker, runtime, or physical-device validation.

- Corrected the audited Privacy Policy copy for isiXhosa and Sinhala by removing
  obsolete legacy guarantees, rewrote Uzbek-AF in the locale's established Uzbek
  Arabic register, and polished Simplified Written Cantonese, Faroese, and
  Luxembourgish while preserving the current five-paragraph English claim set.
  Localization contracts now reject the identified legacy and language-drift
  regressions; this translation self-review is not independent or native-speaker
  validation.

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
