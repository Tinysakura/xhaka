package com.tinysakura.xhaka.common.gateway.remote.core;

import com.tinysakura.xhaka.common.gateway.remote.nlb.LoadBalanceStrategy;
import com.tinysakura.xhaka.common.gateway.remote.nlb.constant.LoadBalanceStrategyConstant;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 网关维护的和被代理服务的连接池
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/28
 */
@Component
@Slf4j
public class GatewaySlaveChannelPool implements ApplicationContextAware, InitializingBean {
    private static GatewaySlaveChannelPool instance;

    /**
     * 绝大多数情况下是读场景使用读写锁提高并发性能
     */
    private ReentrantReadWriteLock reentrantReadWriteLock;

    private Map<String, List<Channel>> slaveChannelPoolMap;

    private ApplicationContext applicationContext;

    private Map<String, LoadBalanceStrategy> loadBalanceStrategyMap = new HashMap<>();

    private GatewaySlaveChannelPool() {
        this.slaveChannelPoolMap = new HashMap<>();
        reentrantReadWriteLock = new ReentrantReadWriteLock();
    }

    public static GatewaySlaveChannelPool getInstance() {
        if (instance == null) {
            synchronized (GatewaySlaveChannelPool.class) {
                if (instance == null) {
                    instance = new GatewaySlaveChannelPool();
                }
            }
        }

        return instance;
    }

    public void addSlaveChannelIntoPool(String serverName, Channel channel) {
        reentrantReadWriteLock.writeLock().lock();

        try {
            List<Channel> channels = this.slaveChannelPoolMap.get(serverName);
            if (channels == null) {
                channels = new ArrayList<>();
            }

            channels.add(channel);

            this.slaveChannelPoolMap.put(serverName, channels);
        } catch (Exception e) {
            log.error("add slave channel into pool occur exception", e);
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    public Channel getSlaveChannelByLoadBalanceStrategy(String strategyName, String serverName) {
        LoadBalanceStrategy loadBalanceStrategy = this.loadBalanceStrategyMap.get(strategyName);
        if (loadBalanceStrategy == null) {
            loadBalanceStrategy = this.loadBalanceStrategyMap.get(LoadBalanceStrategyConstant.LOAD_BALANCE_STRATEGY_RANDOM);
        }

        if (loadBalanceStrategy == null) {
            return null;
        }

        this.reentrantReadWriteLock.readLock().lock();

        try {
            return loadBalanceStrategy.selectChannel(serverName);
        } catch (Exception e) {
            log.error("get slave channel from pool occur exception", e);
        } finally {
            this.reentrantReadWriteLock.readLock().unlock();
        }

        return null;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        Map<String, LoadBalanceStrategy> loadBalanceStrategyMap = this.applicationContext.getBeansOfType(LoadBalanceStrategy.class);

        for (LoadBalanceStrategy loadBalanceStrategy : loadBalanceStrategyMap.values()) {
            this.loadBalanceStrategyMap.putIfAbsent(loadBalanceStrategy.getType(), loadBalanceStrategy);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}