# Localization Policy

This document is the authoritative localization policy and maintenance workflow for Private Audio. It records the product-language rules established by the existing localization work; individual locale additions remain recorded in `PROGRESS.md`, and presentation architecture remains described in `ARCHITECTURE.md` and decisions D-023 and D-024.

## Authority and evidence

Android resources are authoritative. The default, unqualified `app/src/main/res/values/strings.xml` is the English source and fallback, and translations live in standard Android locale resource directories. Do not introduce a custom translation map or a parallel locale registry unless the platform architecture demonstrably requires one. Android Gradle Plugin-generated locale configuration remains the preferred locale-discovery mechanism.

Keep these evidence levels distinct:

- **Linguistically reviewed** means wording, terminology, script, and orthography have been reviewed for the locale.
- **Source-contract tested** means repository checks establish such properties as XML validity and key/placeholder parity. It does not establish rendering quality.
- **Runtime/emulator/device validated** means the specifically recorded presentation or interaction was observed in that environment. Emulator evidence is not physical-device evidence, and validation of one locale or surface is not validation of every translation.

First-party Android, Google, and product terminology is strong evidence of real platform usage and should remain preferred for Settings when it is semantically correct, natural, current, and appropriate to the locale and surface. It is not unquestionable linguistic authority. For non-obvious or high-risk terminology, triangulate platform wording with contemporary native professional or consumer usage. Do not freeze a platform term solely because it exists when evidence shows it is isolated, outdated, regionally mismatched, misleading, or broadly unnatural. Community complaints may trigger review, but require corroboration and cannot override product semantics.

Do not claim all existing translations have been runtime-validated unless repository evidence establishes that fact.

## Product copy and translation quality

English source strings are product copy, not merely source text. Before translating a new or changed string:

1. confirm that the English meaning is final enough;
2. confirm that it is natural user-facing language;
3. remove unnecessary technical jargon; and
4. stabilize its semantics before propagating it across locales.

Do not propagate weak prototype copy into every translation. A translation must prioritize, in order:

1. semantic correctness;
2. native-speaker naturalness;
3. contemporary product and software terminology;
4. compact UI suitability; and
5. consistency with the rest of that locale.

Translate according to natural local UI conventions rather than word for word. “Dictionary-correct” is not automatically “product-correct.” User-facing routing copy should describe the user's perceived action or state. Do not expose networking-style or implementation-heavy routing jargon unless it is the natural local product term; prefer native Android or audio-switching terminology where available.

Resource validity and parity do not make copy localization-complete without product-language review. When batch quality is uncertain, classify each reviewed string as **PASS** (leave unchanged), **POLISH**, **FIX**, or **HOLD** rather than generating edits merely to show activity. A reviewed-no-change locale is a meaningful audit result. For behavioral Settings labels, explicitly separating the object, action/effect, and trigger/condition can prevent a technically plausible translation from blurring the intended behavior.

Private Audio distinguishes **ON / enabled**—the controller or service is enabled and waiting or available—from **ACTIVE**—Private Audio is currently participating in a qualifying communication-audio session. Translations must preserve both product states: an ON/enabled notification or Power state must not use wording with the same meaning as the runtime Active state. During FULL/MINI review, back-check both concepts whenever both occur. Grammatical similarity is acceptable, but semantic identity is not; when a newly reviewed/frozen locale could plausibly collapse the terms, persistent locale-specific regression assertions must protect both sides.

### Independent locale terminology

Each locale owns its terminology. Do not normalize international technology terms across languages merely for superficial consistency, and do not derive one language's terminology from a neighboring language. Existing approved copy demonstrates that some locales naturally use `AI`, others use localized abbreviations such as `IA`, `KI`, `DI`, `ИИ`, or `ШІ`, and others use full native artificial-intelligence terms, including Arabic and Persian. For each new language, determine the contemporary consumer/software standard independently.

`Private Audio` remains untranslated unless a future explicit branding decision changes it.

`Mini` is a product concept, not an untranslated brand token. Preserve the compact or minimized-controller meaning. Latin `Mini` may remain where it is normal and naturally understood; a natural local-script form or transliteration such as `ミニ`, `미니`, or `Мини`, or an established local equivalent such as Chinese `迷你`, is equally valid. Do not force Latin script for visual consistency, and do not replace a familiar concise form with a longer, purist, artificial, or less familiar alternative merely to localize it. Any choice must remain natural and suitable for the constrained Mini surface; script differences alone are not inconsistency.

### Earpiece and routing distinctions

Private Audio distinguishes the built-in call earpiece or receiver from the loudspeaker/speakerphone, headphones, and Bluetooth audio. Every locale must use terminology that preserves this distinction. Do not accept a generic translation of “speaker” when it makes the intended physical output ambiguous.

