# private-audio-router

> **Use AI voice conversations like a private phone call.**

`private-audio-router` (Puzru) is an Android 12+ experimental, private-call-style audio routing controller. It uses only public Android communication-routing APIs and remains outside other applications' audio-data paths.

## Status

A permanent controller with repeated isolated routing cycles is implemented, and physical earpiece routing has been demonstrated on the project's test device. Main, Settings, and a floating controller are present. See [`docs/PROGRESS.md`](docs/PROGRESS.md) for the authoritative current state, remaining validation, and next work.

## Design and safety

The utility is intended to remain local and outside the audio data path: it should request a communication device, observe the result, and clear the routing request. It must not receive, record, decode, capture, forward, or proxy conversation audio. It requires no AI-provider login or cloud credentials and has no approved network dependency. Real telephony retains system priority, failures must leave or restore normal Android behavior, and failed tests do not justify silently escalating to invasive techniques.

## Non-goals

This project is not a ChatGPT or Gemini client, AI or voice assistant, audio recorder, audio capture/proxy tool, Accessibility Service, MediaProjection-based tool, root or Shizuku tool, replacement dialer or VoIP stack, third-party application modifier, application patcher, or tool requiring OpenAI, Google, or other cloud credentials.

## Documentation

- [Project context](docs/PROJECT_CONTEXT.md) — rapid orientation for a fresh AI-assisted development session
- [Project charter](docs/PROJECT_CHARTER.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Localization policy](docs/LOCALIZATION.md)
- [Decision log](docs/DECISIONS.md)
- [Progress](docs/PROGRESS.md)
- [Research baseline](docs/RESEARCH.md)
- [Test plan](docs/TEST_PLAN.md)
- [Development workflow](docs/WORKFLOW.md)

Development sequencing, current evidence, and safety boundaries are defined in those documents; detailed current status is maintained in `docs/PROGRESS.md`.
