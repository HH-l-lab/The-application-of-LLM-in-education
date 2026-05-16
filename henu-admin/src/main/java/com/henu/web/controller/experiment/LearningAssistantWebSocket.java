package com.henu.web.controller.experiment;

import com.henu.common.utils.ai.DeepSeekClient;
import com.henu.common.utils.spring.SpringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 智能伴学 WebSocket 接口
 * 用于学生播放视频或填写表单时的实施大模型交互
 */
@Component
@ServerEndpoint("/websocket/learningRoom")
public class LearningAssistantWebSocket {

    private static final Logger log = LoggerFactory.getLogger(LearningAssistantWebSocket.class);

    // 静态变量，用来记录当前在线连接数
    private static final ConcurrentHashMap<String, Session> sessionPool = new ConcurrentHashMap<>();

    // 存放上下文系统 Prompt 等
    private static final String SYSTEM_PROMPT = "你是一个专业的高校/中学理化生实验助教。你的任务是帮助学生预检实验数据、" +
            "解答相关原理概念，并指出操作过程中的潜在风险。回复应有逻辑、引导性强，并且不能直接告诉学生数据填多少，而是要让他们自己算出来。请使用富文本格式清晰地排版。" +
            "你可以使用 Markdown 公式格式（例如 $$ 或 \\( \\)），但为了最好的全平台可读性，如果是极为简单的公式（如 v=s/t），也可以使用普通文本表达。";

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        sessionPool.put(session.getId(), session);
        log.info("【伴学自习室】有新连接加入：{}，当前在线人数：{}", session.getId(), sessionPool.size());
        try {
            session.getBasicRemote().sendText("【系统提示】智能伴学助手已连接！您可以随时向我提问本次实验的任何问题。");
        } catch (IOException e) {
            log.error("WebSocket 发送消息异常", e);
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        sessionPool.remove(session.getId());
        log.info("【伴学自习室】连接断开：{}，当前在线人数：{}", session.getId(), sessionPool.size());
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("【伴学自习室】收到用户消息: {}", message);
        try {
            // 通过 SpringUtils 取出 DeepSeekClient bean
            DeepSeekClient deepSeekClient = SpringUtils.getBean(DeepSeekClient.class);

            if (deepSeekClient == null) {
                sendMessage(session, "抱歉，AI 导师服务暂未就绪。");
                return;
            }

            // 调用流式接口，逐步返回大模型推演数据
            deepSeekClient.streamChat(
                    SYSTEM_PROMPT,
                    message,
                    token -> sendMessage(session, token),
                    () -> log.info("【伴学自习室】流式回答完毕 - Session: {}", session.getId()),
                    error -> {
                        log.error("AI 伴学回答流异常", error);
                        sendMessage(session, "\n[系统提示：导师思维短路了，请稍后再试 (" + error.getMessage() + ")]");
                    });

        } catch (Exception e) {
            log.error("AI 伴学回答异常", e);
            sendMessage(session, "对不起，智能助手暂时开小差了，请稍后再试。错误信息：" + e.getMessage());
        }
    }

    private synchronized void sendMessage(Session session, String text) {
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(text);
            } catch (IOException e) {
                log.error("发送消息出错", e);
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("学习助手 WebSocket 发生错误", error);
    }
}
