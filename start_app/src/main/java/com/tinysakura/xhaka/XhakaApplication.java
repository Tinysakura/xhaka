package com.tinysakura.xhaka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication(scanBasePackages = "com.tinysakura.xhaka")
public class XhakaApplication {

    public static void main(String[] args) {
        SpringApplication.run(XhakaApplication.class, args);
    }

}
