---
name: private-audio-translation
description: Produce, self-check, and write Puzru translations. Use when translating source strings, propagating new or changed English copy to locales, adding a locale or localized copy, fixing translations identified by an audit, maintaining localized plurals/placeholders/markup, or changing locale-specific user-facing wording. Do not use for an independent audit or verification-only request.
---

# Puzru translation

Produce the best defensible translation candidate for the requested locale and write it. Read `docs/LOCALIZATION.md`, the current default English resources, the current locale inventory, relevant target resources, and current terminology contracts before editing. Treat default English as authoritative; do not silently change its meaning.

## Keep the role bounded

- Modify only localized resources explicitly in scope and their required contracts/documentation.
- Self-check and repair the candidate, but never call that check independent validation.
- Never claim human, native-speaker, or independent validation without recorded evidence.
- Do not refuse a target merely because a native reviewer, external source, web access, or other ideal evidence is unavailable.
- Do not use audit verdicts `PASS`, `POLISH`, `FIX`, or `HOLD` as translation outcomes.
- Route verification-only or quality-classification requests to `private-audio-localization-audit`.

Report one of:

- **`TRANSLATED`**: a candidate was produced, self-checked, and written. This is not independent or human validation.
- **`TRANSLATED_WITH_REVIEW_RECOMMENDED`**: the same completed result, with a specific uncertainty worth later review.
- **`SOURCE COPY REVIEW REQUIRED`**: one identified source item is materially ambiguous, contradictory, factually unsafe, or insufficiently defined, so its intended meaning cannot responsibly be determined.

## Translate and self-check

For ordinary copy:

1. Reconstruct the source meaning, technical referent, and surface context.
2. Generate natural contemporary product language using locale-appropriate terminology.
3. Check naturalness, grammar, script, orthography, and consistency with neighboring target strings.
4. Reconstruct the candidate's meaning independently of its wording.
5. Compare that reconstruction with the source, repair any drift, and write the resulting candidate.

For privacy, permissions, security, diagnostics, backup, account, data-handling, or similarly high-risk factual copy:

1. Model the source claim, as applicable, by **actor, action, object/data category, destination/location, condition/trigger, negation, qualifiers, claim scope, and claim strength**.
2. Generate the candidate.
3. Produce a reverse semantic self-check of the target.
4. Compare the reconstructed target model against the source model and the complete **CLAIM SET** when related statements depend on one another.
5. Correct detected drift, then write the best resulting candidate.

Never strengthen or weaken the factual guarantee. Preserve the actual action expressed by high-risk verbs such as collect, record, transmit/send, access, observe, process, generate, save, request, use, and disable. An approximately related outcome is not equivalent. Preserve these distinctions:

- `does not request microphone access` is not `cannot access the microphone`;
- local processing is not collection or transmission;
- conversation/audio content is not audio-system metadata/state;
- requesting permission is not technical capability;
- generating diagnostic information is not saving/exporting it; and
- app-data backup being disabled does not mean data can never be copied.

### Resolve technical sense before choosing words

Do not translate an isolated English token when its everyday and software/product senses can differ. Before selecting target wording for a non-obvious or polysemous term, state a short conceptual reconstruction:

```text
SOURCE TERM: Version
SOURCE SENSE: software/application release version, not type, kind, or category
SURFACE: Settings version label
```

Apply this proportionally to terms such as version, session, access, crash, route/routing, output, active, available, permission, metadata, backup, server, analytics, diagnostics, report, account, sign-in, built-in, receiver/earpiece, speaker, and proximity sensor. This is a trigger list, not a fixed glossary. Product semantics come first: a generic ICT translation of `Active`, for example, must not replace Puzru's state meaning—current participation in an established qualifying communication-audio routing cycle—with merely enabled/ON.

For every materially uncertain term, maintain a lightweight **Targeted Terminology Ledger** in working analysis rather than a new repository file:

**source concept → source sense/referent → candidate → evidence → reverse gloss → rejected alternative(s), when relevant → decision → confidence/review need**

The entries must contain the actual reasoning, not merely “self-checked.” Report only meaningful decisions and unresolved uncertainty rather than dumping the whole ledger. A dictionary-correct equivalent is insufficient when an established software or Android sense applies; choose wording that denotes the exact product concept. Conversely, do not blindly adopt glossary wording that is obsolete, unnatural, or semantically wrong for the Puzru surface.

When research tools are available, actively seek terminology evidence for a term that is technical or software-specific; concerns privacy, security, or data; is semantically overloaded or unfamiliar; looks like a literal calque; becomes uncertain under reverse gloss; or is likely to differ from ordinary dictionary meaning. Use this evidence hierarchy contextually:

1. exact Puzru semantics;
2. first-party Android, Google, or platform terminology for the target locale when semantically correct;
3. official or institutional target-language technology terminology;
4. established software localization or mature localized software documentation;
5. reputable technical glossaries or dictionaries;
6. contemporary professional target-language technology usage; and
7. general dictionaries as supporting evidence, never automatic authority.

