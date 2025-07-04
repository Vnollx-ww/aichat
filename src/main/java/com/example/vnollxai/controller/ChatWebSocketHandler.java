package com.example.vnollxai.controller;
import com.example.vnollxai.utils.AiChat;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;
import java.io.IOException;
import java.time.Duration;
public class ChatWebSocketHandler extends TextWebSocketHandler {

    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) {
        String userInput = message.getPayload();
        // 调用AI聊天逻辑
        Flux<String> responseFlux = AiChat.chat(userInput);

        // 订阅Flux并通过WebSocket发送结果
        responseFlux.subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        chunk -> {
                            try {
                                session.sendMessage(new TextMessage(chunk));
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        },
                        error -> {
                            try {
                                session.sendMessage(new TextMessage("服务异常: " + error.getMessage()));
                                session.close(CloseStatus.SERVER_ERROR);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        },
                        () -> {
                            try {
                                session.sendMessage(new TextMessage("")); // 结束标记
                                session.close(CloseStatus.NORMAL);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                );

        // 发送心跳包（每30秒）
        Flux.interval(Duration.ofSeconds(30))
                .subscribe(i -> {
                    try {
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage("[HEARTBEAT]"));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // 清理资源
    }
}
