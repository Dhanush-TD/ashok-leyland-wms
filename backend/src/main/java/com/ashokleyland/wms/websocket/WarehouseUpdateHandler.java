package com.ashokleyland.wms.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Plain WebSocket endpoint backing /ws/warehouse-updates.
 * Broadcasts LOCATION_UPDATE, TASK_ASSIGNED and ALERT_MISMATCH events (see API_Documentation.md #5)
 * to every connected client (map viewers, operator handhelds, supervisor dashboards).
 */
@Component
public class WarehouseUpdateHandler extends TextWebSocketHandler {

    private final CopyOnWriteArraySet<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    public void broadcast(String eventType, Object payload) {
        try {
            String json = objectMapper.writeValueAsString(Map.of("type", eventType, "payload", payload));
            TextMessage message = new TextMessage(json);
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(message);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to broadcast warehouse update", e);
        }
    }
}
