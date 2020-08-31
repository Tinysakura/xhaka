package com.tinysakura.xhaka.common.gateway.discovery.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/31
 */
@Slf4j
@Component
public class ZookeeperUtils implements BeanFactoryAware {

    private static ZookeeperUtils   S_INSTANCE        = null;

    private static CuratorFramework S_ZOOKEEPER       = null;

    @Value("${xhaka.gateway.discovery.zk.connectStr}")
    private String                  connectStr;
    private int                     sessionTimeout    = 60000;
    private int                     baseSleepTimeMs   = 60000;
    private int                     maxTryCount       = 3;
    private int                     connectionTimeout = 15000;

    public static CuratorFramework getZooKeeper() {
        if (S_ZOOKEEPER != null) {
            return S_ZOOKEEPER;
        }

        synchronized (ZookeeperUtils.class) {
            if (S_ZOOKEEPER != null) {
                return S_ZOOKEEPER;
            }

            assert S_INSTANCE.connectStr != null;

            CuratorFramework client = CuratorFrameworkFactory.builder().connectString(S_INSTANCE.connectStr).sessionTimeoutMs(S_INSTANCE.sessionTimeout).connectionTimeoutMs(S_INSTANCE.connectionTimeout).retryPolicy(new ExponentialBackoffRetry(
                    S_INSTANCE.baseSleepTimeMs,
                    S_INSTANCE.maxTryCount)).build();

            if (client != null) {
                client.start();
                S_ZOOKEEPER = client;
            } else {
                throw new RuntimeException("zookeeper create fail.connectUrl:" + S_INSTANCE.connectStr);
            }

            log.info("zookeeper create successfully .connectUrl:" + S_INSTANCE.connectStr);
            return S_ZOOKEEPER;

        }

    }

    @Override
    public void setBeanFactory(BeanFactory arg0) throws BeansException {
        S_INSTANCE = arg0.getBean("zookeeperUtils", ZookeeperUtils.class);
    }

    public String getConnectStr() {
        return connectStr;
    }

    public void setConnectStr(String connectStr) {
        this.connectStr = connectStr;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public int getMaxTryCount() {
        return maxTryCount;
    }

    public void setMaxTryCount(int maxTryCount) {
        this.maxTryCount = maxTryCount;
    }

    public static void resetZookeeperInstance() {

    }

    public int getBaseSleepTimeMs() {
        return baseSleepTimeMs;
    }

    public void setBaseSleepTimeMs(int baseSleepTimeMs) {
        this.baseSleepTimeMs = baseSleepTimeMs;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

}