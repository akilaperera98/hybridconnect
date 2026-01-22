import SockJS from "sockjs-client/dist/sockjs";
import Stomp from "stompjs";

export function createWsClient({ token, onConnected, onError }) {
  const socket = new SockJS("/ws");
  const client = Stomp.over(socket);
  client.debug = null;

  client.connect(
    { Authorization: "Bearer " + token },
    (frame) => onConnected?.(client, frame),
    (err) => onError?.(err)
  );

  return client;
}

export function subscribeMessages(client, onMessage) {
  // server sends to /user/queue/messages
  return client.subscribe("/user/queue/messages", (msg) => {
    try {
      onMessage(JSON.parse(msg.body));
    } catch {
      // ignore bad JSON
    }
  });
}

export function sendChatMessage(client, { toUserId, text }) {
  client.send("/app/chat.send", {}, JSON.stringify({ toUserId, text }));
}
