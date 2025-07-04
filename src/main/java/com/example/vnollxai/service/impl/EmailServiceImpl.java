package com.example.vnollxai.service.impl;

import com.example.vnollxai.entity.User;
import com.example.vnollxai.mapper.UserMapper;
import com.example.vnollxai.service.EmailService;
import com.example.vnollxai.utils.Result;
import com.example.vnollxai.utils.SendEmail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Objects;
import java.util.Random;
@Service
public class EmailServiceImpl implements EmailService {
    @Autowired
    private JedisPool jedisPool;
    @Autowired
    private UserMapper userMapper;
    @Override
    public Result send(String email,String option) {
        User u=userMapper.getUserByEmail(email);
        if (u!=null&& Objects.equals(option, "register")){
            return Result.LogicError("该邮箱地址已存在，无法注册");
        }
        if(u==null&& Objects.equals(option, "forget")){
            return Result.LogicError("该邮箱地址并未注册");
        }
        String authCode = String.valueOf(new Random().nextInt(899999) + 100000);
        boolean ok= SendEmail.sendEmail(email,authCode,option);
        if (ok){
            Jedis jedis=jedisPool.getResource();
            String key=option+"-email:"+ email;
            if(jedis.ttl(key)>0){
                return Result.LogicError("请勿重复点击发送验证码");
            }
            jedis.setex(key,60,authCode);
            return Result.Success("发送验证码成功，有效期一分钟");
        }else{
            return Result.SystemError("发送验证码失败");
        }
    }
}
