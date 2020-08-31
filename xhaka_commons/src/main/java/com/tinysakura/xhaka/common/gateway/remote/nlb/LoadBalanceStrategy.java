package com.tinysakura.xhaka.common.gateway.remote.nlb;

import io.netty.channel.Channel;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/31
 */
public interface LoadBalanceStrategy {
    String getType();

    /**
     * 需要实现不同的负载均衡算法从被代理的服务集群中选出一个实例响应请求的接口
     * @param serverName
     * @return
     */
     Channel selectChannel(String serverName);
}
