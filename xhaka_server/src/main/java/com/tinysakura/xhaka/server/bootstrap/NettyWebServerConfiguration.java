package com.tinysakura.xhaka.server.bootstrap;

import io.netty.bootstrap.Bootstrap;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/20
 */
//@Configuration
@ConditionalOnWebApplication
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class NettyWebServerConfiguration {

    @Bean
    @ConditionalOnClass({Bootstrap.class})
    @ConditionalOnMissingBean(search = SearchStrategy.CURRENT, value = NettyWebServerFactory.class)
    public NettyWebServerFactory nettyWebServer() {
        return new NettyWebServerFactory();
    }

    @Bean
    @ConditionalOnClass({Bootstrap.class})
    public NettyWebServerConfigProp nettyWebServerConfigProp() {
        return new NettyWebServerConfigProp();
    }

}