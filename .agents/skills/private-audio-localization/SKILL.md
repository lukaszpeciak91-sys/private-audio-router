---
name: private-audio-localization
description: Apply Private Audio's localization-quality workflow when adding a locale; translating, changing, or auditing translations; adding or changing English/default user-facing strings or other localizable resources; reviewing locale-specific Android UI terminology; maintaining localization or plurals/placeholders; or handling RTL, script, orthography, and language-specific UI copy. Do not use for unrelated Android routing, diagnostics, AudioTrack, or other implementation work unless it also changes user-facing localizable copy or locale behavior.
---

# Private Audio localization workflow

**Before translating or modifying localized product copy, read `docs/LOCALIZATION.md`, then inspect the CURRENT default strings and CURRENT locale inventory in the repository. Do not rely on remembered locale lists or previous task prompts.** The policy remains authoritative; this skill operationalizes it and is not a translation dictionary.

## Establish scope and source meaning

1. Inspect the current localization resources, contracts, approved terminology, and task-relevant surfaces.
2. For every new or changed English/default user-facing string, gate propagation by asking:
   - Is it natural product English with stable, unambiguous meaning?
   - What are its object, action/effect, state, and trigger/condition?
   - Which hardware component and user-perceived behavior does it describe?
   - Is it overly technical, telegraphic, or prototype copy?
   - Would propagation create a poor translation baseline?
3. For behavioral or Settings copy, explicitly decompose subject/object, action/effect, trigger/condition, state versus action, hardware component, and user-perceived behavior where relevant. In particular, do not blur the screen, phone, proximity trigger, or screen power-off behavior.
4. If source copy is materially weak or ambiguous, stop propagation and report **`SOURCE COPY REVIEW REQUIRED`** with the problem. Correct a small obvious defect only when the task explicitly authorizes changing English copy. Technical validity alone is not localization readiness.

## Assess language quality

For each locale, prefer in order: semantic correctness, native-speaker naturalness, contemporary Android/software terminology, compact UI suitability, and locale-internal consistency. Ask whether the wording could plausibly appear in a professionally localized Android app—not merely whether a native speaker could understand it. Avoid literal translations and calques.

Classify audit results without manufacturing edits:

- **PASS:** natural professional product language; do not change it.
- **POLISH:** correct and understandable, with a meaningfully better native product wording available.
- **FIX:** misleading, semantically wrong, substantially unnatural, or clearly poor product copy.
- **HOLD:** insufficient evidence to choose responsibly among plausible alternatives.

For every proposed language change, record **assessment confidence** and **replacement confidence** as `HIGH`, `MEDIUM`, or `LOW`. A confident diagnosis need not imply a confident replacement. Never silently implement a `LOW`-confidence replacement. Record reviewed-no-change locales as meaningful results.

## Back-check semantic relationships

Before finalizing a locale, perform a source-to-target semantic back-check for strings whose meaning depends on an important relationship or distinction. Scale the check to semantic risk, with particular attention to system versus device, app versus system, screen versus phone/device, enabled versus active, state versus action, source versus destination, built-in earpiece versus loudspeaker/headphones, trigger versus effect, subject/object relationships, and accessibility-control semantics.

Ask whether the target preserves the same subject, object, referent, and relationship between components; whether it accidentally collapses two distinct source concepts; and whether grammatically natural wording implies different behavior. A target that changes or collapses those semantics is **`FIX`**. Grammatical naturalness never overrides semantic fidelity.

Treat **ON / enabled versus ACTIVE** as a mandatory product-state check whenever the source contains both concepts. Before translating, reconstruct them independently: Power ON or an enabled controller/service means Private Audio is waiting or available, while Active means it is currently participating in a qualifying communication-audio session. Verify that the target does not collapse those meanings; semantic identity is **`FIX`**, although grammatical similarity is acceptable. Power ON, service/controller enabled, waiting/ready, and runtime Active are not interchangeable states. Preserve both concepts independently in reviewed/frozen copy, and add persistent locale-specific contract assertions for both when the distinction is a meaningful regression target.

For a linguistically non-obvious or high-risk string, briefly reconstruct the target meaning in plain English or equivalent source-language semantics. This reverse gloss is a sanity check—not machine back-translation offered as proof of quality—for swapped subjects and objects, lost distinctions, wrong referents, missing triggers, and altered action/state semantics. If the reconstructed meaning differs materially from the source, stop and review. Apply this proportionally; trivial labels such as `Close`, `Error`, and `Settings` do not each require a reverse gloss.

## Resolve terminology independently

