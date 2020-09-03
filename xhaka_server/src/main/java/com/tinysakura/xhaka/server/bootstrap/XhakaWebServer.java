package com.tinysakura.xhaka.server.bootstrap;

import com.tinysakura.xhaka.common.handler.FullHttpRequest2HttpServletHandler;
import com.tinysakura.xhaka.server.handler.XhakaHttpServletHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.DefaultEventLoopGroup;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;

import java.net.InetSocketAddress;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/20
 */
@Slf4j
public class XhakaWebServer implements WebServer {
    Integer port;

    Integer businessThreadCount;

    String contextPath;

    private EventLoopGroup bossGroup;

    private EventLoopGroup workerGroup;

    private DefaultEventLoopGroup businessGroup;

    public XhakaWebServer(Integer port, Integer businessThreadCount, String contextPath) {
        this.port = port;
        this.businessThreadCount = businessThreadCount;
        this.contextPath = contextPath;
    }

    @Override
    public void start() throws WebServerException {
        InetSocketAddress inetSocketAddress = new InetSocketAddress(port);

        ServerBootstrap serverBootstrap = new ServerBootstrap();
        bossGroup = new NioEventLoopGroup(1);
        // 默认为cpu核数 * 2
        workerGroup = new NioEventLoopGroup();
        businessGroup = new DefaultEventLoopGroup(businessThreadCount);

        ChannelFuture future = serverBootstrap.channel(NioServerSocketChannel.class)
                .group(bossGroup, workerGroup)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new HttpServerCodec())
                                .addLast(new HttpObjectAggregator(100 * 1024))
                                .addLast(new ChunkedWriteHandler())
                                .addLast(new FullHttpRequest2HttpServletHandler())
                                .addLast(businessGroup, new XhakaHttpServletHandler());
                    }
                }).bind(inetSocketAddress).awaitUninterruptibly();

        if (future.cause() != null) {
            log.error("start netty web server failed", future.cause());
        }

        log.info("start netty web server on port:{}", port);
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
            log.error("netty web server stop failed", e);
        }
    }

    @Override
    public int getPort() {
        return this.port;
    }
}