package com.tinysakura.xhaka.server.bootstrap;

import com.tinysakura.xhaka.common.context.XhakaWebServerContext;
import com.tinysakura.xhaka.common.gateway.discovery.constant.XhakaDiscoveryConstant;
import com.tinysakura.xhaka.common.gateway.discovery.util.ZookeeperUtils;
import com.tinysakura.xhaka.server.base.XhakaGatewayClientThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.springframework.beans.BeansException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.CollectionUtils;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

/**
 * 实现ApplicationRunner 在spring-boot工程启动完成后执行以下操作：
 * 1. 开启网关侧的netty server，接收外部流量
 * 2. 读取zk中已经注册的代理服务，连接代理服务
 * 3. 监听zk对应节点的变化（代理服务有新节点注册上来），向这些节点发起新连接
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/31
 */
@Slf4j
public class XhakaServerBootstrap implements ApplicationRunner, ApplicationContextAware {

    private ApplicationContext context;

    @Override
    public void run(ApplicationArguments args) {
        try {
            Integer port = Integer.valueOf(Objects.requireNonNull(context.getEnvironment().getProperty("server.port")));
            String contextPath = context.getEnvironment().getProperty("server.servlet.context-path");
            String businessThreadCountProperty = context.getEnvironment().getProperty("xhaka.gateway.business.thread.count");
            Integer businessThreadCount = 500;
            if (StringUtils.isNotEmpty(businessThreadCountProperty)) {
                businessThreadCount = Integer.valueOf(businessThreadCountProperty);
            }

            if (StringUtils.isEmpty(contextPath)) {
                contextPath = "/";
            }

            // 1. start gateway netty server
            new XhakaWebServerContext(contextPath, port);
            new XhakaWebServer(port, businessThreadCount, contextPath).start();

            // 2. connect register slave
            CuratorFramework zkClient = ZookeeperUtils.getZooKeeper();
            List<String> childPaths = zkClient.getChildren().forPath(XhakaDiscoveryConstant.REGISTER_SERVER_PARENT_PATH);
            if (!CollectionUtils.isEmpty(childPaths)) {
                for (String childPath : childPaths) {
                    String tmp = XhakaDiscoveryConstant.REGISTER_SERVER_PARENT_PATH + childPath;
                    byte[] content = zkClient.getData().forPath(tmp);

                    handleChildPath(tmp, content);
                }
            }

            // watch zk path
            PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, XhakaDiscoveryConstant.REGISTER_SERVER_PARENT_PATH, false);
            pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
                    switch (event.getType()) {
                        case CHILD_ADDED:
                            handleChildPath(event.getData().getPath(), event.getData().getData());
                            return;
                        case CHILD_UPDATED:
                            handleChildPath(event.getData().getPath(), event.getData().getData());
                            return;
                        default:
                    }
                }
            });
        } catch (Exception e) {
            log.error("XhakaServerBootstrap run occur error", e);
        }
    }

    private void handleChildPath(String childPath, byte[] content) {
        String pathContent = new String(content, Charset.forName("UTF-8"));
        String[] ipList = pathContent.split(XhakaDiscoveryConstant.REGISTER_SERVER_INSTANCE_IP_SEPARATOR);
        for (String ip : ipList) {
            String[] split = ip.split(":");
            if (split.length != 2) {
                log.error("invalid ip : {}", ip);
                continue;
            }

            XhakaGatewayClientThreadPool.submit(new XhakaGatewayClientThreadPool.XhakaGatewayClientRunnable(childPath, split[0], Integer.valueOf(split[1])));
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}