- During every FULL locale review, explicitly resolve artificial-intelligence terminology for that locale: determine whether contemporary consumer/software usage naturally prefers `AI`, a localized abbreviation such as `AA`, `MI`, `KI`, or `UI`, or a full native-language term. Never inherit `AI` automatically from English, another or related locale, or a previous task prompt. Prefer current first-party Android/Google/product terminology, then other high-quality native technology sources; when the reviewed choice is meaningfully locale-specific and frozen, protect it in the locale-specific semantic contract.
- Do not infer one locale from a related language or normalize terminology or grammatical aspect across a language family. Treat Polish/Czech/Slovak, Ukrainian/Belarusian/Russian, Arabic/Persian/Urdu, `pt-BR`/`pt-PT`, and `zh-Hans`/`zh-Hant` independently. Related locales are sanity comparisons, not linguistic authorities. A Polish improvement does not invalidate other Slavic recurring-action constructions.
- Do not apply linguistic purism. An international loanword such as AI, Error, Advanced, Default, earpiece, audio, or Settings vocabulary may be the most natural contemporary choice; conversely, use an established local Android term where one exists.
- When terminology is uncertain, prefer evidence from Android/Google UI or documentation, major Android device vendors, Unicode CLDR for locale/script conventions, then high-quality native technology sources. Do not treat machine-translation sites as linguistic authority. Without web access, do not fabricate platform evidence; state confidence honestly and use `HOLD` when needed.
- Preserve the policy's native-script and orthography requirements. Check Persian `ی`/`ک`, Turkish dotted/dotless I, Vietnamese diacritics, Indic and Thai combining marks, Arabic-script shaping, the correct CJK variant, and mixed local script with `Private Audio` or international technical terms where relevant. Never introduce ASCII/transliteration shortcuts.
- Preserve the distinction between the **built-in call earpiece/receiver** and loudspeaker, speakerphone, headphones, earbuds, or Bluetooth audio. Research a better term if a generic “speaker” equivalent is ambiguous.
- Prefer native user concepts such as audio switching or directing/redirecting audio over network-style routing jargon, without changing product semantics.

## Execute the relevant workflow

### Add a locale

1. Inspect current English/default strings and derive the current locale/key inventory.
2. Review core terminology and product states—including an explicit ON/enabled-versus-ACTIVE back-check when both occur—identify and research uncertainty, then freeze approved copy.
3. Implement standard Android resources without silently rewriting approved copy. Report genuine language problems instead of improvising.
4. Verify key and placeholder parity, generated discovery, script/orthography, Mini labels, and shared RTL/bidi behavior where applicable.
5. For each locale, add durable locale-specific assertions to the existing localization contract for FULL/MINI-reviewed or otherwise frozen high-risk terminology, including meaningful reviewed Mini states. Ad-hoc Python, Java, or manual checks and parity alone are not substitutes.
6. Regress existing locales and update `docs/PROGRESS.md` with accurate evidence levels.

### Add or change a source string

1. Pass the English source-copy gate and establish semantic intent.
2. Derive the CURRENT supported locale inventory from the repository and assess impact across every supported locale.
3. Update all locales in one change when practical; otherwise use explicit controlled batches and track every `HOLD` or pending locale. English fallback is not completion.
4. Distinguish changed locales from reviewed-no-change locales and run parity checks. Avoid the false-success pattern of mechanical translation followed only by passing contracts.

### Introduce quantities

Use Android `plurals`, follow CLDR categories, and audit every locale rather than copying English assumptions. Retain the Nigerian Pidgin (`pcm`) future-plural guardrail in `docs/LOCALIZATION.md`.

## Keep linguistic and layout review separate

Do not weaken correct language merely to match English length. First seek a naturally shorter equivalent while preserving semantics, then evaluate supported wrapping. Use compact variants only with explicit need and review, ellipsize only where acceptable, never globally shrink typography, and never invent abbreviations solely to fit geometry.

## Review accessibility and control descriptions as spoken language

When an accessibility or control description enumerates controls or actions, verify grammatical parallelism, a consistent action form, clear control semantics, and natural screen-reader phrasing. Do not mix infinitive, imperative, and noun forms unless that pattern is a genuine native convention. A list conceptually equivalent to **Enable / Expand / Close** should use one natural parallel grammatical pattern in the target locale. Review accessibility copy as spoken product language, not merely as a complete set of translated words.

## Apply the final linguistic review gate

Do not award final **PASS** solely because XML is valid, keys and placeholders match, required terminology is present, or individual sentences are grammatical. Before **PASS**, explicitly verify semantic fidelity to the source, native Android/product naturalness, relationship and referent fidelity, action/state fidelity, and—where applicable—accessibility grammar. Scale this gate to semantic risk rather than turning trivial labels into heavy ceremony.

## Minimize changes and report evidence precisely

- Do not rewrite `PASS` strings, improve neighboring keys opportunistically, normalize languages, or turn a focused audit into a locale-wide rewrite.
- Change only approved strings plus required parity, test, and documentation updates.
- Keep **linguistically reviewed**, **source-contract tested**, and **runtime/emulator/device validated** evidence separate. Parity cannot prove native quality, XML cannot prove rendering, and emulator evidence cannot prove physical behavior.
- Keep durable policy in `docs/LOCALIZATION.md` and meaningful completed progress in `docs/PROGRESS.md`; do not create per-language documentation.

## Validate against the current repository

Use the repository's CURRENT test architecture. As applicable, check XML validity, duplicate keys, key parity, placeholder parity, generated locale discovery, relevant localization contracts, and RTL regressions. Before declaring **PASS**, inspect the CURRENT localization contract and confirm durable semantic coverage for every newly added locale in a batch; key/placeholder parity does not protect frozen terminology. Derive assertion literals from the final implemented resource text, not memory, freeze notes, dictionary/base forms, or an earlier review draft, and preserve the resource's exact grammatical form in inflected or agglutinative languages. Verify that every new locale-specific literal assertion occurs in its corresponding CURRENT resource; if Gradle cannot run, perform an equivalent static source check to catch assertion/resource drift. Also scan conflict markers and run `git diff --check` and `git status --short`. Report Gradle environment/network limitations separately; never change Gradle or Java configuration merely to run localization tests.
