package com.tinysakura.xhaka.client.slave.bootstrap;

import com.tinysakura.xhaka.common.context.XhakaWebServerContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Objects;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/31
 */
public class XhakaSlaveBootstrap implements ApplicationRunner, ApplicationContextAware {
    private ApplicationContext context;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String serverName = context.getEnvironment().getProperty("app_name");
        if (StringUtils.isEmpty(serverName)) {
            serverName = context.getEnvironment().getProperty("spring.application.name");
        }

        Integer port = Integer.valueOf(Objects.requireNonNull(context.getEnvironment().getProperty("server.port")));
        String contextPath = context.getEnvironment().getProperty("server.servlet.context-path");
        if (StringUtils.isEmpty(contextPath)) {
            contextPath = "/";
        }

        new XhakaWebServerContext(contextPath, port);

        final String finalServerName = serverName;
        new Thread(() -> {
            new XhakaSlaveServer(finalServerName).start();
        }, "thread-xhaka-slave-main").start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}