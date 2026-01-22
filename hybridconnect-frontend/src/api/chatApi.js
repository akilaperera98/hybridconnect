import { apiFetch } from "./http";

export function getConversations() {
  return apiFetch("/api/chat/conversations");
}

export function getMessagesWith(otherUserId) {
  return apiFetch(`/api/chat/messages/with/${otherUserId}`);
}

export function markSeen(conversationId) {
  return apiFetch(`/api/chat/messages/seen/${conversationId}`, { method: "PUT" });
}
