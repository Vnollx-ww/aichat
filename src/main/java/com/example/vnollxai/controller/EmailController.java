package com.example.vnollxai.controller;

import com.example.vnollxai.service.EmailService;
import com.example.vnollxai.utils.Result;
import com.example.vnollxai.utils.SendEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/email")
public class EmailController {
    @Autowired
    private EmailService emailService;
    @PostMapping("/send")
    public Result send(@RequestParam String email, @RequestParam String option){
        return emailService.send(email,option);
    }
}
