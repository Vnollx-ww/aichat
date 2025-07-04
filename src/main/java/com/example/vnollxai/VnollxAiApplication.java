package com.example.vnollxai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ServletComponentScan
@EnableScheduling
@MapperScan("com.example.vnollxai.mapper")
public class VnollxAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(VnollxAiApplication.class, args);
    }

}
