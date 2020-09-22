package com.tinysakura.xhaka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportResource;

@SpringBootApplication(scanBasePackages = "com.tinysakura.xhaka")
@ImportResource(locations = {"classpath:spring-mbean.xml"})
public class XhakaApplication {

    public static void main(String[] args) {
        SpringApplication.run(XhakaApplication.class, args);
    }

}
