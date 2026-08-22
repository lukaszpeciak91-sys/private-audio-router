---
name: private-audio-translation
description: Produce, self-check, and write Private Audio translations. Use when translating source strings, propagating new or changed English copy to locales, adding a locale or localized copy, fixing translations identified by an audit, maintaining localized plurals/placeholders/markup, or changing locale-specific user-facing wording. Do not use for an independent audit or verification-only request.
---

# Private Audio translation

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

1. Reconstruct the source meaning and surface context.
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

Never strengthen or weaken the factual guarantee. Preserve these distinctions:

- `does not request microphone access` is not `cannot access the microphone`;
- local processing is not collection or transmission;
- conversation/audio content is not audio-system metadata/state;
- requesting permission is not technical capability;
- generating diagnostic information is not saving/exporting it; and
- app-data backup being disabled does not mean data can never be copied.

## Perform Targeted Self-Review

When a phrase creates a specific linguistic uncertainty:

1. Name the exact ambiguity.
2. Compare plausible formulations.
3. Test each against source semantics and target-language usage.
4. Choose the best defensible candidate.
5. Write it.
6. Optionally report `REVIEW_RECOMMENDED: <locale/key/reason>` and use `TRANSLATED_WITH_REVIEW_RECOMMENDED`.

`REVIEW_RECOMMENDED` is informational and never means that no translation was produced. Never blanket-block locales because native-speaker verification is unavailable. Stop only the affected item with `SOURCE COPY REVIEW REQUIRED` when the source meaning itself is unknowable; do not invent whether an ambiguous verb such as “use” means collect, record, process, access, or transmit.

## Preserve product terminology

- Apply terminology in this order:
  1. exact Private Audio product semantics;
  2. established Android or first-party platform terminology when semantically correct for that locale and surface;
  3. established terminology already used consistently in that Private Audio locale; and
  4. natural contemporary native-language software/UI wording.
- Treat platform terminology as evidence, not authority. Never adopt a platform term that collapses a protected distinction such as earpiece versus loudspeaker/speakerphone, headphones/headset, or Bluetooth, and do not replace a more precise existing Private Audio term merely to match an imperfect platform translation.
- Keep **ON/enabled** (waiting or available) distinct from runtime **ACTIVE**.
- Distinguish the built-in call earpiece/receiver from loudspeaker, speakerphone, headphones, earbuds, and Bluetooth.
- Preserve source versus destination, action versus state, trigger versus effect, and screen versus phone/device.
- Resolve AI terminology independently for each locale. Do not infer terminology from a related language.
- Keep `Private Audio` untranslated. Treat `Mini` as a compact-controller concept, not necessarily a Latin-script brand token.
- Check transliterated/international terms for misleading, offensive, embarrassing, or concept-colliding target-language meanings.
- Use the normal native script and orthography, including locale-specific Unicode distinctions and the correct CJK variant.
- Research non-obvious terms when tools are available without fabricating evidence when they are not.

### Classify short international-looking UI terms

Do not automatically freeze concise terms such as `Mini`, `Compact`, `Pro`, `Default`, or `Advanced` as untranslated technical tokens. First distinguish a **brand token** from a **localizable UI concept**: `Private Audio` is currently a brand token, while `Mini` is currently a localizable compact-controller concept. Script is not a reason to keep English, and ASCII/Latin wording inside otherwise non-Latin UI is a review signal rather than a default strategy.

For each concept, establish its exact Private Audio meaning and surface, then research first-party Android/Google/Microsoft or equivalent terminology where available and contemporary target-language software/product usage. Determine whether users normally expect an established native UI term, an attested target-script transliteration or loanword, a concise semantic equivalent, or the Latin original. Prefer established target-language product terminology, then an established target-script loanword, then a concise established semantic equivalent, and use the Latin original only when it is genuinely normal target-language UI usage; this order is evidence-guided, not rigid. Do not invent a phonetic spelling mechanically. Check every candidate for unrelated, offensive, embarrassing, misleading, technical, or grammatical lexical collisions. Khmer `ខ្នាតតូច` illustrates a natural semantic equivalent that is preferable to an invented transliteration.

Surface constraints affect the decision: a first-party phrase for a full component such as `Miniplayer` may be unsuitable for a standalone one-word button. Adapt evidence to the actual Private Audio surface instead of copying a long component name. Script consistency is evidence, not a hard cross-locale uniformity rule; never replace an established native form merely to make locales look alike. Visual fit does not authorize inferior language: follow **NATURAL LANGUAGE → MEASURE → 16f / 15f / 14f → compact linguistic review → ellipsis safety**.

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

Natural language comes before geometry. For Mini, keep Ready / Waiting / Active / Error as one semantic paradigm, then measure the full set using the existing shared 16f / 15f / 14f fitting path. Character count is not layout measurement. Only demonstrated failure at 14f opens compact-copy review; never silently alter semantics or full accessibility wording to fit. Preserve existing component geometry and approved overrides unless explicitly reopened.

## Complete implementation

Inspect and follow the repository's current localization contracts rather than assuming fixed filenames. As applicable, update locale resources, meaningful semantic regression assertions, Android resource-resolution/direction coverage, Mini measurement coverage, and `docs/PROGRESS.md`. Run current XML, duplicate-key, key/placeholder parity, discovery, contract, RTL, conflict-marker, and whitespace checks. Report static, linguistic self-check, runtime, emulator, physical-device, independent-audit, and human/native evidence separately.
