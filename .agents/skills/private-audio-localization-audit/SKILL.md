---
name: private-audio-localization-audit
description: Independently audit existing Private Audio translations without editing them. Use for locale audits, translation verification, semantic-fidelity reviews, Privacy Policy or other high-risk copy review, terminology checks, English-fallback detection, and PASS/POLISH/FIX/HOLD quality classification. Do not use to produce or write translations.
---

# Private Audio localization audit

Independently determine whether existing target copy is acceptable relative to authoritative default English. Read `docs/LOCALIZATION.md`, current source and target resources, relevant terminology contracts, and the complete in-scope claim set. Treat provenance and any Translator self-check as irrelevant to acceptance.

## Remain independent and read-only

- Be read-only by default. Do not silently modify target resources or production files.
- Report proposed replacement wording when useful, but do not repair a defect and certify that same repair.
- Send a separately authorized fix task to `private-audio-translation`, then re-audit the written result independently.
- Do not treat key/placeholder parity, XML validity, tests, prior approval labels, or Translation Skill output as proof of linguistic quality.
- Never claim native-speaker or human validation unless that evidence actually exists.
- Reuse a recorded unchanged audit baseline unless a source/candidate/policy change, credible evidence, defect report, or explicit fresh audit invalidates it.

## Reconstruct meaning independently

Inspect source meaning first, then target meaning without assuming equivalence. For meaningful or high-risk copy record, as applicable:

**actor → action → object/data category → destination/location → condition/trigger → negation → qualifiers → claim scope → claim strength → reverse gloss → verdict**

Review related factual statements as a complete **CLAIM SET**. Before `PASS` on high-risk privacy, permissions, security, diagnostics, backup, account, or data-handling copy, require a reverse semantic gloss and explicitly confirm all applicable elements. Grammatical, natural wording that changes a factual guarantee cannot pass.

Detect at least:

- English fallback masquerading as localization or key parity without real translation;
- lost or altered negation, condition, qualifier, scope, or claim strength;
- changed actor, action/state, object/data category, destination, or technical referent;
- stronger claims or weaker guarantees than English;
- collapsed product concepts; and
- natural-sounding wording that expresses the wrong meaning.

Preserve these high-risk distinctions: requesting microphone access versus technical capability; local processing versus collection/transmission; conversation/audio content versus audio-system metadata/state; generating diagnostics versus saving/exporting; and backup disabled versus impossible copying.

## Apply product and locale gates

- Apply terminology evidence in this order: exact Private Audio product semantics; established Android or first-party platform terminology when semantically correct for the locale and surface; established terminology already used consistently in that Private Audio locale; then natural contemporary native-language software/UI wording.
- Treat first-party localized Android/Google wording as strong evidence for non-obvious Android-facing terms, not automatic `PASS` proof. Verify the exact physical/product referent, compare existing locale terminology, and reject platform wording that collapses a protected distinction or is clearly unnatural or outdated for the surface. Do not replace a more precise Private Audio term merely to match an imperfect platform translation.
- Keep ON/enabled distinct from runtime ACTIVE.
- Distinguish built-in call earpiece/receiver from loudspeaker, speakerphone, headphones, earbuds, and Bluetooth.
- Preserve source/destination, action/state, trigger/effect, and screen/phone/device referents.
- Evaluate AI terminology independently for each locale and inspect native script, orthography, Unicode, and CJK variant requirements.
- Check transliterated and international terms for lexical collisions, including offensive, embarrassing, humorous, or misleading meanings.
- Treat `Private Audio` as the brand token and `Mini` as the compact-controller concept.
- Audit accessibility copy separately as spoken language.

Natural language comes before geometry. Audit Mini Ready / Waiting / Active / Error as one semantic and stylistic paradigm. Keep linguistic correctness separate from the existing measured 16f / 15f / 14f fit; character count proves nothing. Compact copy may not silently alter semantics, and full accessibility text requires separate review.

Retain Android compatibility checks where relevant: BCP-47 identity, resource qualifiers and legacy aliases, XML/placeholder/plural integrity, direction-driven RTL, glyph/shaping risks, generated locale discovery, and real resource resolution. These checks support an audit but never substitute for it.

## Audit contextual resource grammar

For localized status/value resources, inspect production call sites rather than the string alone. When one translation is reused beside multiple nouns or objects, determine whether its grammar and meaning remain valid in every rendered context, including agreement or formulation changes required by the target language. An individually correct translation may still expose locale-unsafe resource architecture.

Classify **FIX** when the shared resource necessarily produces ungrammatical or semantically wrong UI in a supported locale, and **POLISH** when every context remains correct but a contextual formulation would materially improve native product language. Report whether the defect is in translation wording, source copy, or localization/resource architecture. When architecture is defective, do not ask the translator to find one “universal” wording.

## Return exactly one conceptual verdict per finding

- **PASS** — semantically faithful and professionally acceptable. This does not imply native/human validation.
- **POLISH** — meaning is preserved and usable, but a materially better natural or product formulation exists; normally non-semantic and non-blocking.
- **FIX** — a concrete semantic, terminology, fallback, or material naturalness defect exists. Explain the defect precisely and give evidence/confidence; proposed wording is optional.
- **HOLD** — available evidence cannot responsibly establish acceptance. Identify the concrete unresolved linguistic or semantic uncertainty.

Lack of a human/native reviewer alone does not justify `HOLD`. Do not manufacture blanket HOLD results. Report assessment confidence and, if proposing wording, replacement confidence separately as `HIGH`, `MEDIUM`, or `LOW`.

## Report evidence

For each scope reviewed, identify source and target, reverse gloss where required, verdict, precise reason, confidence, and evidence limitations. Separate linguistic audit evidence from source-contract, runtime/emulator/device, and human/native validation. Record reviewed-no-change results without manufacturing edits. If corrections are requested afterward, hand the findings to the Translation Skill and require re-audit rather than self-certifying the repair.
