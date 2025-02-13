package com.eh.common.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.URLDataSource;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import jakarta.mail.util.ByteArrayDataSource;
import lombok.extern.slf4j.Slf4j;
@Slf4j
public class EmailUtil {
    private static final String HOST = "smtp.126.com";
    private static final String PORT = "465";
    private static final String USERNAME = "email";
    private static final String PASSWORD = "code"; // 授权码

    //指定邮箱，主题和内容
    public static void sendEmail(String toEmail, String subject, String content) {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", HOST);
        props.put("mail.smtp.port", PORT);

        // 配置SSL
        props.put("mail.smtp.socketFactory.port", PORT);
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(props,
                new jakarta.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(USERNAME, PASSWORD);
                    }
                });

        try {

            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(USERNAME));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
//            message.setText(content);


            // 创建多部分内容
            MimeMultipart multipart = new MimeMultipart("related");

            // 创建HTML部分
            MimeBodyPart htmlPart = new MimeBodyPart();
            htmlPart.setContent(content, "text/html; charset=utf-8");
            multipart.addBodyPart(htmlPart);

            // 创建内嵌图片部分
            MimeBodyPart imagePart = new MimeBodyPart();
            String imageUrl = "https://s2.loli.net/2024/11/04/4cPnKmjDMRArCpu.jpg";
            URI uri = new URI(imageUrl);
            URL url = uri.toURL();
            DataSource dataSource = new URLDataSource(url);
            imagePart.setDataHandler(new DataHandler(dataSource));
            imagePart.setContentID("logo");
            imagePart.setDisposition(MimeBodyPart.INLINE);
            multipart.addBodyPart(imagePart);

            // 设置邮件内容为多部分内容
            message.setContent(multipart);


//            message.setContent(content, "text/html;charset=UTF-8");
            Transport.send(message);

            log.info("Email sent successfully to {}",toEmail);

        } catch (MessagingException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
    public static void sendCode(String toEmail,String code){
        sendEmail(toEmail,"八部半登录验证码","<!DOCTYPE html>\n" +
                "<html lang=\"zh\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>验证码展示</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: 'Arial', sans-serif;\n" +
                "            background-color: #f0f8ff;\n" +
                "            display: flex;\n" +
                "            justify-content: center;\n" +
                "            align-items: center;\n" +
                "            height: 100vh;\n" +
                "            margin: 0;\n" +
                "            color: #333;\n" +
                "        }\n" +
                "\n" +
                "        .container {\n" +
                "            text-align: center;\n" +
                "            background: white;\n" +
                "            padding: 30px;\n" +
                "            border-radius: 10px;\n" +
                "            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);\n" +
                "            width: 300px;\n" +
                "        }\n" +
                "\n" +
                "        h1 {\n" +
                "            font-size: 24px;\n" +
                "            margin-bottom: 20px;\n" +
                "            color: #007BFF;\n" +
                "        }\n" +
                "\n" +
                "        .message {\n" +
                "            font-size: 16px;\n" +
                "            margin-bottom: 20px;\n" +
                "            line-height: 1.5;\n" +
                "        }\n" +
                "\n" +
                "        .captcha {\n" +
                "            font-size: 20px;\n" +
                "            font-weight: bold;\n" +
                "            margin: 20px 0;\n" +
                "            padding: 10px;\n" +
                "            border: 2px solid #007BFF;\n" +
                "            border-radius: 5px;\n" +
                "            display: inline-block;\n" +
                "            background-color: #e9f7ff;\n" +
                "            color: #007BFF;\n" +
                "        }\n" +
                "\n" +
                "        .image {\n" +
                "            width: 64px;\n" +
                "            height: 64px;\n" +
                "            margin-bottom: 10px;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <img src=\"cid:logo\" alt=\"验证码图标\" class=\"image\">\n" +
                "        <h1>验证码通知</h1>\n" +
                "        <div class=\"message\">\n" +
                "            尊敬的【八部半】用户您好，您的验证码为：\n" +
                "        </div>\n" +
                "        <div class=\"captcha\" id=\"captcha-code\"> " + code +"</div>\n" +
                "        <div class=\"message\">\n" +
                "            请在5分钟内完成验证。\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>\n");
    }

    public static void main(String[] args) {
        sendCode("zhuangshaokun@126.com","v7iX>0H#");
    }

}
