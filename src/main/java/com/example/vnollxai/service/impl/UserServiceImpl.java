package com.example.vnollxai.service.impl;

import com.example.vnollxai.entity.User;
import com.example.vnollxai.mapper.UserMapper;
import com.example.vnollxai.service.UserService;
import com.example.vnollxai.utils.Jwt;
import com.example.vnollxai.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Objects;
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private JedisPool jedisPool;
    @Override
    public Result login(String email, String password) {
        User user=userMapper.getUserByEmail(email);
        if(user==null){
            return Result.LogicError("用户不存在");
        }
        if(!Objects.equals(password, user.getPassword())){
            return Result.LogicError("密码不正确");
        }
        String token= Jwt.generateToken(String.valueOf(user.getId()));
        return Result.Success(token,"登录成功");
    }

    @Override
    public Result register(String name, String email, String password,String captcha) {
        User user=userMapper.getUserByEmail(email);
        if(user!=null){
            return Result.LogicError("用户已存在");
        }
        Jedis jedis=jedisPool.getResource();
        String key="register"+"-email:"+email;
        if (jedis.ttl(key)<=0){
            return Result.LogicError("验证码已过期");
        }
        if(!Objects.equals(jedis.get(key), captcha)){
            return Result.LogicError("验证码错误");
        }
        jedis.del(key);
        userMapper.addUser(name,password,email);
        return Result.Success("注册成功");
    }

    @Override
    public Result updatePassword(String email,String newPassword,String captcha) {
        User user=userMapper.getUserByEmail(email);
        if (user==null){
            return Result.LogicError("用户不存在");
        }
        Jedis jedis=jedisPool.getResource();
        String key="forget"+"-email:"+email;
        if (jedis.ttl(key)<=0){
            return Result.LogicError("验证码已过期");
        }
        if(!Objects.equals(jedis.get(key), captcha)){
            return Result.LogicError("验证码错误");
        }
        jedis.del(key);
        userMapper.updatePassword(email,newPassword);
        return Result.Success("修改密码成功");
    }

    @Override
    public Result updateEmail(int uid,String oldEmail, String newEmail) {
        User user=userMapper.getUserById(uid);
        if (user==null){
            return Result.LogicError("用户不存在");
        }
        User u=userMapper.getUserByEmail(newEmail);
        if(u!=null){
            return Result.LogicError("邮箱已存在");
        }
        userMapper.updateEmail(uid,newEmail);
        return Result.Success("修改邮箱成功");
    }
}
