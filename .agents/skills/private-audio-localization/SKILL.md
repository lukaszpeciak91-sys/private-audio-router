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
   - For privacy, security, permissions, account, diagnostics, backup, data-handling, or similar factual copy, is each claim sufficiently bounded and supportable rather than an ambiguous absolute or unsupported technical incapability?
3. For behavioral or Settings copy, explicitly decompose subject/object, action/effect, trigger/condition, state versus action, hardware component, and user-perceived behavior where relevant. In particular, do not blur the screen, phone, proximity trigger, or screen power-off behavior.
4. If source copy is materially weak, ambiguous, or factually overbroad, stop propagation and report **`SOURCE COPY REVIEW REQUIRED`** with the problem. For example, prefer `does not request microphone access` to unsupported `cannot access the microphone`, and prefer `does not collect, record, or transmit your conversations or audio content` to `does not process any data` when technical metadata is processed locally. Correct a small obvious defect only when the task explicitly authorizes changing English copy. Technical validity alone is not localization readiness.

## Assess language quality

For each locale, prefer in order: semantic correctness, native-speaker naturalness, contemporary Android/software terminology, compact UI suitability, and locale-internal consistency. Ask whether the wording could plausibly appear in a professionally localized Android app—not merely whether a native speaker could understand it. Avoid literal translations and calques.

Classify audit results without manufacturing edits:

- **PASS:** natural professional product language; do not change it.
- **POLISH:** correct and understandable, with a meaningfully better native product wording available.
- **FIX:** misleading, semantically wrong, substantially unnatural, or clearly poor product copy.
- **HOLD:** insufficient evidence to choose responsibly among plausible alternatives.

For high-risk factual claims, **FIX** includes a stronger claim than the source; a weaker, materially different guarantee; a changed actor, data category, or destination; lost negation or user-action condition; collapsed content and metadata; implied technical incapability where the source states only behavior or permission state; or contradiction within the translated claim set. Use **HOLD** when available linguistic evidence cannot confidently preserve exact factual scope. Do not mark natural stylistic or cross-sentence restructuring as **FIX** when the complete factual meaning remains equivalent.

For every proposed language change, record **assessment confidence** and **replacement confidence** as `HIGH`, `MEDIUM`, or `LOW`. A confident diagnosis need not imply a confident replacement. Never silently implement a `LOW`-confidence replacement. Record reviewed-no-change locales as meaningful results.

## Back-check semantic relationships

Before finalizing a locale, perform a source-to-target semantic back-check for strings whose meaning depends on an important relationship or distinction. Scale the check to semantic risk, with particular attention to system versus device, app versus system, screen versus phone/device, enabled versus active, state versus action, source versus destination, built-in earpiece versus loudspeaker/headphones, trigger versus effect, subject/object relationships, and accessibility-control semantics.

Ask whether the target preserves the same subject, object, referent, and relationship between components; whether it accidentally collapses two distinct source concepts; and whether grammatically natural wording implies different behavior. A target that changes or collapses those semantics is **`FIX`**. Grammatical naturalness never overrides semantic fidelity.

Treat **ON / enabled versus ACTIVE** as a mandatory product-state check whenever the source contains both concepts. Before translating, reconstruct them independently: Power ON or an enabled controller/service means Private Audio is waiting or available, while Active means it is currently participating in a qualifying communication-audio session. Verify that the target does not collapse those meanings; semantic identity is **`FIX`**, although grammatical similarity is acceptable. Power ON, service/controller enabled, waiting/ready, and runtime Active are not interchangeable states. Preserve both concepts independently in reviewed/frozen copy, and add persistent locale-specific contract assertions for both when the distinction is a meaningful regression target.

For a linguistically non-obvious or high-risk string, briefly reconstruct the target meaning in plain English or equivalent source-language semantics. This reverse gloss is a sanity check—not machine back-translation offered as proof of quality—for swapped subjects and objects, lost distinctions, wrong referents, missing triggers, and altered action/state semantics. If the reconstructed meaning differs materially from the source, stop and review. Apply this proportionally; trivial labels such as `Close`, `Error`, and `Settings` do not each require a reverse gloss.

## Translate high-risk factual claims conservatively

Treat privacy, security, permissions, data-handling, account, backup, diagnostics, and similar factual product statements as one high-risk semantic-copy class. Before translating each statement, identify as applicable:

- **actor:** Private Audio, Android, the user, a server, or another service;
- **action:** collect, access, record, store, process, transmit, request permission, save, generate, or back up;
- **object/data category:** conversation or audio content, microphone input, metadata, technical state, settings, or diagnostic data;
- **location/destination:** locally on device, Android, a backup system, a server, or elsewhere;
- **trigger/condition:** automatically, as needed, only after user action, or never; and
- **negation and qualifiers:** only, may, does not, does not request, does not contain, and other limitations.

Preserve the exact claim scope and strength. Never strengthen the source, weaken an explicit guarantee without genuine linguistic necessity, or add a privacy promise, legal guarantee, disclaimer, permission, data category, technical capability, or restriction absent from the frozen source. In particular:

- `does not request microphone access` must not become `cannot access the microphone`;
- `does not send data to a server` must not become `no data ever leaves the device`;
- `does not contain conversation or audio content` must not become `contains no audio-related data`;
- `Android app-data backup is disabled` must not become `the data can never be backed up or copied`; and
- a report saved only when the user chooses to save it must retain that user-initiated condition.

