package com.example.vnollxai.service;

import com.example.vnollxai.utils.Result;

import java.util.concurrent.locks.ReentrantLock;

public interface AiService {
    Result addMessage(String time, String question, String thinking, String response, int uid);
    Result getMessageList(int uid);
}
