package com.tinysakura.xhaka.server.conf;

import com.tinysakura.xhaka.server.bootstrap.XhakaServerBootstrap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/31
 */
@Configuration
public class XhakaSeverConfiguration {

    @ConditionalOnProperty(name = "xhaka.server.open", havingValue = "true")
    @Bean
    public XhakaServerBootstrap xhakaServerBootstrap() {
        return new XhakaServerBootstrap();
    }

}