Keep conversation/audio content distinct from audio-system metadata/state; recording microphone input from observing audio-system state; local processing from collection or transmission; requesting permission from technical capability; and saving/exporting a report from generating diagnostic data.

When multiple statements jointly describe actual product behavior, review them as a semantic **CLAIM SET**, not as independent sentences. The translated set must be internally consistent and must not create a broader promise than the frozen source. If the target language restructures meaning across sentences, evaluate the complete set.

For every high-risk factual claim, a reverse semantic gloss is mandatory before **PASS**. Confirm the actor, action, object/data category, destination/location, condition/trigger, negation, qualifier, and claim strength wherever applicable. When linguistic evidence cannot confidently reconcile natural wording with exact factual scope, use **HOLD** rather than guessing. This additional decomposition is proportional: ordinary labels such as `Close`, `Settings`, `Error`, and `Mini` do not require it unless they materially participate in a factual claim.

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

#### Short-task input contract

A short task such as “Implement locale X according to the current Private Audio localization skill” invokes this **complete** ADD LOCALE workflow when it supplies the locale-specific frozen data:

1. logical locale identity;
2. expected Android qualifier/resource directory, if already reviewed;
3. script and direction;
4. intended language, region, and script tags that must resolve the locale;
5. the final frozen translated resource set or exact key/value mapping;
6. locale-specific terminology decisions and known exceptions; and
7. remaining **HOLD** / `MEDIUM`-confidence items or explicit implementation warnings.

Derive everything else from the CURRENT repository, `docs/LOCALIZATION.md`, and this skill. Do not require implementation prompts to repeat stable rules from either authority. Once linguistic review is frozen, prefer the short locale-specific prompt. Policy belongs in `docs/LOCALIZATION.md`, execution belongs here, and locale facts belong in the task, final resources, durable tests, and `docs/PROGRESS.md`; do not create per-language workflow documents.

#### Default execution contract

Unless the CURRENT architecture has changed, automatically:

1. Inspect the CURRENT English/default source, keys, locale inventory, contracts, and relevant surfaces; stop with **`SOURCE COPY REVIEW REQUIRED`** if the source-copy gate fails.
2. Pass the Android qualifier gate before creating the correct resource tree: establish the logical BCP-47 identity; inspect the supported runtime range, `minSdk`, `targetSdk`, generated LocaleConfig behavior, and legacy aliases; derive the qualifier from actual Android matching; and avoid unsafe parallel legacy/modern trees. Current `id`/`in`, `he`/`iw`, and `yi`/`ji` mappings are examples, not an eternal list.
3. Preserve the task-supplied frozen copy exactly and enforce exact key and placeholder parity, native script/orthography, and meaningful accessibility/control-description parallelism. Do **not** independently retranslate, shorten, normalize, “improve,” harmonize with a related language, or replace a reviewed loanword or hardware term. If implementation exposes a contextual grammar or platform problem, report its exact **PASS / POLISH / FIX / HOLD** status; never silently rewrite the copy.
4. Extend the CURRENT durable semantic-contract architecture with meaningful regression targets rather than mechanically asserting every translated word. For a normal FULL-reviewed locale, protect as applicable: ON/enabled versus ACTIVE; the full Ready / Waiting / Active / Error paradigm; reviewed AI and Mini choices (including lexical-collision decisions); specifically reviewed Default and Advanced terms; built-in call-earpiece distinction; final user-facing routing wording; script/orthography-sensitive terms; risky accessibility grammar; qualifier/resource identity; and absence of unsafe duplicate qualifiers. Exact literals must come from the FINAL resource text.
5. Cover generated LocaleConfig discovery and exercise every task-supplied intended tag through **actual Android resource resolution** when the CURRENT instrumentation architecture supports it, asserting localized resource retrieval and correct LTR/RTL layout direction. Folder presence, `Locale.forLanguageTag()` canonicalization, and LocaleConfig alone do not prove resource resolution.
6. Apply the standard Mini contract automatically: resolve all four natural state aliases through Android resources; run the existing production-equivalent shared selector; and assert one common candidate selected from 16f / 15f / 14f. Do not hard-code the selected size unless instrumentation actually measured it, and do not add a locale-specific compact override because a label merely looks long. Only measured failure at 14f opens compact-copy review.
7. Regress the CURRENT localization contracts, including scope and accessibility sanity checks; update `docs/PROGRESS.md` with precise evidence levels; run `git diff --check`, scan conflict markers, and inspect `git status --short`. Distinguish static/source-contract, JVM, instrumentation, runtime/emulator, and physical-device evidence accurately.

Use the CURRENT established test architecture rather than treating filenames as permanent. Today, examples include the Layer 4.1 semantic contract, Android resource-resolution instrumentation, and production-equivalent Mini measurement coverage. The typical current change shape is one locale strings resource, locale-specific durable contract coverage, Android resource-resolution coverage, Mini measurement coverage, and the progress update. This is guidance, not an immutable five-file prescription; follow the CURRENT architecture if it evolves.

#### ADD LOCALE final report

Report the files changed; logical locale and resource qualifier; intended tags and actual resource-resolution coverage; confirmation that final frozen copy was preserved; semantic contracts added; LocaleConfig and direction coverage; Mini selected size **only if actually measured**; checks executed and checks not executed with reasons; all **PASS / POLISH / FIX / HOLD** findings; and confirmation that no unrelated production behavior changed. A future task need not restate this reporting contract.

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
