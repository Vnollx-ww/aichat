package com.example.vnollxai.utils;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.SimpleEmail;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SendEmail {

    public static Boolean sendEmail(String targetEmail, String authCode,String option) {
        try {
            SimpleEmail mail = new SimpleEmail();

            // 1. 先设置主机和端口（新版必须先设置这些）
            mail.setHostName("smtp.qq.com");
            mail.setSmtpPort(465);

            // 2. 设置认证信息
            mail.setAuthentication("2720741614@qq.com", "vxbicqslkdwbddba");

            // 3. 配置SSL/TLS（关键修改点）
            mail.setSSLOnConnect(true);
            mail.setSSLCheckServerIdentity(true); // 验证服务器身份

            // 4. 通过系统属性设置协议（必须在创建Email对象前设置）
            System.setProperty("mail.smtp.ssl.protocols", "TLSv1.2");
            System.setProperty("mail.smtp.ssl.ciphersuites", "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256");

            // 5. 设置邮件内容
            mail.setFrom("2720741614@qq.com", "vnollx");
            mail.addTo(targetEmail);
            if (Objects.equals(option, "register")) mail.setSubject("注册验证码");
            else if(Objects.equals(option, "forget"))mail.setSubject("修改密码验证码");
            mail.setMsg("您的验证码为:" + authCode + "(一分钟内有效)");

            // 发送邮件
            mail.send();
            return true;
        } catch (EmailException e) {
            System.err.println("邮件发送失败: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}