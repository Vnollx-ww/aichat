package com.example.vnollxai.controller;

import com.example.vnollxai.service.UserService;
import com.example.vnollxai.utils.Result;
import javax.servlet.http.HttpServletRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/login")
    public Result login(@RequestParam String email,@RequestParam String password){
        return userService.login(email,password);
    }
    @PostMapping("/register")
    public Result register(@RequestParam String name,@RequestParam String email,@RequestParam String password,@RequestParam String captcha){
        return userService.register(name,email,password,captcha);
    }
    @PostMapping("/update/password")
    public Result updatePassword(@RequestParam String email,@RequestParam String newPassword,@RequestParam String captcha){
        return userService.updatePassword(email,newPassword,captcha);
    }
    @PostMapping("/update/email")
    public Result updateEmail(HttpServletRequest request,@RequestParam String oldEmail,@RequestParam String newEmail){
        String userId = (String) request.getAttribute("uid");
        if (userId != null) {
            return userService.updateEmail(Integer.parseInt(userId),oldEmail,newEmail);
        } else {
            return Result.LogicError("未获取到用户ID");
        }
    }
}
