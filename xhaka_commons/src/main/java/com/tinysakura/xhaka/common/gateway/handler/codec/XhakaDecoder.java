package com.tinysakura.xhaka.common.gateway.handler.codec;

import com.tinysakura.xhaka.common.protocal.Xhaka;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/26
 */
@Slf4j
public class XhakaDecoder extends LengthFieldBasedFrameDecoder {

    public XhakaDecoder(int maxFrameLength, int lengthFieldOffset,
                               int lengthFieldLength) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf frame = (ByteBuf) super.decode(ctx, in);

        //说明是半包，交由io线程继续读
        if (frame == null) {
            return null;
        }

        try {
            byte[] bytes = new byte[frame.readableBytes() - 4];
            log.info("frame length:{}", frame.readInt());
            frame.readBytes(bytes);

            Xhaka xhaka = new Xhaka(ArrayUtils.subarray(bytes, 0, 13));
            frame.release();

            xhaka.setBody(ArrayUtils.subarray(bytes, 13, bytes.length));
            log.info("receive xhaka-id:{} response, now:{}", xhaka.getXhakaId(), System.currentTimeMillis());

            return xhaka;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}