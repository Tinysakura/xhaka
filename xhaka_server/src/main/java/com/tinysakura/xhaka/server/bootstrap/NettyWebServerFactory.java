package com.tinysakura.xhaka.server.bootstrap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/20
 */

public class NettyWebServerFactory extends AbstractServletWebServerFactory {

    @Autowired
    private NettyWebServerConfigProp nettyWebServerConfigProp;

    @Override
    public WebServer getWebServer(ServletContextInitializer... initializers) {
        //return new XhakaWebServer(getPort(), nettyWebServerConfigProp.getBusinessThreadCount());
        return null;
    }
}