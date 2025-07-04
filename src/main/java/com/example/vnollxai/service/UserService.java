package com.example.vnollxai.service;

import com.example.vnollxai.utils.Result;
import org.springframework.stereotype.Service;

public interface UserService {
    Result login(String email,String password);
    Result register(String name,String email,String password,String captcha);

    Result updatePassword(String email,String newPassword,String captcha);
    Result updateEmail(int uid,String oldEmail,String newEmail);
}
