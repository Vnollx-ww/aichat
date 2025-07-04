package com.example.vnollxai.mapper;

import com.example.vnollxai.entity.Message;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AiMapper {

    @Insert("INSERT INTO message(uid,thinking,create_time,response,question) VALUES(#{uid}, #{thinking},#{time},#{response},#{question})")
    void addMessage(String time,String question,String thinking,String response,int uid);

    @Select("SELECT * FROM message WHERE uid=#{uid}")
    List<Message> getMessageList(int uid);

    @Delete("DELETE from message WHERE uid = #{uid} LIMIT 1")
    void deleteMessage(int uid);

    @Select("SELECT COUNT(*) FROM message WHERE uid=#{uid}")
    int getMessageCount(int uid);
}
