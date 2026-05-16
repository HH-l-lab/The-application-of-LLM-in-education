package com.henu.web.controller.message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局系统消息 WebSocket 接口
 * 用于向特定用户实时推送站内信（小铃铛）通知
 */
@Component
@ServerEndpoint("/websocket/message/{userId}")
public class MessageWebSocket {

    private static final Logger log = LoggerFactory.getLogger(MessageWebSocket.class);

    // 存放所有用户的连接 userId -> Session
    private static final ConcurrentHashMap<String, Session> sessionPool = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        if (userId != null && !userId.equals("undefined") && !userId.equals("null")) {
            sessionPool.put(userId, session);
            log.info("【系统消息】用户 {} 上线，当前在线人数：{}", userId, sessionPool.size());
        }
    }

    @OnClose
    public void onClose(Session session, @PathParam("userId") String userId) {
        if (userId != null) {
            sessionPool.remove(userId);
            log.info("【系统消息】用户 {} 离线，当前在线人数：{}", userId, sessionPool.size());
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        // 心跳或客户端上行消息
        log.debug("收到客户端消息: {}", message);
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("【系统消息】WebSocket 发生错误", error);
    }

    /**
     * 向指定用户发送实时消息通知
     * 
     * @param userId  接收方用户ID
     * @param message 消息内容（建议下发 "NEW_MESSAGE" 信号让前端去拉取）
     */
    public static void sendMessageToUser(String userId, String message) {
        Session session = sessionPool.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                log.info("【系统消息】成功向用户 {} 推送实时消息: {}", userId, message);
            } catch (IOException e) {
                log.error("【系统消息】向用户 {} 推送消息失败", userId, e);
            }
        }
    }

    /**
     * 发送广播消息（比如系统公告）
     */
    public static void sendAllMessage(String message) {
        for (Session session : sessionPool.values()) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    log.error("【系统消息】广播消息失败", e);
                }
            }
        }
    }
}
