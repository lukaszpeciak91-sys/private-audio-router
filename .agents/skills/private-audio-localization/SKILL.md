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

- Semantic correctness is non-negotiable. Among semantically correct alternatives, prefer natural contemporary native product language. First-party Android, Google, and product terminology is strong evidence of real platform usage and often the best answer, but is not unquestionable linguistic authority: it must match the meaning, UI surface, locale or region, and reasonably current usage. Do not manufacture research for trivial established terms. For non-obvious, high-risk, disputed, unusually literal, dated-looking, or freeze-worthy terminology, cross-check first-party wording against contemporary native professional or consumer usage. Useful corroboration includes major software or device vendors, reputable native technology sources, operator or device documentation, and genuine native-speaker usage. Do not freeze isolated, outdated, regionally mismatched, misleading, or broadly unnatural first-party wording solely because it exists. Community or forum complaints are review signals and corroboration, not sole authority; popular wording cannot override product semantics. When strong evidence conflicts and no professional replacement is sufficiently established, use **HOLD** rather than inventing certainty.
- During every FULL locale review, explicitly resolve artificial-intelligence terminology for that locale: determine whether contemporary consumer/software usage naturally prefers `AI`, a localized abbreviation such as `AA`, `MI`, `KI`, or `UI`, another abbreviation, or a full native-language term. Never inherit `AI` automatically from English, another or related locale, or a previous task prompt. Android/Google remains a preferred first-party reference, but does not automatically defeat strong contemporary native usage. When the reviewed choice is meaningfully locale-specific and frozen, protect it in the locale-specific semantic contract.
- Do not infer one locale from a related language or normalize terminology or grammatical aspect across a language family. Treat Polish/Czech/Slovak, Ukrainian/Belarusian/Russian, Arabic/Persian/Urdu, `pt-BR`/`pt-PT`, and `zh-Hans`/`zh-Hant` independently. Related locales are sanity comparisons, not linguistic authorities. A Polish improvement does not invalidate other Slavic recurring-action constructions.
- Do not apply linguistic purism. An international loanword such as AI, Error, Advanced, Default, earpiece, audio, or Settings vocabulary may be the most natural contemporary choice; conversely, use an established local Android term where one exists.
- When terminology is uncertain, apply the evidence model above; use Unicode CLDR for locale and script conventions and do not treat machine-translation sites as linguistic authority. Without web access, do not fabricate evidence; state confidence honestly and use `HOLD` when needed.
- Preserve the policy's native-script and orthography requirements. Check Persian `ی`/`ک`, Turkish dotted/dotless I, Vietnamese diacritics, Indic and Thai combining marks, Arabic-script shaping, the correct CJK variant, and mixed local script with `Private Audio` or international technical terms where relevant. Never introduce ASCII/transliteration shortcuts.
- Preserve the distinction between the **built-in call earpiece/receiver** and loudspeaker, speakerphone, headphones, earbuds, or Bluetooth audio. Research a better term if a generic “speaker” equivalent is ambiguous.
- Prefer native user concepts such as audio switching or directing/redirecting audio over network-style routing jargon, without changing product semantics.
- Treat `Private Audio` as the untranslated brand token, but treat `Mini` as the compact or minimized-controller concept rather than a second brand token. Latin `Mini` is valid where natural; natural local-script forms such as `ミニ`, `미니`, or `Мини`, and established local equivalents such as Chinese `迷你`, are equally valid. Never force Latin script for visual consistency or replace a familiar concise form with a longer, artificial, purist, or less familiar translation merely to localize it. Preserve the concept and constrained-surface suitability; script differences alone do not show inconsistency. Do not bulk-normalize existing reviewed forms.
- Apply a mandatory lexical-collision gate to every international loanword, abbreviation, or product/software term transliterated into a target script—not only `Mini`, but terms such as AI, Power, Active, Default, Settings, audio, and earpiece. Before PASS or freeze, verify the resulting written form as a lexical item in the target language rather than accepting phonetic similarity alone. Check for collisions with an unrelated common word; vulgar, sexual, medical, or body-related language; an insult or offensive, embarrassing, or humorous expression; a misleading product concept; or another strongly established meaning. A clear, materially inappropriate collision is normally **FIX**. Prefer another established transliteration, a natural native equivalent, or the original Latin term where contemporary product usage supports it; never invent a strange purist form merely to avoid a collision. Apply this universal rule especially carefully to Arabic-derived scripts and abjads (including future Punjabi Shahmukhi or Pashto reviews), Hebrew, Indic and Cyrillic transliterations, CJK phonetic loanwords, and any adaptation where omitted vowels or script conversion creates ambiguity. Use normal review for an obvious established native/product form, an explicit collision check for a non-obvious, transliterated, or freeze-worthy form, and **HOLD** when evidence remains ambiguous; heavyweight dictionary research is not required for every obvious established loanword.

## Reuse recorded audit baselines

