# FaceLab Studio API

Cloudflare Worker API using the existing private `paliaapk-hub` R2 bucket. All FaceLab objects use the `facelab/` prefix; existing PaliaAPK Hub objects are not modified.

## Routes

- `GET /health` — service and provider readiness.
- `POST /v1/jobs` — create an upload job and one-time bearer token.
- `PUT /v1/jobs/:id/face` — upload source image (JPEG/PNG/WebP, max 12 MB).
- `PUT /v1/jobs/:id/video` — upload target video (MP4/MOV/WebM, max 80 MB).
- `POST /v1/jobs/:id/process` — validate uploaded assets and check AI provider readiness.
- `GET /v1/jobs/:id/status` — inspect job status.

All job routes require `Authorization: Bearer <uploadToken>`.

## AI status

The Worker intentionally does not claim a face swap has run. A compatible video face-swap model endpoint and model-specific adapter are still required. Until then, process returns `waiting_for_ai_provider` and keeps the uploaded media in private R2 storage.

## Deploy

From this folder, use Cloudflare's current `cf` CLI or Wrangler if the project is already configured for Wrangler. Bind the existing `paliaapk-hub` R2 bucket. Do not make the bucket public.
