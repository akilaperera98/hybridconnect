export default function MessageList({ myId, messages }) {
  return (
    <div style={{ flex: 1, padding: 12, overflowY: "auto" }}>
      {messages.map((m) => {
        const me = m.senderId === myId;
        return (
          <div key={m.id} style={{ display: "flex", justifyContent: me ? "flex-end" : "flex-start", margin: "8px 0" }}>
            <div style={{ padding: 10, borderRadius: 12, maxWidth: 520, background: me ? "#93c5fd" : "#bbf7d0" }}>
              <div>{m.text}</div>
              <div style={{ fontSize: 12, opacity: 0.8, marginTop: 4, textAlign: "right" }}>
                {m.seen && me ? "Seen" : ""}
              </div>
            </div>
          </div>
        );
      })}
    </div>
  );
}