Before a corpus-wide re-audit, inspect the latest localization audit baseline in `docs/PROGRESS.md`. Do not repeat a full linguistic audit of unchanged PASS locales without a concrete invalidation trigger. Reopen affected locales when source semantics or localized copy changes, a locale is added, relevant policy or skill guidance changes, credible new terminology evidence appears, Android locale/resource behavior or the supported API/runtime range changes, a runtime/device/user report exposes a defect, a targeted product reason requires reopening a frozen term, or an explicit fresh full-corpus audit is requested. Otherwise review only changed, new, disputed, or evidence-invalidated locales. This does not suppress key, placeholder, qualifier, semantic-regression, or other necessary checks after source changes.

## Execute the relevant workflow

### Add a locale

1. Inspect current English/default strings and derive the current locale/key inventory.
2. Pass the mandatory Android qualifier-compatibility gate before creating a resource directory:
   - establish the modern BCP-47 logical tag;
   - research whether Android/Java has a legacy alias relevant across the project's current supported runtime range;
   - inspect current `minSdk`, `targetSdk`, `generateLocaleConfig`, and generated-locale discovery behavior;
   - determine the resource qualifier from current Android matching behavior, not merely the modern ISO code;
   - keep logical locale identity distinct from resource-directory identity;
   - do not create duplicate legacy and modern locale trees without proven platform need; and
   - add durable qualifier, discovery, unsafe-folder-absence, and real resource-resolution coverage when compatibility-sensitive.

   Known current examples are modern Indonesian `id` / legacy qualifier `in`, Hebrew `he` / `iw`, and Yiddish `yi` / `ji`. These illustrate a general Android rule, not an exhaustive alias list; research this boundary before implementing every new language.
3. Review core terminology and product states—including an explicit ON/enabled-versus-ACTIVE back-check when both occur—identify and research uncertainty, then freeze approved copy.
4. Implement standard Android resources without silently rewriting approved copy. Report genuine language problems instead of improvising.
5. Verify key and placeholder parity, generated discovery, script/orthography, Mini labels, and shared RTL/bidi behavior where applicable.
6. For each locale, add durable locale-specific assertions to the existing localization contract for FULL/MINI-reviewed or otherwise frozen high-risk terminology, including meaningful reviewed Mini states. Ad-hoc Python, Java, or manual checks and parity alone are not substitutes.
7. Regress existing locales and update `docs/PROGRESS.md` with accurate evidence levels.

### Add or change a source string

1. Pass the English source-copy gate and establish semantic intent.
2. Derive the CURRENT supported locale inventory from the repository and assess impact across every supported locale.
3. Update all locales in one change when practical; otherwise use explicit controlled batches and track every `HOLD` or pending locale. English fallback is not completion.
4. Distinguish changed locales from reviewed-no-change locales and run parity checks. Avoid the false-success pattern of mechanical translation followed only by passing contracts.

### Introduce quantities

Use Android `plurals`, follow CLDR categories, and audit every locale rather than copying English assumptions. Retain the Nigerian Pidgin (`pcm`) future-plural guardrail in `docs/LOCALIZATION.md`.

## Keep linguistic and layout review separate

Do not weaken correct language merely to match English length. For Mini status copy, use the mandatory sequence **NATURAL LANGUAGE → MEASURE THE FULL FOUR-STATE PARADIGM → SHARED 16f / 15f / 14f FONT FIT → COMPACT COPY ONLY IF NATURAL COPY STILL FAILS AT 14f → ELLIPSIS AS FINAL SAFETY**. Resolve and measure Ready, Waiting, Active, and Error by actual rendered width with production assumptions, never by character count. Select one largest fitting candidate for the entire locale paradigm and retain it across state changes; recalculate on localized configuration/resource change. Never add locale-, language-, script-, word-, or length-specific runtime sizing branches or locale-specific geometry. Fourteen remains the runtime minimum and ellipsis remains the final safeguard.

Only a demonstrated measured failure of natural copy at 14f can open targeted compact-copy review. Review visual compact and full spoken accessibility labels separately; fitting must not reduce accessibility copy. Existing approved compact overrides are not automatically invalidated and require an explicit targeted linguistic review before alteration. Do not invent abbreviations solely to fit geometry.

For a compact state surface, require demonstrated measured failure after the shared component-local fitting rule and review Ready / Waiting / Active / Error together as one locale-specific status-label paradigm—never shorten one state alone. Exact parts of speech may differ, but perceived grammar, style, register, aspect, and peer-label coherence must be deliberate; no label may become an instruction, another state, or an unrelated concept, and all four semantic distinctions must remain intact. Choose natural language first and brevity second: prohibit manual truncation and geometry-driven invented abbreviations, and prefer a small shared measured layout accommodation when the natural set narrowly misses fit rather than introducing locale-specific geometry or degrading copy. Review compact visual and full spoken accessibility copy separately, and persistently contract-test every reviewed or frozen compact set.

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
