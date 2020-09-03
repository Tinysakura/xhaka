package com.tinysakura.xhaka.client.slave.conf;

import com.tinysakura.xhaka.client.slave.bootstrap.XhakaSlaveBootstrap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/31
 */
@Configuration
public class XhakaSlaveConfiguration {

    @Bean
    @ConditionalOnProperty(name = "xhaka.slave.open", havingValue = "true")
    public XhakaSlaveBootstrap xhakaSlaveBootstrap() {
        return new XhakaSlaveBootstrap();
    }


}