The gate is proportional: ordinary, clearly established labels such as `Close` or `Error` normally need no terminology dossier, while `audio session`, `metadata`, `crash reporting`, or Privacy `access` often do. State evidence limitations honestly; never fabricate first-party usage, Android terminology, native-speaker validation, or consensus. A related or neighboring language may reveal questions to investigate, but its cognate, loanword, or vocabulary is not target-language evidence and must not be copied mechanically.

Reverse gloss must test whether the target denotes the same technical **object, action, state, or relationship**, not merely whether the sentence sounds broadly equivalent. Reject shifts such as software version → type/kind, audio session → section/part, access → receive/get, software crash → physical breakage, earpiece → generic speaker, audio routing → networking route, ACTIVE → enabled/ON, report generation → report saving, or permission request → technical capability. For high-risk copy, add this technical-referent check to—never replace—the actor/action/object/destination/location/condition/negation/qualifier/scope/strength model above.

## Perform Targeted Self-Review

Targeted Self-Review triggers automatically when a reverse gloss changes the noun class or referent; multiple technical equivalents are plausible; only a generic dictionary candidate exists; a candidate looks like a literal calque; first-party and glossary terminology disagree; an international loanword competes with a native technical term; a related-language form is tempting to copy; a choice changes state or action semantics; a privacy/security verb can drift in scope; or an exact test would freeze the candidate.

When triggered:

1. Name the exact ambiguity.
2. Compare plausible formulations.
3. Test each against source semantics and target-language usage.
4. Choose the best defensible candidate.
5. Write it.
6. Classify terminology confidence and report a narrow review need where applicable:
   - **HIGH-CONFIDENCE / FROZEN** — strong evidence and the exact referent are established; an exact lexical regression assertion may be used when valuable.
   - **DEFENSIBLE BUT REVIEW-WORTHY** — the best available written candidate preserves source semantics, but evidence is incomplete or usage genuinely varies; report `REVIEW_RECOMMENDED: <locale/key/reason>` and use `TRANSLATED_WITH_REVIEW_RECOMMENDED`.
   - **UNSUPPORTED / SEMANTICALLY UNRESOLVED** — continue targeted research and self-review because the candidate's technical sense is not established; do not freeze it in an exact lexical assertion merely to finish.

`REVIEW_RECOMMENDED` is informational and never means that no translation was produced. Weak evidence does not by itself prevent writing the best defensible candidate, and missing native-speaker verification must not blanket-block a locale. Stop only the affected item with `SOURCE COPY REVIEW REQUIRED` when the source meaning itself is unknowable; do not invent whether an ambiguous verb such as “use” means collect, record, process, access, or transmit. These confidence labels are Translator working outcomes, not Audit verdicts or independent acceptance.

## Preserve product terminology

- Apply terminology in this order:
  1. exact Puzru product semantics;
  2. established Android or first-party platform terminology when semantically correct for that locale and surface;
  3. established terminology already used consistently in that Puzru locale; and
  4. natural contemporary native-language software/UI wording.
- Treat platform terminology as evidence, not authority. Never adopt a platform term that collapses a protected distinction such as earpiece versus loudspeaker/speakerphone, headphones/headset, or Bluetooth, and do not replace a more precise existing Puzru term merely to match an imperfect platform translation.
- Keep **ON/enabled** (waiting or available) distinct from runtime **ACTIVE**.
- Distinguish the built-in call earpiece/receiver from loudspeaker, speakerphone, headphones, earbuds, and Bluetooth.
- Preserve source versus destination, action versus state, trigger versus effect, and screen versus phone/device.
- Resolve terminology independently for each locale, including AI terminology. Related Oromo/Somali/Amharic, Slavic, Indic, Malay/Indonesian, African regional, Romance, or other language-group copy is not lexical evidence for the target locale.
- Keep `Puzru` untranslated. Treat `Mini` as a compact-controller concept, not necessarily a Latin-script brand token.
- Check transliterated/international terms for misleading, offensive, embarrassing, or concept-colliding target-language meanings.
- Use the normal native script and orthography, including locale-specific Unicode distinctions and the correct CJK variant.
- Apply the technical-sense and terminology-evidence gate above rather than relying on plausibility or a general dictionary gloss.

### Classify short international-looking UI terms

Do not automatically freeze concise terms such as `Mini`, `Compact`, `Pro`, `Default`, or `Advanced` as untranslated technical tokens. First distinguish a **brand token** from a **localizable UI concept**: `Puzru` is currently a brand token, while `Mini` is currently a localizable compact-controller concept. Script is not a reason to keep English, and ASCII/Latin wording inside otherwise non-Latin UI is a review signal rather than a default strategy.

