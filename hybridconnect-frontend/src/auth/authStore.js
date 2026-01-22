const TOKEN_KEY = "token";
const MYID_KEY = "myId";

function normalizeToken(t) {
  if (!t) return null;

  let token = String(t).trim();

  // remove quotes if stored like "eyJ..."
  token = token.replace(/^"+|"+$/g, "");

  // if someone stored "Bearer eyJ..." already, remove "Bearer "
  if (token.toLowerCase().startsWith("bearer ")) {
    token = token.slice(7).trim();
  }

  // remove accidental newlines
  token = token.replace(/\s+/g, " ").trim();

  return token || null;
}

export function setAuth({ token, myId }) {
  const clean = normalizeToken(token);
  if (clean) localStorage.setItem(TOKEN_KEY, clean);
  if (myId !== undefined && myId !== null) localStorage.setItem(MYID_KEY, String(myId));
}

export function getToken() {
  return normalizeToken(localStorage.getItem(TOKEN_KEY));
}

export function getMyId() {
  const v = localStorage.getItem(MYID_KEY);
  return v ? Number(v) : null;
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(MYID_KEY);
}

export function isLoggedIn() {
  return !!getToken();
}
