package com.tinysakura.xhaka.common.servlet.request;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import java.io.IOException;

/**
 * 装饰者模式
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/21
 */
public class XhakaServletInputStream extends ServletInputStream {

    private ByteBuf fullHttpRequestByteBuf;

    private ByteBufInputStream byteBufInputStream;

    private volatile boolean closed = false;

    public XhakaServletInputStream(ByteBuf byteBuf) {
        this.fullHttpRequestByteBuf = byteBuf;
        this.byteBufInputStream = new ByteBufInputStream(byteBuf);
    }


    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void setReadListener(ReadListener readListener) {

    }

    @Override
    public int read() throws IOException {
        return this.byteBufInputStream.read();
    }

    @Override
    public int read(byte[] b) throws IOException {
        return this.byteBufInputStream.read(b);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        return this.byteBufInputStream.read(b, off, len);
    }

    @Override
    public void close() throws IOException {
        if (!closed) {
            this.fullHttpRequestByteBuf.release();
            closed = true;
        }
    }
}