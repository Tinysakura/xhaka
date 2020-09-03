package com.tinysakura.xhaka.client.slave.conf;

import com.tinysakura.xhaka.client.context.TomcatServletContext;
import org.apache.catalina.core.StandardContext;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/28
 */
@Configuration
public class SlaveServletContextConfiguration {

    @ConditionalOnClass(value = {StandardContext.class})
    @Bean
    public TomcatServletContext tomcatServletContext() {
        return new TomcatServletContext();
    }
}