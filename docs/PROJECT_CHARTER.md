# Project Charter

## Mission

Create a small Android utility that allows a user to request the built-in earpiece as the output device for Android communication audio.

## Primary user problem

Voice AI can be useful while walking, commuting, waiting at a bus stop, or being around other people, but loudspeaker output is socially awkward and exposes responses to nearby people. Listening through the built-in earpiece should make the interaction feel more like a normal phone call.

## Product principles

- Private by design and local.
- Provider-independent and minimal.
- Reversible, with safe failure behavior.
- No conversation processing and no cloud account required.
- No invasive Android privileges.
- Real telephony takes priority.

## Scope

Initial scope is Android only. The initial technical goal is not to support every sound on the phone; it is specifically to test and control Android communication-audio routing using the least invasive public mechanisms available.

## Non-goals

The project is **not**:

- a ChatGPT client, Gemini client, AI assistant, or voice assistant;
- an audio recorder or audio capture/proxy tool;
- an Accessibility Service or MediaProjection-based tool;
- a root or Shizuku tool;
- a replacement dialer or replacement VoIP stack;
- a third-party application modifier or a tool for patching ChatGPT or other applications; or
- a tool requiring OpenAI, Google, or other cloud credentials.

There is no approved network requirement. Adding one requires a future explicit architectural decision.

## Success criteria

### M0 — Prove the route

Success means a standalone Android application can, using public Android APIs only, request the built-in earpiece as the communication-audio output device, observe the routing state, clear its routing request, and leave normal Android audio behavior intact.

Compatibility with ChatGPT Voice is a physical-device validation target, not an assumed fact.

### M1 — Make it usable

Only after M0, possible work includes a persistent controller, simple toggle, foreground service if required, and floating control if justified. These are candidates, not commitments.

### M2 — Make it universal

Only after earlier milestones, validate ChatGPT Voice, Gemini voice, browser-based realtime voice, and other communication-audio applications. Compatibility is not promised.
