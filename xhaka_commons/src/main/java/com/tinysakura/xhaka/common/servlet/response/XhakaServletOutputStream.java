package com.tinysakura.xhaka.common.servlet.response;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufOutputStream;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultHttpContent;
import io.netty.handler.codec.http.HttpUtil;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/21
 */

public class XhakaServletOutputStream extends ServletOutputStream {
    private ByteBufOutputStream byteBufOutputStream;
    private XhakaHttpServletResponse xhakaHttpServletResponse;
    private ByteBuf pooledDirectByteBuf;

    private boolean flushed = false;
    private boolean closed = false;

    public XhakaServletOutputStream(XhakaHttpServletResponse response, ByteBuf byteBuf) {
        this.xhakaHttpServletResponse = response;
        this.pooledDirectByteBuf = byteBuf;
        this.byteBufOutputStream = new ByteBufOutputStream(pooledDirectByteBuf);
    }

    @Override
    public boolean isReady() {
        return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {

    }

    @Override
    public void write(int b) throws IOException {
        this.byteBufOutputStream.write(b);
    }

    @Override
    public void write(byte[] b) throws IOException {
        this.byteBufOutputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        this.byteBufOutputStream.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        if (!flushed)
        {
            xhakaHttpServletResponse.setResponseBasicHeader();
        }
        boolean chunked = HttpUtil.isTransferEncodingChunked(xhakaHttpServletResponse.getOriginResponse());
        ChannelHandlerContext ctx = xhakaHttpServletResponse.getCtx();
        if (chunked && ctx.channel().isActive()) {
            if (!flushed) {
                ctx.writeAndFlush(xhakaHttpServletResponse.getOriginResponse());
            }
            this.flushed = true;
        }
    }

    @Override
    public void close() throws IOException {
        if (closed) {
            return;
        }
        closed = true;

        boolean chunked = HttpUtil.isTransferEncodingChunked(xhakaHttpServletResponse.getOriginResponse());
        ChannelHandlerContext ctx = xhakaHttpServletResponse.getCtx();
        if (!chunked) {
            // 设置content-length头
            if (!HttpUtil.isContentLengthSet(xhakaHttpServletResponse.getOriginResponse()))
            {
                HttpUtil.setContentLength(xhakaHttpServletResponse.getOriginResponse(), this.byteBufOutputStream.buffer().readableBytes());
            }
            if (ctx.channel().isActive())
            {
                ctx.writeAndFlush(xhakaHttpServletResponse.getOriginResponse());
            }
        }

        if (byteBufOutputStream.buffer().writerIndex() > byteBufOutputStream.buffer().readerIndex() && ctx.channel().isActive()) {
            ctx.writeAndFlush((new DefaultHttpContent(byteBufOutputStream.buffer())));
            resetBuffer();
        }
    }

    public void resetBuffer() {
        this.byteBufOutputStream.buffer().clear();
    }

    public int getBufferSize() {
        return this.pooledDirectByteBuf.capacity();
    }
}