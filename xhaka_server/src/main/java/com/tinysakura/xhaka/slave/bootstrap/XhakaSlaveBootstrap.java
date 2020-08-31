package com.tinysakura.xhaka.slave.bootstrap;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/31
 */

public class XhakaSlaveBootstrap implements ApplicationRunner, ApplicationContextAware {
    private ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        String serverName = applicationContext.getEnvironment().getProperty("app_name");
        if (StringUtils.isEmpty(serverName)) {
            serverName = applicationContext.getEnvironment().getProperty("spring.application.name");
        }

        new XhakaSlaveServer(serverName).start();
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}