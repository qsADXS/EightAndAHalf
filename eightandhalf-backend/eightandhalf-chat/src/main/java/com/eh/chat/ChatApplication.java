package com.eh.chat;


import com.eh.api.config.DefaultFeignConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan(basePackages =  {"com.eh.chat", "com.eh.common"})
@EnableFeignClients(basePackages = "com.eh.api.client",defaultConfiguration = DefaultFeignConfig.class)
@SpringBootApplication
public class ChatApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }


}
