import { useEffect, useMemo, useRef, useState } from "react";
import "../styles/chat.css";
import { getMyId, getToken, clearAuth } from "../auth/authStore";
import { getConversations, getMessagesWith, markSeen } from "../api/chatApi";

import SockJS from "sockjs-client/dist/sockjs";
import { Client } from "@stomp/stompjs";

export default function ChatPage() {
  const myId = useMemo(() => getMyId(), []);
  const token = useMemo(() => getToken(), []);

  const stompRef = useRef(null);

  const [wsState, setWsState] = useState("connecting");

  const [conversations, setConversations] = useState([]);
  const [search, setSearch] = useState("");

  const [currentOtherId, setCurrentOtherId] = useState(null);
  const [currentOtherName, setCurrentOtherName] = useState(null);
  const [messages, setMessages] = useState([]);

  const [text, setText] = useState("");

  // ---- Typing states ----
  // typingMap[userId] = true/false for preview list
  const [typingMap, setTypingMap] = useState({});
  const typingTimeoutsRef = useRef(new Map()); // userId -> timeoutId
  const typingSendTimerRef = useRef(null); // debounce timer

  const messagesWrapRef = useRef(null);

  // -------------------- helpers --------------------
  async function loadConversations() {
    const list = await getConversations();
    setConversations(Array.isArray(list) ? list : []);
  }

  function logout() {
    clearAuth();
    window.location.href = "/login";
  }

  function scrollToBottom() {
    const el = messagesWrapRef.current;
    if (!el) return;
    el.scrollTop = el.scrollHeight + 9999;
  }

  function setTypingFor(userId, typing) {
    setTypingMap((prev) => {
      const next = { ...prev };
      if (typing) next[userId] = true;
      else delete next[userId];
      return next;
    });
  }

  // auto clear typing after 3s (in case "typing:false" lost)
  function armTypingAutoClear(fromUserId) {
    const existing = typingTimeoutsRef.current.get(fromUserId);
    if (existing) clearTimeout(existing);

    const t = setTimeout(() => {
      setTypingFor(fromUserId, false);
      typingTimeoutsRef.current.delete(fromUserId);
    }, 3000);

    typingTimeoutsRef.current.set(fromUserId, t);
  }

  function publishTyping(isTyping) {
    const client = stompRef.current;
    if (!client || !client.connected) return;
    if (!currentOtherId) return;

    client.publish({
      destination: "/app/chat.typing",
      body: JSON.stringify({ toUserId: currentOtherId, typing: isTyping }),
    });
  }

  // debounce: user types -> send typing:true after 250ms, stop after 900ms idle
  function onComposerChange(v) {
    setText(v);

    // if no chat selected, no typing
    if (!currentOtherId) return;

    // immediate: schedule typing true
    if (typingSendTimerRef.current) clearTimeout(typingSendTimerRef.current);
    typingSendTimerRef.current = setTimeout(() => publishTyping(true), 250);

    // also: schedule auto stop when idle
    // (we just reuse the same timer technique: when user types again it resets)
    if (window.__stopTypingTimer) clearTimeout(window.__stopTypingTimer);
    window.__stopTypingTimer = setTimeout(() => publishTyping(false), 900);
  }

  async function openConversation(c) {
    setCurrentOtherId(c.otherUserId);
    setCurrentOtherName(c.otherUserName || `User ${c.otherUserId}`);
    setMessages([]);

    // when open, clear typing UI for that user
    setTypingFor(c.otherUserId, false);

    const msgs = await getMessagesWith(c.otherUserId);
    setMessages(Array.isArray(msgs) ? msgs : []);

    // mark seen
    const convoId = Array.isArray(msgs) && msgs.length ? msgs[0].conversationId : null;
    if (convoId) {
      await markSeen(convoId);
      await loadConversations(); // update unread badges
    }

    // stop typing when switching
    publishTyping(false);

    setTimeout(scrollToBottom, 50);
  }

  function sendMsg() {
    const client = stompRef.current;
    if (!client || !client.connected) return;

    const messageText = text.trim();
    if (!messageText || !currentOtherId) return;

    // IMPORTANT: do NOT optimistic-add message to UI.
    // Backend already echoes WS message to sender too -> prevents duplicates.
    client.publish({
      destination: "/app/chat.send",
      body: JSON.stringify({ toUserId: currentOtherId, text: messageText }),
    });

    setText("");
    publishTyping(false);
  }

  // Enter to send
  function onKeyDown(e) {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendMsg();
    }
  }

  // -------------------- connect WS + initial load --------------------
  useEffect(() => {
    let client;

    async function init() {
      await loadConversations();

      setWsState("connecting");

      client = new Client({
        webSocketFactory: () => new SockJS("/ws"),
        connectHeaders: { Authorization: `Bearer ${token}` },
        debug: () => {},
        onConnect: () => {
          setWsState("connected");
          stompRef.current = client;

          // messages
          client.subscribe("/user/queue/messages", async (frame) => {
            const m = JSON.parse(frame.body);

            // Update messages if belongs to opened chat
            if (
              currentOtherId &&
              (m.senderId === currentOtherId || m.receiverId === currentOtherId)
            ) {
              setMessages((prev) => [...prev, m]);
              setTimeout(scrollToBottom, 30);

              // if I am receiver and chat open -> seen
              if (m.receiverId === myId && m.conversationId) {
                await markSeen(m.conversationId);
              }
            }

            // Update left list lastMessage/unread (simple method for now)
            await loadConversations();
          });

          // typing
          client.subscribe("/user/queue/typing", (frame) => {
            const ev = JSON.parse(frame.body);
            // ev: { fromUserId, toUserId, typing }

            // typing preview only matters for senderId -> show on conversation list
            if (!ev?.fromUserId) return;

            if (ev.typing) {
              setTypingFor(ev.fromUserId, true);
              armTypingAutoClear(ev.fromUserId);
            } else {
              setTypingFor(ev.fromUserId, false);
              const t = typingTimeoutsRef.current.get(ev.fromUserId);
              if (t) clearTimeout(t);
              typingTimeoutsRef.current.delete(ev.fromUserId);
            }
          });
        },
        onStompError: () => setWsState("error"),
        onWebSocketError: () => setWsState("error"),
      });

      client.activate();
    }

    init();

    return () => {
      try {
        client?.deactivate();
      } catch {}
      // cleanup typing timeouts
      typingTimeoutsRef.current.forEach((t) => clearTimeout(t));
      typingTimeoutsRef.current.clear();
    };
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []); // once

  // If currentOtherId changes, update WS message handling closure by re-subscribing is heavy.
  // Instead: we rely on state checks; React closure in subscribe uses old value.
  // So we keep latest value in a ref:
  const currentOtherIdRef = useRef(null);
  useEffect(() => {
    currentOtherIdRef.current = currentOtherId;
  }, [currentOtherId]);

  // filtered list
  const filtered = conversations.filter((c) => {
    const q = search.trim().toLowerCase();
    if (!q) return true;
    return (
      (c.otherUserName || "").toLowerCase().includes(q) ||
      (c.lastMessage || "").toLowerCase().includes(q)
    );
  });

  const isTypingCurrent = currentOtherId ? !!typingMap[currentOtherId] : false;

  return (
    <div style={{ width: "100%", display: "flex" }}>
      {/* Left */}
      <div id="left">
        <div id="leftHeader">
          <div style={{ fontWeight: 900 }}>HybridConnect</div>
          <div id="meLine">Logged as userId: {myId ?? "?"}</div>

          <input
            id="search"
            placeholder="Search chats..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />

          <div style={{ marginTop: 10, display: "flex", gap: 10, alignItems: "center" }}>
            <div style={{ fontSize: 12, color: "#6b7280" }}>
              WS: {wsState === "connected" ? "connected ✅" : wsState === "error" ? "error ❌" : "connecting..."}
            </div>
            <button onClick={logout} style={{ marginLeft: "auto", cursor: "pointer" }}>
              Logout
            </button>
          </div>
        </div>

        <div id="conversations">
          {filtered.map((c) => {
            const typing = !!typingMap[c.otherUserId];
            return (
              <div
                key={c.otherUserId}
                className={"conv" + (c.otherUserId === currentOtherId ? " active" : "")}
                data-user-id={c.otherUserId}
                onClick={() => openConversation(c)}
              >
                <div className="avatar">
                  {(c.otherUserName || "?").slice(0, 1).toUpperCase()}
                </div>

                <div className="convMain">
                  <div className="convTop">
                    <div className="name">{c.otherUserName || `User ${c.otherUserId}`}</div>

                    <div style={{ display: "flex", alignItems: "center" }}>
                      <div className="time">{fmtTime(c.lastMessageAt)}</div>
                      {Number(c.unreadCount) > 0 && <span className="badge">{c.unreadCount}</span>}
                    </div>
                  </div>

                  <div className={"preview" + (typing ? " typing" : "")}>
                    {typing ? "typing…" : (c.lastMessage || "")}
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Right */}
      <div id="right">
        <div id="chatHeader">
          <div>
            <div id="chatTitle">
              {currentOtherName ? `Chat with ${currentOtherName}` : "Select a chat"}
            </div>
            <div id="status">
              {currentOtherName
                ? (isTypingCurrent ? `${currentOtherName} is typing…` : "Realtime enabled")
                : "Realtime enabled"}
            </div>
          </div>

          <div style={{ fontSize: 12, color: "#6b7280" }}>
            {wsState === "connected" ? "Online" : wsState === "error" ? "Offline" : "Connecting..."}
          </div>
        </div>

        <div id="messagesWrap" ref={messagesWrapRef}>
          {!currentOtherId ? (
            <div className="emptyState">Left side එකෙන් conversation එකක් select කරන්න.</div>
          ) : messages.length === 0 ? (
            <div className="emptyState">No messages yet. Say hi 👋</div>
          ) : (
            messages.map((m) => (
              <div
                key={m.id ?? `${m.senderId}-${m.createdAt}-${m.text}`}
                className={"bubbleRow " + (m.senderId === myId ? "me" : "other")}
              >
                <div className={"bubble " + (m.senderId === myId ? "me" : "other")}>
                  {m.text}
                  <div className="meta">
                    <span>{fmtTime(m.createdAt)}</span>
                    {m.senderId === myId && <span className="seenDot">{m.seen ? "Seen" : ""}</span>}
                  </div>
                </div>
              </div>
            ))
          )}
        </div>

        <div id="composer">
          <input
            id="text"
            placeholder="Type a message..."
            value={text}
            onChange={(e) => onComposerChange(e.target.value)}
            onKeyDown={onKeyDown}
            onBlur={() => publishTyping(false)}
            disabled={!currentOtherId}
          />

          <button
            id="sendBtn"
            onClick={sendMsg}
            disabled={!currentOtherId || !text.trim() || wsState !== "connected"}
          >
            Send
          </button>
        </div>
      </div>
    </div>
  );
}

function fmtTime(iso) {
  if (!iso) return "";
  const d = new Date(iso);
  if (isNaN(d.getTime())) return "";
  return d.toLocaleString([], { hour: "2-digit", minute: "2-digit" });
}