For each concept, establish its exact Puzru meaning and surface, then research first-party Android/Google/Microsoft or equivalent terminology where available and contemporary target-language software/product usage. Determine whether users normally expect an established native UI term, an attested target-script transliteration or loanword, a concise semantic equivalent, or the Latin original. Prefer established target-language product terminology, then an established target-script loanword, then a concise established semantic equivalent, and use the Latin original only when it is genuinely normal target-language UI usage; this order is evidence-guided, not rigid. Do not invent a phonetic spelling mechanically. Check every candidate for unrelated, offensive, embarrassing, misleading, technical, or grammatical lexical collisions. Khmer `ខ្នាតតូច` illustrates a natural semantic equivalent that is preferable to an invented transliteration.

Surface constraints affect the decision: a first-party phrase for a full component such as `Miniplayer` may be unsuitable for a standalone one-word button. Adapt evidence to the actual Puzru surface instead of copying a long component name. Script consistency is evidence, not a hard cross-locale uniformity rule; never replace an established native form merely to make locales look alike. Visual fit does not authorize inferior language: follow **NATURAL LANGUAGE → MEASURE → 16f / 15f / 14f → compact linguistic review → ellipsis safety**.

## Check contextual grammar before propagation

Before translating or reusing one value/status resource across UI objects or call sites, inspect every semantic context where it renders. Do not assume a source-language invariant such as Available, Not available, Unknown, Enabled, Disabled, Connected, Selected, or Ready has one safe target-language form. Check agreement for grammatical gender, noun class, number, case, animacy, other relevant morphology, and materially different natural formulations required by different UI objects.

If one shared resource would force ungrammatical or misleading wording, prefer a natural context-neutral state when it accurately expresses the product state (for example, a loading/progress phrase); otherwise use context-specific localized resources. Do not hide a source/resource-contract defect behind an awkward “universal” translation. Complete this check before broad locale propagation.

## Preserve Android resources and surfaces

- Preserve XML, escaping, markup, keys, numbered placeholder types, and locale-correct plurals; enforce key and placeholder parity.
- Establish the logical BCP-47 identity and compatible Android resource qualifier before adding a locale. Retain current legacy-alias rules and avoid duplicate modern/legacy resource trees.
- Preserve shared direction-driven RTL behavior; do not add locale-specific runtime logic or manual bidi marks without demonstrated need.
- Before changing a compact label, inspect its production call sites to determine whether the same resource is also used for TalkBack or another content description. Verify both the visible and spoken contexts; do not silently lengthen visible `Mini` copy for accessibility. If one resource cannot naturally serve both, report the resource/call-site issue separately rather than redesigning accessibility architecture inside a translation task.
- Review accessibility text as natural spoken language and separately from compact visual labels.
- Keep diagnostic-core output English-only; only its user-facing actions/messages are localizable.

### Semantic text structure and whitespace contract

Paragraph boundaries can be part of the source's semantic structure. Treat intentional line breaks and list boundaries as localizable content structure, not disposable formatting, and inspect source escapes such as `\n` and `\n\n` before translating. When `\n\n` separates semantic source paragraphs, the target should normally preserve equivalent functional paragraph boundaries unless a reviewed locale-specific exception exists.

Pretty XML formatting is not a substitute for Android runtime escape sequences: Android tooling may normalize raw physical newlines inside a resource value. Use explicit Android resource escapes when a runtime line or paragraph break is intended, and verify both the raw resource representation and, where available, the Android-resolved string.

Natural language comes before geometry. For Mini, keep Ready / Waiting / Active / Error as one semantic paradigm, then measure the full set using the existing shared 16f / 15f / 14f fitting path. Character count is not layout measurement. Only demonstrated failure at 14f opens compact-copy review; never silently alter semantics or full accessibility wording to fit. Preserve existing component geometry and approved overrides unless explicitly reopened.

## Complete implementation

Inspect and follow the repository's current localization contracts rather than assuming fixed filenames. Tests protect previously justified decisions; they do not justify a Translator's candidate. A passing localization contract is source-contract evidence, not linguistic acceptance evidence.

- Prefer semantic invariants and protected distinctions over exact full-string assertions.
- Use exact lexical assertions only for deliberately reviewed/frozen terminology with adequate evidence, or to protect exact wording required by a known regression.
- Never invent a candidate, assert that same candidate, and cite the passing test as evidence for its correctness.
- Do not freeze a materially uncertain candidate merely because implementation needs a test. Where possible, test the protected distinction and report `REVIEW_RECOMMENDED` for the wording.

For example, a contract may correctly preserve Oromo ON versus ACTIVE and earpiece versus loudspeaker; an assertion freezing an unsupported word for software `Version` would only preserve the defect. As applicable, update locale resources, meaningful semantic regression assertions, Android resource-resolution/direction coverage, Mini measurement coverage, and `docs/PROGRESS.md`. Run current XML, duplicate-key, key/placeholder parity, discovery, contract, RTL, conflict-marker, and whitespace checks. Report static, linguistic self-check, runtime, emulator, physical-device, independent-audit, and human/native evidence separately.
