package com.example.vnollxai.service.impl;

import com.example.vnollxai.entity.Message;
import com.example.vnollxai.mapper.AiMapper;
import com.example.vnollxai.service.AiService;
import com.example.vnollxai.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AiServiceImpl implements AiService {
    @Autowired
    private AiMapper aiMapper;
    @Override
    public Result addMessage(String time, String question, String thinking, String response, int uid) {
        aiMapper.addMessage(time,question,thinking,response,uid);
        if (aiMapper.getMessageCount(uid)>5){
            aiMapper.deleteMessage(uid);
        }
        return Result.Success("添加消息记录成功");
    }

    @Override
    public Result getMessageList(int uid) {
        List<Message> messageList=aiMapper.getMessageList(uid);
        return Result.Success(messageList,"获取历史聊天记录成功");
    }
}
