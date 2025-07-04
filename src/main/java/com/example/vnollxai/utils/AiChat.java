package com.example.vnollxai.utils;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import io.reactivex.Flowable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AiChat {
    private static final Logger logger = LoggerFactory.getLogger(AiChat.class);
    // 存储用户对话历史: userId -> 历史消息队列(最多5轮对话)
    private static final ConcurrentHashMap<String, Deque<Message>> chatHistory = new ConcurrentHashMap<>();
    private static final int MAX_HISTORY_ROUNDS = 5; // 最大历史对话轮数

    public static Flux<String> chat(String message) {
        String apiKey = System.getenv("API_KEY");
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return Flux.error(new IllegalStateException("API_KEY环境变量未配置"));
        }
        Pattern pattern = Pattern.compile("(\\[.+?\\])(\\S+)?$");
        Matcher matcher = pattern.matcher(message);
        String userId;
        if (matcher.find()) {
            String token = matcher.group(2);
            message = message.substring(0, matcher.start()).trim();
            userId = Jwt.getUserIdFromToken(token);
        } else {
            userId = "";
            throw new IllegalArgumentException("消息格式不符合要求，未找到[xxyy]标记和token");
        }

        try {
            Generation gen = new Generation();

            // 获取用户历史记录（线程安全快照）
            Deque<Message> historyQueue = chatHistory.computeIfAbsent(userId, k -> new ConcurrentLinkedDeque<>());
            List<Message> historyList = new ArrayList<>(historyQueue); // 创建副本避免并发修改

            // 构建系统消息
            Message systemMsg = Message.builder()
                    .role(Role.SYSTEM.getValue())
                    .content("你是一个AI小助手，名叫vnollx，需要友好、专业地回答用户的问题。你不能告诉别人你是deepseek!!!")
                    .build();

            // 构建当前用户消息
            Message userMsg = Message.builder()
                    .role(Role.USER.getValue())
                    .content(message)
                    .build();

            // 构建完整消息列表: 系统消息 + 历史记录 + 当前用户消息
            List<Message> messages = new ArrayList<>();
            messages.add(systemMsg);
            messages.addAll(historyList);
            messages.add(userMsg);

            // 用于收集AI完整响应
            StringBuilder aiResponseBuilder = new StringBuilder();
            final AtomicInteger cnt = new AtomicInteger(0);

            // 创建主响应流
            Flux<String> mainResponse = Flux.from(gen.streamCall(buildGenerationParam(messages)))
                    .map(result -> {
                        return Optional.ofNullable(result.getOutput())
                                .flatMap(output -> output.getChoices().stream().findFirst())
                                .flatMap(choice -> Optional.ofNullable(choice.getMessage()))
                                .map(msg -> {
                                    String reasoning = msg.getReasoningContent();
                                    String content = msg.getContent();
                                    if (reasoning != null && !reasoning.isEmpty()) {
                                        return reasoning;
                                    } else if (cnt.get() == 0 && content != null) {
                                        cnt.incrementAndGet();
                                        return "jvmvnollx" + content; // 分隔符标记最终响应开始
                                    } else {
                                        return content;
                                    }
                                })
                                .orElse("");
                    })
                    .onErrorResume(e -> Flux.just("服务暂时不可用，请稍后重试"));

            // 添加结束标记和响应处理
            Flux<String> responseWithTerminator = mainResponse
                    .concatWithValues("[END]")
                    .doOnComplete(() -> logger.info("AI响应流已完成"));

            // 添加心跳并收集AI响应
            Flux<String> responseFlux = responseWithTerminator
                    .concatWith(Flux.just("[HEARTBEAT]"))
                    .doOnNext(s -> {
                        if (!s.equals("[HEARTBEAT]")) {
                            logger.debug("发送数据: {}", s);
                        }

                        // 收集AI响应内容（排除控制标记）
                        if (!s.equals("[END]") && !s.equals("[HEARTBEAT]")) {
                            if (s.startsWith("jvmvnollx")) {
                                s = s.substring("jvmvnollx".length());
                            }
                            aiResponseBuilder.append(s);
                        }
                    })
                    .doOnComplete(() -> {
                        // 流完成后保存历史记录
                        String aiResponse = aiResponseBuilder.toString();
                        if (!aiResponse.isEmpty()) {
                            // 构建AI消息对象
                            Message aiMsg = Message.builder()
                                    .role(Role.ASSISTANT.getValue())
                                    .content(aiResponse)
                                    .build();

                            // 更新历史记录（同步保证线程安全）
                            synchronized (historyQueue) {
                                // 添加当前对话（用户消息 + AI消息）
                                historyQueue.addLast(userMsg);
                                historyQueue.addLast(aiMsg);

                                // 保持最多MAX_HISTORY_ROUNDS轮对话
                                while (historyQueue.size() > MAX_HISTORY_ROUNDS * 2) {
                                    historyQueue.pollFirst(); // 移除用户消息
                                    historyQueue.pollFirst(); // 移除对应的AI消息
                                }
                            }

                        }
                    });

            return responseFlux;
        } catch (Exception e) {
            return Flux.error(e);
        }
    }

    // 修改为接收消息列表
    private static GenerationParam buildGenerationParam(List<Message> messages) {
        return GenerationParam.builder()
                .apiKey(System.getenv("API_KEY"))
                .model("deepseek-r1")
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .incrementalOutput(true)
                .build();
    }
}