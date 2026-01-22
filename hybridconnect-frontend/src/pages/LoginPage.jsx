import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from "../api/authApi";
import { setAuth } from "../auth/authStore";

export default function LoginPage() {
  const nav = useNavigate();
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [err, setErr] = useState("");
  const [loading, setLoading] = useState(false);

  async function onSubmit(e) {
    e.preventDefault();
    setErr("");
    setLoading(true);

    try {
      const res = await login(email, password);

      // backend returns: { success, message, userId, role, token }
      setAuth({ token: res.token, myId: res.userId });

      nav("/chat", { replace: true });
    } catch (ex) {
      setErr(ex.message || "Login failed");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div style={{ minHeight: "100vh", display: "grid", placeItems: "center", background: "#0b1220" }}>
      <form
        onSubmit={onSubmit}
        style={{
          width: 420,
          maxWidth: "92vw",
          background: "#0f1a33",
          border: "1px solid rgba(255,255,255,.08)",
          borderRadius: 16,
          padding: 24,
          color: "#e5e7eb",
        }}
      >
        <h2 style={{ margin: 0, marginBottom: 16 }}>HybridConnect Login</h2>

        <input
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          placeholder="Email"
          style={inputStyle}
        />
        <input
          type="password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          placeholder="Password"
          style={inputStyle}
        />

        {err && (
          <div style={{ marginTop: 12, background: "#7f1d1d", padding: 10, borderRadius: 12 }}>
            {err}
          </div>
        )}

        <button
          type="submit"
          disabled={loading}
          style={{
            width: "100%",
            marginTop: 14,
            padding: 12,
            border: 0,
            borderRadius: 12,
            fontWeight: 800,
            background: "#2563eb",
            color: "white",
            cursor: "pointer",
            opacity: loading ? 0.7 : 1,
          }}
        >
          {loading ? "Logging in..." : "Login"}
        </button>
      </form>
    </div>
  );
}

const inputStyle = {
  width: "100%",
  marginTop: 10,
  padding: 12,
  borderRadius: 12,
  border: "1px solid rgba(255,255,255,.1)",
  outline: "none",
  background: "#0b1220",
  color: "#e5e7eb",
};
