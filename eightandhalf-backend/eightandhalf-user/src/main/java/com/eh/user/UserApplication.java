package com.eh.user;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import springfox.documentation.spring.web.plugins.DefaultConfiguration;

@ComponentScan(basePackages =  {"com.eh.user", "com.eh.common"})
@SpringBootApplication

public class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

}
