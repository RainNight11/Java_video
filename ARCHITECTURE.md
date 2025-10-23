# AI Virtual Presenter Platform

This repository hosts a multi-module project that ships an Android client,
a Spring Boot backend, and a Vue-based creator dashboard. The system produces
AI-assisted virtual presenter videos by combining speech cloning, story-driven
scene generation, and compositing pipelines.

## High-Level Flow

1. **Voice capture** – The Android app uploads a user-provided voice clip to the backend.
2. **Voice cloning** – The backend delegates to a voice cloning engine (e.g. Coqui TTS,
   Resemblyzer, Torchaudio) to extract a speaker embedding and stores it for reuse.
3. **Content creation**
   - *Creative Mode*: user story/script → story segmentation → scene generation (LLM) →
     storyboard frames (image generator) → animation timeline JSON.
   - *Broadcast Mode*: user portrait + cloned voice + script → lip-sync video compositor.
4. **Rendering** – The backend coordinates media rendering (FFmpeg / custom service)
   and pushes progress notifications via WebSocket.
5. **Delivery** – Rendered assets are exposed via object storage and downloadable
   from the Android app.

## Modules

| Module | Path | Description |
| --- | --- | --- |
| Android app | `app/` | Native app built with Jetpack Compose, Retrofit, ExoPlayer, and CameraX. |
| Spring Boot backend | `backend/` | REST + WebSocket services, orchestrates AI pipelines. |
| Vue dashboard | `web-dashboard/` | Optional creator dashboard for rich editing and review. |

## Key Services

- **VoiceCloneService** – Wraps speaker embedding extraction, voice conversion model.
- **StoryboardingService** – Calls LLM/image APIs to turn script into structured scenes.
- **RenderService** – Runs background rendering jobs, publishes progress updates.
- **MediaStorageService** – Persists raw uploads and rendered outputs (e.g. MinIO/S3).

## Data Contracts

```
POST /api/v1/voices
  multipart/form-data voiceFile -> VoiceId, timbreEmbedding

POST /api/v1/stories
  { mode: CREATIVE/BROADCAST, script, voiceId, avatarImage? }
  -> jobId

GET /api/v1/jobs/{jobId}
  -> { status, progress, downloadUrl? }
```

WebSocket channel `/topic/jobs/{jobId}` streams `{ status, progress, message }`.

## Android Client Layout

- `ui` – Compose screens (`WelcomeScreen`, `CreativeScreen`, `BroadcastScreen`).
- `viewmodel` – ViewModels using Hilt to inject use cases.
- `data` – Retrofit API definitions, repository, DTO mapping.
- `domain` – Use cases (`SubmitStoryUseCase`, `UploadVoiceUseCase`).

## Rendering Pipeline Notes

- Voice cloning and animation rendering are CPU/GPU intensive. Back-pressure is
  handled with a job queue (Spring Batch + Redis or custom scheduler).
- Model hosting can be external (e.g. replicate.com) accessed via adapters to keep
  the core service testable.
- FFmpeg/Deforum or D-ID style avatar rendering are abstracted via the
  `RenderAdapter` interface so providers can be swapped without touching the API.

## Next Steps

1. Implement the backend scaffolding (`backend/` module) with controllers and services.
2. Scaffold the Android app packages, Retrofit client, Compose screens.
3. Wire continuous integration (Gradle + Maven + Node) once build scripts land.

## Implementation Snapshot (Oct 2024)

- **Backend**
  - `VoiceController`, `StoryController`, `JobController` expose `/api/v1/voices`, `/api/v1/stories`, `/api/v1/jobs/{id}`.
  - `VoiceCloneService` hashes uploaded audio to simulate timbre embeddings and persists metadata in an in-memory `VoiceStore`.
  - `StoryWorkflowService` validates requests, persists `GenerationJob` instances, and hands execution to `JobWorkflowOrchestrator`.
  - `JobWorkflowOrchestrator` runs a synthetic pipeline that walks through `PREPROCESSING → CLONING_VOICE → GENERATING_SCRIPT → GENERATING_ASSETS → RENDERING → COMPLETED`, broadcasting progress over STOMP `/topic/jobs/{id}`.

- **Android App**
  - Compose UI mirrors the three-step web dashboard: voice cloning, mode selection, story submission, and job tracking.
  - Retrofit stack targets `http://10.0.2.2:8080/` by default; repositories wrap DTO ↔ domain conversions.
  - `VirtualPresenterViewModel` orchestrates uploads, story submissions, and polling-based job tracking (1.5s cadence) while keeping UI state in a `StateFlow`.

- **Known Gaps**
  - Voice cloning and rendering remain simulated; replace `VoiceCloneServiceImpl` and `JobWorkflowOrchestrator` with real adapters when models are ready.
  - Push progress via WebSocket on Android once a STOMP client is integrated; the current client relies on REST polling.
