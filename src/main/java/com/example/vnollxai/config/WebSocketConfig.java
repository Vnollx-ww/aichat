package com.example.vnollxai.config;

import com.example.vnollxai.controller.ChatWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler(), "/ai/chat")
                .setAllowedOrigins("*"); // 生产环境建议限制源
    }

    @Bean
    public WebSocketHandler chatWebSocketHandler() {
        return (WebSocketHandler) new ChatWebSocketHandler();
    }
}