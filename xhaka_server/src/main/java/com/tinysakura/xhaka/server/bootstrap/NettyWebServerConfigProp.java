package com.tinysakura.xhaka.server.bootstrap;

import org.springframework.beans.factory.annotation.Value;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/20
 */

public class NettyWebServerConfigProp {
    @Value("${business_thread_count}")
    private Integer businessThreadCount;


    public Integer getBusinessThreadCount() {
        return businessThreadCount;
    }
}