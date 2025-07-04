package com.example.vnollxai.controller;

import com.example.vnollxai.service.AiService;
import com.example.vnollxai.utils.AiChat;
import com.example.vnollxai.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/message")
public class AiController {
    @Autowired
    private AiService aiService;
    @PostMapping("/add")
    public Result ask(@RequestParam String time, @RequestParam String question, @RequestParam String thinking, @RequestParam String response, HttpServletRequest request){
        String userId = (String) request.getAttribute("uid");
        if (userId != null) {
            return aiService.addMessage(time,question,thinking,response,Integer.parseInt(userId));
        } else {
            return Result.LogicError("未获取到用户ID");
        }
    }
    @PostMapping("/getmessagelist")
    public Result getMessagelist(HttpServletRequest request){
        String userId = (String) request.getAttribute("uid");
        if (userId != null) {
            return aiService.getMessageList(Integer.parseInt(userId));
        } else {
            return Result.LogicError("未获取到用户ID");
        }
    }

}
