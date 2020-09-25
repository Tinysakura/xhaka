package com.tinysakura.xhaka.common.gateway.handler.codec;

import com.tinysakura.xhaka.common.protocal.Xhaka;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * xhaka协议请求包发送到网络前的最后一层encoder
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/26
 */
@Slf4j
public class XhakaEncoder extends MessageToMessageEncoder<Xhaka> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Xhaka xhaka, List<Object> list) throws Exception {
        log.info("MessageToMessageEncoder, xhaka:{}", xhaka);

        ByteBuf byteBuf;
        if (xhaka.getBody() != null) {
            byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(xhaka.getHeader().length + xhaka.getBody().length + 4);

            // 配合定长解码器避免粘包问题
            byteBuf.writeInt(xhaka.getHeader().length + xhaka.getBody().length);
            byteBuf.writeBytes(xhaka.getHeader());
            byteBuf.writeBytes(xhaka.getBody());
        } else { // 心跳包body可能为空
            byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(xhaka.getHeader().length + 4);

            byteBuf.writeInt(xhaka.getHeader().length);
            byteBuf.writeBytes(xhaka.getHeader());
        }

        list.add(byteBuf);
        log.info("send xhaka-id:{} request, now:{}", xhaka.getXhakaId(), System.currentTimeMillis());
    }
}