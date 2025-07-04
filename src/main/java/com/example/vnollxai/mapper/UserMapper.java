package com.example.vnollxai.mapper;

import com.example.vnollxai.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {
    @Insert("INSERT INTO user(name, password,email) VALUES(#{name}, #{password},#{email})")
    void addUser(String name,String password,String email);

    @Select("SELECT * from user WHERE email=#{email}")
    User getUserByEmail(String email);

    @Select("SELECT * from user WHERE id=#{id}")
    User getUserById(int id);

    @Update("UPDATE user SET email =#{email} Where id=#{id}")
    void updateEmail(int id,String email);

    @Update("UPDATE user SET password =#{password} Where email=#{email}")
    void updatePassword(String email,String password);
}
