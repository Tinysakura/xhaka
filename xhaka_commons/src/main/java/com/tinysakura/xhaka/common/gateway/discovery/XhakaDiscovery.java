package com.tinysakura.xhaka.common.gateway.discovery;

import com.tinysakura.xhaka.common.gateway.discovery.constant.XhakaDiscoveryConstant;
import com.tinysakura.xhaka.common.gateway.discovery.util.ZookeeperUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/31
 */
@Slf4j
public class XhakaDiscovery {

    public static void registerXhaka(String serverName, String host, Integer port) {
        try {
            String slaveInstanceChildPath =host + ":" + port;
            String zkPath = XhakaDiscoveryConstant.REGISTER_SERVER_PARENT_PATH + "/" + serverName + "_" + slaveInstanceChildPath;

            CuratorFramework zkClient = ZookeeperUtils.getZooKeeper();

            Stat stat = zkClient.checkExists().forPath(zkPath);

            if (stat == null) {
                zkClient.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(zkPath);
            }

            log.info("server:{} register success, ip:{}", serverName, host + ":" + port);
        } catch (Exception e) {
            log.error("server:{} register failed, ip:{}", serverName, host + ":" + port);
        }

    }
}