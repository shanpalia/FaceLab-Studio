const json = (data, status = 200) => new Response(JSON.stringify(data), {
  status,
  headers: { "content-type": "application/json; charset=utf-8", "cache-control": "no-store", "access-control-allow-origin": "*" }
});

const MAX_FACE_BYTES = 12 * 1024 * 1024;
const MAX_VIDEO_BYTES = 80 * 1024 * 1024;

function cors(response) {
  const headers = new Headers(response.headers);
  headers.set("access-control-allow-origin", "*");
  headers.set("access-control-allow-methods", "GET,POST,PUT,OPTIONS");
  headers.set("access-control-allow-headers", "content-type,authorization");
  headers.set("access-control-max-age", "86400");
  return new Response(response.body, { status: response.status, headers });
}

async function loadJob(env, id) {
  const object = await env.FACELAB_MEDIA.get(`facelab/jobs/${id}.json`);
  if (!object) return null;
  return object.json();
}
async function saveJob(env, job) {
  await env.FACELAB_MEDIA.put(`facelab/jobs/${job.id}.json`, JSON.stringify(job), {
    httpMetadata: { contentType: "application/json" }
  });
}
async function authorized(env, id, request) {
  const job = await loadJob(env, id);
  const token = request.headers.get("authorization")?.replace(/^Bearer\s+/i, "");
  if (!job || !token || token !== job.uploadToken) return null;
  return job;
}

export default {
  async fetch(request, env) {
    if (request.method === "OPTIONS") return cors(new Response(null, { status: 204 }));
    const url = new URL(request.url);
    const parts = url.pathname.split("/").filter(Boolean);

    if (request.method === "GET" && url.pathname === "/health") {
      return cors(json({ ok: true, service: "FaceLab Studio API", storage: "connected", ai: env.AI_PROVIDER_READY === "true" ? "ready" : "not_ready" }));
    }

    if (request.method === "POST" && url.pathname === "/v1/jobs") {
      if (env.AI_PROVIDER_READY !== "true") {
        return cors(json({ error: "AI processing is not enabled. Media upload is disabled until a provider adapter is deployed." }, 503));
      }
      const id = crypto.randomUUID();
      const uploadToken = Array.from(crypto.getRandomValues(new Uint8Array(32)), b => b.toString(16).padStart(2, "0")).join("");
      const job = {
        id, uploadToken, status: "created", createdAt: new Date().toISOString(),
        faceKey: null, videoKey: null, resultKey: null,
        message: "Upload the source face and target video."
      };
      await saveJob(env, job);
      return cors(json({ jobId: id, uploadToken, status: job.status, limits: { faceBytes: MAX_FACE_BYTES, videoBytes: MAX_VIDEO_BYTES } }, 201));
    }

    if (parts[0] === "v1" && parts[1] === "jobs" && parts[2]) {
      const id = parts[2];
      const job = await authorized(env, id, request);
      if (!job) return cors(json({ error: "Job not found or upload token invalid." }, 404));

      if (request.method === "PUT" && (parts[3] === "face" || parts[3] === "video")) {
        const kind = parts[3];
        const contentType = request.headers.get("content-type") || "application/octet-stream";
        const allowed = kind === "face"
          ? ["image/jpeg", "image/png", "image/webp"]
          : ["video/mp4", "video/quicktime", "video/webm", "application/octet-stream"];
        if (!allowed.includes(contentType.split(";")[0].trim().toLowerCase())) return cors(json({ error: "Unsupported media type." }, 415));
        const bytes = Number(request.headers.get("content-length") || 0);
        const max = kind === "face" ? MAX_FACE_BYTES : MAX_VIDEO_BYTES;
        if (!bytes || bytes > max) return cors(json({ error: `File must be under ${Math.round(max / 1024 / 1024)} MB.` }, 413));
        const key = `facelab/uploads/${id}/${kind}`;
        await env.FACELAB_MEDIA.put(key, request.body, { httpMetadata: { contentType } });
        if (kind === "face") job.faceKey = key; else job.videoKey = key;
        job.status = job.faceKey && job.videoKey ? "uploaded" : "uploading";
        job.message = job.faceKey && job.videoKey ? "Both files uploaded. Ready to request AI processing." : `${kind} uploaded.`;
        await saveJob(env, job);
        return cors(json({ jobId: id, status: job.status, message: job.message }));
      }

      if (request.method === "POST" && parts[3] === "process") {
        if (!job.faceKey || !job.videoKey) return cors(json({ error: "Upload both the face photo and target video first." }, 409));
        if (!env.AI_PROVIDER_URL) {
          job.status = "waiting_for_ai_provider";
          job.message = "Media uploaded securely. No compatible free video face-swap GPU provider is configured yet.";
          await saveJob(env, job);
          return cors(json({ jobId: id, status: job.status, message: job.message, providerConfigured: false }, 503));
        }
        job.status = "provider_integration_required";
        job.message = "An AI provider URL exists, but its model-specific request adapter must be configured before processing.";
        await saveJob(env, job);
        return cors(json({ jobId: id, status: job.status, message: job.message }, 501));
      }

      if (request.method === "GET" && parts[3] === "status") {
        const { uploadToken, ...safeJob } = job;
        return cors(json(safeJob));
      }
    }
    return cors(json({ error: "Route not found." }, 404));
  }
};