### Script and orthography

Use the normal contemporary writing system of the target locale; never Latin-transliterate merely for convenience. In particular, Ukrainian and contemporary Belarusian use their standard Cyrillic forms, Hindi uses Devanagari, Bengali uses Bengali script, Arabic/Persian/Urdu use their proper scripts and orthographic conventions, and Japanese and Korean follow native-script conventions. Simplified and Traditional Chinese are independently reviewed product localizations.

Preserve locale-specific Unicode and orthographic distinctions, including Persian `ی`/`ک`, Turkish dotted/dotless I, Vietnamese diacritics, and Indic combining marks.

## Locale variants

Create separate regional or script variants only when meaningful vocabulary, grammar, script, or UI-convention differences justify them. Current examples are Brazilian and European Portuguese (`pt-BR`, `pt-PT`) and Simplified and Traditional Chinese (`zh-Hans`, `zh-Hant`). Treat each as an independent product localization: do not create a variant merely because a country exists, and do not mechanically convert one variant into another.

### Android legacy ISO language aliases

A product locale's modern BCP-47 logical language tag and its Android resource qualifier are related but are not always textually identical. Before adding or renaming a locale, inspect Android resource matching across the project's current `minSdk` through `targetSdk` and current Android runtime range. Current compatibility-sensitive mappings are Indonesian `id` → `values-in`, Hebrew `he` → `values-iw`, and Yiddish `yi` → `values-ji`.

Use modern identities (`id`, `he`, and `yi`) in product and application logic, but use the legacy Android-compatible qualifier where the supported runtime range requires it. Do not maintain parallel legacy and modern resource trees without platform evidence that both are required; duplicates create parity and maintenance risks. `generateLocaleConfig = true`, valid XML, key parity, and successful `Locale.forLanguageTag()` canonicalization do not prove that Android will resolve a resource.

For a compatibility-sensitive locale, persistent contracts must independently protect its logical identity, exact resource qualifier, generated discovery where applicable, absence of the unsafe parallel modern directory, and ideally real Android resource resolution from the modern logical tag. A `minSdk`, `targetSdk`, Android Gradle Plugin, or other tooling change does not authorize renaming a legacy directory by itself; first re-evaluate resource behavior across the complete supported runtime range.

## Product surfaces and compact copy

Do not apply one wording decision mechanically to every surface:

- **Main:** prioritize clear product language.
- **Settings:** prefer standard platform terminology.
- **Mini:** status labels face strict space constraints. The existing measured, single-line, ellipsized rendering is the layout safeguard; do not silently invent shorter status semantics.

Correct native wording is not shortened merely because English is shorter. When a translation creates layout pressure:

1. verify that it is naturally concise;
2. prefer a shorter native synonym only when semantics remain correct;
3. allow wrapping where the component supports it;
4. introduce a dedicated compact resource only when a component truly requires distinct presentation and after explicit review;
5. use ellipsizing only where acceptable;
6. never reduce typography globally because of one locale; and
7. never replace correct terminology with an inferior abbreviation solely to fit geometry.

Accessibility text may be fuller than a compact visible label when their semantics remain aligned. Detailed layout refinement belongs to the localization UI-polishing workflow and must preserve the accepted component-local fitting rules in D-024.

Compact state variants require demonstrated presentation need and must be reviewed as a complete locale-specific Ready / Waiting / Active / Error status-label system, never as one shortened label in isolation. Exact part-of-speech equality is not required, but the four labels must form one natural grammatical and stylistic paradigm without collapsing their distinct meanings. When natural compact language still narrowly misses measured fit, prefer a small shared geometry accommodation over degrading the translation.

## RTL, fonts, and glyphs

RTL support is shared, direction-driven infrastructure, not locale-specific code. Preserve the established architecture:

- directional chevrons follow layout direction while direction-neutral icons do not mirror merely because a locale is RTL;
- Mini window and drag positioning use physical screen coordinates;
- Mini content mirrors by layout direction and its status uses bidi-aware measured rendering;
- active notification presentation refreshes after a localization configuration change; and
- Compose uses its normally inherited `LayoutDirection`.

Do not create separate RTL logic for Arabic, Urdu, Persian, Hebrew, or another RTL locale. Rely on standard Android bidi handling first for mixed RTL/LTR text, and do not insert manual bidi markers without demonstrated need.

Use Android system font fallback by default. A new script alone does not justify a custom font. For each new writing system, verify glyph coverage, shaping, combining marks, line height and clipping, mixed Latin/local-script text, and Mini rendering. Custom typography requires a demonstrated product need.

## Diagnostics boundary

