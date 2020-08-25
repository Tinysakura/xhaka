package com.tinysakura.xhaka;

import com.tinysakura.xhaka.common.context.XhakaWebServerContext;
import com.tinysakura.xhaka.core.webserver.XhakaWebServer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Objects;

@SpringBootApplication(scanBasePackages = "com.tinysakura.xhaka")
public class XhakaApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(XhakaApplication.class, args);

        Integer port = Integer.valueOf(Objects.requireNonNull(context.getEnvironment().getProperty("server.port")));
        String contextPath = context.getEnvironment().getProperty("server.servlet.context-path");
        if (StringUtils.isEmpty(contextPath)) {
            contextPath = "/";
        }

        new XhakaWebServerContext(contextPath, port);
        new XhakaWebServer(port, 500, contextPath).start();
    }

}
