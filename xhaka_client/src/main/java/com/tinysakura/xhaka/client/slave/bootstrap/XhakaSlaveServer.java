package com.tinysakura.xhaka.client.slave.bootstrap;

import com.tinysakura.xhaka.common.gateway.config.XhakaGateWayConfig;
import com.tinysakura.xhaka.common.gateway.discovery.XhakaDiscovery;
import com.tinysakura.xhaka.client.handler.SlaveXhakaHttpServletHandler;
import com.tinysakura.xhaka.common.gateway.handler.XhakaProtocolHandler;
import com.tinysakura.xhaka.common.gateway.handler.codec.FullHttpResponse2XhakaEncoder;
import com.tinysakura.xhaka.common.gateway.handler.codec.Xhaka2FulHttpRequestDecoder;
import com.tinysakura.xhaka.common.gateway.handler.codec.XhakaDecoder;
import com.tinysakura.xhaka.common.gateway.handler.codec.XhakaEncoder;
import com.tinysakura.xhaka.common.handler.FullHttpRequest2HttpServletHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * 被网关代理的服务在本地启动的nettyServer
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/26
 */
@Slf4j
public class XhakaSlaveServer implements WebServer {

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private DefaultEventLoopGroup businessGroup;

    private String serverName;

    public XhakaSlaveServer(String serverName) {
        this.serverName = serverName;
    }

    @Override
    public void start() throws WebServerException {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(XhakaGateWayConfig.getInstance().getSlaveNettyServerPort());

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1);
        // 默认为cpu核数 * 2
        workerGroup = new NioEventLoopGroup();
        businessGroup = new DefaultEventLoopGroup(XhakaGateWayConfig.getInstance().getSlaveBusinessThreadCount());

        ChannelFuture future = serverBootstrap.channel(NioServerSocketChannel.class)
                .group(bossGroup, workerGroup)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new XhakaDecoder(XhakaGateWayConfig.getInstance().getXhakaMsgMaxLength(), 0, 4))
                                //处理xhaka协议
                                .addLast(new XhakaProtocolHandler())
                                //处理xhaka协议转换的http request
                                .addLast(new Xhaka2FulHttpRequestDecoder())
                                //将FullHttpRequest转换为适配Servlet容器的xhakaHttpServletRequest
                                .addLast(new FullHttpRequest2HttpServletHandler())
                                //代理服务处理servlet request
                                .addLast(businessGroup, new SlaveXhakaHttpServletHandler())
                                //将响应转换为xhaka协议发送给调用网关
                                .addLast(new FullHttpResponse2XhakaEncoder())
                                .addLast(new XhakaEncoder());
                    }
                }).bind(inetSocketAddress).awaitUninterruptibly();

        if (future.cause() != null) {
            log.error("start xhaka slave server failed", future.cause());
            return;
        }

        // 开启netty server端口后，将自己注册到zk，xhaka网关会监听到zk节点的变化发起与该代理服务的连接
        InetSocketAddress localAddress = (InetSocketAddress) future.channel().localAddress();

        try {
            InetAddress addr = InetAddress.getLocalHost();
            XhakaDiscovery.registerXhaka(serverName, addr.getHostAddress(), localAddress.getPort());
        } catch (UnknownHostException e) {
            log.error("obtain localAddress failed", e);
        }

        log.info("start xhaka slave server on port:{}", getPort());
    }

    @Override
    public void stop() throws WebServerException {
        try {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully().await();
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully().await();
            }
            if (businessGroup != null) {
                businessGroup.shutdownGracefully().await();
            }
        } catch (InterruptedException e) {
            log.error("xhaka slave server stop failed", e);
        }
    }

    @Override
    public int getPort() {
        return XhakaGateWayConfig.getInstance().getSlaveNettyServerPort();
    }
}