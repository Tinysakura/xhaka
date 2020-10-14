package com.tinysakura.xhaka.client.slave.bootstrap;

import com.tinysakura.xhaka.client.filter.FilterAsyncDispatcherSupportEnhance;
import com.tinysakura.xhaka.common.context.XhakaWebServerContext;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import org.apache.catalina.core.ApplicationFilterRegistration;
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
        enhanceApplicationFilterRegistration();

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

    private void enhanceApplicationFilterRegistration() {
        new ByteBuddy().subclass(ApplicationFilterRegistration.class)
                .method(ElementMatchers.named("addMappingForServletNames"))
                .intercept(MethodDelegation.to(FilterAsyncDispatcherSupportEnhance.class))
                .make()
                .load(ClassLoader.getSystemClassLoader());
    }
}