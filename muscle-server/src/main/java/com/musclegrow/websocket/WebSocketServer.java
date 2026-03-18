package com.musclegrow.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket服务
 */
@Component
@ServerEndpoint("/ws/{sid}")
@Slf4j
public class WebSocketServer {

    private static final Map<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("sid") String sid) {
        SESSION_MAP.put(sid, session);
        log.info("websocket connected, sid={}, onlineCount={}", sid, SESSION_MAP.size());
    }

    @OnMessage
    public void onMessage(String message, @PathParam("sid") String sid) {
        log.info("websocket message received, sid={}, message={}", sid, message);
    }

    @OnClose
    public void onClose(@PathParam("sid") String sid) {
        SESSION_MAP.remove(sid);
        log.info("websocket disconnected, sid={}, onlineCount={}", sid, SESSION_MAP.size());
    }

    @OnError
    public void onError(Session session, Throwable throwable, @PathParam("sid") String sid) {
        SESSION_MAP.remove(sid);
        log.warn("websocket error, sid={}", sid, throwable);
        if (session != null && session.isOpen()) {
            try {
                session.close();
            } catch (IOException e) {
                log.warn("close websocket session failed, sid={}", sid, e);
            }
        }
    }

    public void sendToAllClient(String message) {
        SESSION_MAP.entrySet().removeIf(entry -> entry.getValue() == null || !entry.getValue().isOpen());

        for (Map.Entry<String, Session> entry : SESSION_MAP.entrySet()) {
            try {
                entry.getValue().getBasicRemote().sendText(message);
            } catch (IOException e) {
                log.warn("websocket broadcast failed, sid={}", entry.getKey(), e);
            }
        }
    }
}
