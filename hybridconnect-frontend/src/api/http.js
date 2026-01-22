import { getToken } from "../auth/authStore";

export async function apiFetch(url, options = {}) {
  const token = getToken();

  const headers = {
    ...(options.headers || {}),
  };

  // only add content-type if body exists (FormData breaks if we force JSON)
  const hasBody = options.body !== undefined && options.body !== null;
  const isFormData = hasBody && (options.body instanceof FormData);

  if (!isFormData && !headers["Content-Type"] && !headers["content-type"]) {
    headers["Content-Type"] = "application/json";
  }

  if (token) headers.Authorization = `Bearer ${token}`;

  console.log("apiFetch ->", url, "AUTH?", headers.Authorization ? "YES" : "NO");

  const res = await fetch(url, { ...options, headers });

  const text = await res.text();
  let data = null;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = text;
  }

  if (res.status === 401) {
    throw new Error("UNAUTHORIZED");
  }

  if (!res.ok) {
    const msg = typeof data === "string" ? data : (data?.message || res.statusText);
    throw new Error(msg);
  }

  return data;
}
