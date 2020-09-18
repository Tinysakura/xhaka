package com.tinysakura.xhaka.common.gateway.context.client;

import com.tinysakura.xhaka.common.gateway.config.XhakaGateWayConfig;
import com.tinysakura.xhaka.common.gateway.handler.SlaveXhakaHttpServletResponsehHandler;
import com.tinysakura.xhaka.common.gateway.handler.XhakaProtocolHandler;
import com.tinysakura.xhaka.common.gateway.handler.codec.FullHttpRequest2XhakaEncoder;
import com.tinysakura.xhaka.common.gateway.handler.codec.Xhaka2FullHttpResponseDecoder;
import com.tinysakura.xhaka.common.gateway.handler.codec.XhakaDecoder;
import com.tinysakura.xhaka.common.gateway.handler.codec.XhakaEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

/**
 * 网关向代理端发起连接client
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/31
 */
@Slf4j
public class XhakaGatewayClient {
    private EventLoopGroup workerGroup;

    private String serverName;

    private String remoteHost;

    private Integer remotePort;

    public XhakaGatewayClient(String serverName, String remoteHost, Integer remotePort) {
        this.serverName = serverName;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    public void connect() {
        workerGroup = new NioEventLoopGroup();

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline()
                                .addLast(new XhakaDecoder(XhakaGateWayConfig.getInstance().getXhakaMsgMaxLength(), 0, 4))
                                //处理xhaka协议
                                .addLast(new XhakaProtocolHandler())
                                //处理xhaka协议转换的http response
                                .addLast(new Xhaka2FullHttpResponseDecoder())
                                //处理代理服务发送的响应
                                .addLast(new SlaveXhakaHttpServletResponsehHandler())
                                //将请求转换为xhaka协议发送给代理服务
                                .addLast(new FullHttpRequest2XhakaEncoder())
                                .addLast(new XhakaEncoder());
                    }
                });

        ChannelFuture f = bootstrap.connect(new InetSocketAddress(remoteHost, remotePort));
        try {
            if (f.cause() == null) {
                GatewaySlaveChannelPool.getInstance().addSlaveChannelIntoPool(serverName, remoteHost, remotePort, f.channel());
                HeartbeatPacemaker.getInstance().pacemaker(serverName, remoteHost + ":" + remotePort, null);
                log.info("XhakaGatewayClient connect success, remoteHost:{}, remotePort:{}", remoteHost, remotePort);

                f.channel().closeFuture().addListener(future -> {
                    log.debug("连接关闭:{}", remoteHost + ":" + remotePort);
                    GatewaySlaveChannelPool.getInstance().removeSlaveChannelFromPool(serverName, remoteHost + ":" + remotePort);
                });
                f.channel().closeFuture().sync();
            } else {
                log.error("连接{}:{}失败", serverName, remoteHost + remotePort);
            }
        } catch (InterruptedException e) {
            log.error("XhakaGatewayClient remoteHost:{}, remotePort:{} occur error", remoteHost, remotePort, e);
        }
    }

    public void disconnect() {
        try {
            if (workerGroup != null) {
                workerGroup.shutdownGracefully().await();
            }
        } catch (InterruptedException e) {
            log.error("xhaka gateway client disconnect failed", e);
        }
    }
}