Technical diagnostic report content and formatting remain stable, English-only output. Do not mix diagnostic-core wording into user-facing localization resources. User-facing diagnostic actions, labels, and messages may be localized. Localization maintenance must not change routing or diagnostic semantics.

## Plurals and placeholders

Never assume English plural rules apply to another locale. Whenever quantity or count strings are introduced:

- use Android `plurals`;
- follow the applicable CLDR plural categories;
- audit every supported locale; and
- pay particular attention to locales with non-English plural behavior.

Nigerian Pidgin (`pcm`) carries a known future-maintenance guardrail: review its CLDR plural behavior independently rather than copying English behavior. This is an example of the general rule, not an exception limited to `pcm`.

Preserve numbered format placeholders and their types across every translation. A localized string may reorder numbered placeholders, but key and placeholder parity must remain complete.

## Maintenance workflow

### Add a locale

1. Review the language's product terminology.
2. Identify uncertain terms and perform a targeted mini-review of them.
3. Freeze the approved copy.
4. Establish the modern BCP-47 identity, research Android legacy-alias behavior across the supported runtime range, and add the Android resource locale with the proven compatible qualifier.
5. Enforce key and placeholder parity.
6. Add locale-specific assertions to the existing localization contract for FULL/MINI-reviewed or otherwise frozen high-risk terminology, including meaningful reviewed Mini states.
7. Perform linguistic and consistency sanity checks.
8. Check script, glyph, shaping, and layout risks.
9. Validate generated discovery and the locale selector.
10. Validate Mini status presentation.
11. Validate shared RTL behavior when applicable.
12. Regress existing locales.
13. Update the authoritative locale/progress record in `PROGRESS.md`, clearly recording the evidence level achieved and validation still pending.

Codex must not silently rewrite approved terminology during implementation. If implementation reveals a genuine linguistic problem, report it for review rather than substituting new wording without approval.

### Change English or add a string

Any change to existing English user-facing copy triggers localization impact review. Add a new localizable key to English first and confirm that it is genuinely user-facing/localizable. For small changes, update all supported locales in the same change whenever practical. English fallback preventing a crash does not make the work localization-complete.

When locale count or review complexity makes an all-locale update unsafe, use explicit controlled language batches. Every batch must start from the same approved English semantics, preserve established terminology, and track pending locales until the full set is complete. Do not allow batches to drift semantically, silently rely on English fallback for already-supported product locales, or leave parity accidentally incomplete.

For either an English change or a new key, preserve placeholders and run key/placeholder parity checks. Larger product-copy changes may use controlled batches to protect linguistic quality; small copy changes should normally update all locales together.

### Automated contracts

Localization checks must verify at minimum:

- locale discovery;
- key parity;
- placeholder parity;
- absence of duplicate keys; and
- XML validity.

Keep generic contracts data-driven where practical to reduce merge conflicts. Retain locale-specific assertions when they protect meaningful terminology, script, or RTL decisions; do not weaken linguistic regression coverage merely to reduce boilerplate.

When the repository's established contract pattern protects frozen high-risk terminology, every new locale with FULL/MINI review or otherwise frozen terminology must add its own durable semantic assertions, including reviewed Mini states when they are meaningful regression targets. Batch work must satisfy this independently for every locale. Key/placeholder parity and ad-hoc Python, Java, or manual checks do not protect approved wording and do not replace persistent contract coverage. Before declaring **PASS**, inspect the CURRENT localization contract and confirm that each newly added locale has that coverage.

### Documentation

When a locale is added, update the existing authoritative localization/progress record. When a localization rule changes, update this policy. Do not create one-off documents for individual languages; this repository has one localization policy.

## Codex maintenance checklist

### New or changed user-facing string

- [ ] English copy reviewed?
- [ ] Resource-based?
- [ ] All supported locales updated or explicitly batched?
- [ ] Key parity checked?
- [ ] Placeholder parity checked?
- [ ] Locale-specific terminology checked?
- [ ] Long-string risk checked?
- [ ] Mini checked?
- [ ] RTL checked if relevant?
- [ ] Script/glyph rendering checked?
- [ ] Diagnostics untouched?

### New locale

- [ ] Language review completed?
- [ ] Uncertain terminology reviewed?
- [ ] Native script and orthography used?
- [ ] Locale resource qualifier correct?
- [ ] Logical tag/resource qualifier compatibility researched across the full supported Android range, with alias-resolution coverage where sensitive?
- [ ] Selector discovery verified?
- [ ] Full string parity checked?
- [ ] Durable semantic contract coverage confirmed for frozen terminology?
- [ ] Mini statuses checked?
- [ ] Earpiece terminology unambiguous?
- [ ] Routing wording natural?
- [ ] RTL and glyph testing completed if applicable?
- [ ] Documentation updated with accurate evidence status?
