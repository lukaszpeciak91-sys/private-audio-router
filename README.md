# private-audio-router

> **Use AI voice conversations like a private phone call.**

`private-audio-router` is an experimental Android project intended to become a small, provider-independent system control surface that temporarily requests the phone's built-in earpiece for communication audio. Its primary use case is listening to a voice conversation in public without playing responses through the main loudspeaker. Compatibility with ChatGPT Voice, Gemini voice, browsers, and other applications is unverified and will be validated experimentally on physical devices.

## Status

The project is at **repository foundation / pre-implementation** status. This repository currently contains governance, research, architecture, and testing documentation only; there is no Android application yet. The first milestone is to prove safe, reversible communication-device routing with public Android APIs.

## Design and safety

The utility is intended to remain local and outside the audio data path: it should request a communication device, observe the result, and clear the routing request. It must not receive, record, decode, capture, forward, or proxy conversation audio. It requires no AI-provider login or cloud credentials and has no approved network dependency. Real telephony retains system priority, failures must leave or restore normal Android behavior, and failed tests do not justify silently escalating to invasive techniques.

## Non-goals

This project is not a ChatGPT or Gemini client, AI or voice assistant, audio recorder, audio capture/proxy tool, Accessibility Service, MediaProjection-based tool, root or Shizuku tool, replacement dialer or VoIP stack, third-party application modifier, application patcher, or tool requiring OpenAI, Google, or other cloud credentials.

## Documentation

- [Project charter](docs/PROJECT_CHARTER.md)
- [Architecture](docs/ARCHITECTURE.md)
- [Decision log](docs/DECISIONS.md)
- [Progress](docs/PROGRESS.md)
- [Research baseline](docs/RESEARCH.md)
- [Test plan](docs/TEST_PLAN.md)
- [Development workflow](docs/WORKFLOW.md)

Development sequencing and safety boundaries are defined in those documents. The recommended next PR is the minimal diagnostic shell described in the workflow, with no routing changes.
