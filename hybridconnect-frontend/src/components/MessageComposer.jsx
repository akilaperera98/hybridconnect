import { useState } from "react";

export default function MessageComposer({ onSend, disabled }) {
  const [text, setText] = useState("");

  function send() {
    const t = text.trim();
    if (!t) return;
    onSend(t);
    setText("");
  }

  return (
    <div style={{ borderTop: "1px solid #333", padding: 10, display: "flex", gap: 10 }}>
      <input
        value={text}
        onChange={(e) => setText(e.target.value)}
        placeholder="Type message..."
        style={{ flex: 1, padding: 10 }}
        onKeyDown={(e) => {
          if (e.key === "Enter") send();
        }}
      />
      <button onClick={send} disabled={disabled || !text.trim()} style={{ padding: "10px 14px" }}>
        Send
      </button>
    </div>
  );
}
