// utils/websocket.ts
import { getAccessToken } from "@/utils/auth";

export function initializeWebSocket(onMessage: (this: WebSocket, ev: MessageEvent) => any): WebSocket {
  const token = getAccessToken();
  const socket = new WebSocket(`ws://ip:8889/webSocket?accessToken=${token}`);
//ws://ip:8889/webSocket
  socket.onopen = () => console.log("WebSocket 已连接");
  socket.onmessage = onMessage;
  socket.onclose = () => console.log("WebSocket 已关闭");

  return socket;
}
