export default function ConversationList({ items, activeOtherId, onSelect }) {
  return (
    <div style={{ width: 320, borderRight: "1px solid #333", padding: 10 }}>
      <h3>Chats</h3>
      {items.map((c) => (
        <div
          key={c.otherUserId}
          onClick={() => onSelect(c)}
          style={{
            padding: 10,
            borderRadius: 10,
            cursor: "pointer",
            background: c.otherUserId === activeOtherId ? "#1f2937" : "transparent",
            marginBottom: 6,
            display: "flex",
            justifyContent: "space-between",
            gap: 10,
          }}
          data-id={c.otherUserId}
        >
          <div style={{ minWidth: 0 }}>
            <div style={{ fontWeight: 700 }}>{c.otherUserName}</div>
            <div style={{ opacity: 0.8, whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>
              {c.lastMessage || ""}
            </div>
          </div>

          {Number(c.unreadCount || 0) > 0 && (
            <div style={{ background: "red", color: "white", borderRadius: 999, padding: "2px 8px", height: "fit-content" }}>
              {c.unreadCount}
            </div>
          )}
        </div>
      ))}
    </div>
  );
}
