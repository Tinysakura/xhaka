package com.tinysakura.xhaka.client.bootstrap;

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
 * @Date: 2020/9/4
 */

public class XhakaSlaveBootStrap implements ApplicationRunner, ApplicationContextAware {

    private ApplicationContext context;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Integer port = Integer.valueOf(Objects.requireNonNull(context.getEnvironment().getProperty("server.port")));
        String contextPath = context.getEnvironment().getProperty("server.servlet.context-path");
        if (StringUtils.isEmpty(contextPath)) {
            contextPath = "/";
        }

        new XhakaWebServerContext(contextPath, port);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}