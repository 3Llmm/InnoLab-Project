import { type NextRequest } from "next/server";

export const dynamic = "force-dynamic";

const DEFAULT_BACKEND_URL = "http://localhost:8080";
const BODYLESS_METHODS = new Set(["GET", "HEAD"]);
const HOP_BY_HOP_HEADERS = new Set([
  "connection",
  "content-encoding",
  "content-length",
  "host",
  "keep-alive",
  "proxy-authenticate",
  "proxy-authorization",
  "te",
  "trailer",
  "transfer-encoding",
  "upgrade",
]);

type RouteContext = {
  params: Promise<{
    path?: string[];
  }>;
};

function getBackendBaseUrl() {
  const configuredUrl =
    process.env.API_PROXY_TARGET || process.env.API_URL || DEFAULT_BACKEND_URL;

  return configuredUrl.replace(/\/+$/, "");
}

function getForwardHeaders(request: NextRequest) {
  const headers = new Headers(request.headers);

  for (const header of HOP_BY_HOP_HEADERS) {
    headers.delete(header);
  }

  return headers;
}

function getResponseHeaders(headers: Headers) {
  const responseHeaders = new Headers(headers);

  for (const header of HOP_BY_HOP_HEADERS) {
    responseHeaders.delete(header);
  }

  return responseHeaders;
}

async function proxyRequest(request: NextRequest, context: RouteContext) {
  const { path = [] } = await context.params;
  const backendUrl = new URL(
    `/api/${path.map(encodeURIComponent).join("/")}`,
    getBackendBaseUrl(),
  );
  backendUrl.search = request.nextUrl.search;

  const backendResponse = await fetch(backendUrl, {
    method: request.method,
    headers: getForwardHeaders(request),
    body: BODYLESS_METHODS.has(request.method)
      ? undefined
      : await request.arrayBuffer(),
    redirect: "manual",
  });

  return new Response(backendResponse.body, {
    status: backendResponse.status,
    statusText: backendResponse.statusText,
    headers: getResponseHeaders(backendResponse.headers),
  });
}

export const GET = proxyRequest;
export const HEAD = proxyRequest;
export const POST = proxyRequest;
export const PUT = proxyRequest;
export const PATCH = proxyRequest;
export const DELETE = proxyRequest;
export const OPTIONS = proxyRequest;
