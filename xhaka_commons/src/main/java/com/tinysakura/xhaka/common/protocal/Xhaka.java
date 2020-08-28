package com.tinysakura.xhaka.common.protocal;

import com.tinysakura.xhaka.common.util.BytesUtils;

/**
 * xhaka协议
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/20
 */
public class Xhaka {
    public static final byte XHAKA_EVENT_TYPE_MASK = 1;

    public static final byte XHAKA_PACK_TYPE_MASK = 2;

    public static final byte XHAKA_SERIALIZATION_MASK = 12;

    public static final int XHAKA_STATUS_MASK = 15 << 4;

    /**
     * 协议控制头（固定为13个字节104位）
     * 第1位表示事件类型，header[0] & 1 == 1 ? request : response
     * 第2为表示xhaka包类型，header[0] & (1 << 1) == 1 ? 普通包 : 心跳包
     * 第3,4位表示消息体使用的序列化协议，((header[0] << 4) & (3 << 6)) >> 6  0:java serializable 1:json 2:protobuf
     * 第5~8位表示协议状态 header[0] >> 4
     *
     * 第2~9个字节表示请求编号，运行时生成 Bytes.bytes2Long(header, 1);
     * 第10~13个字节表示消息体长度，运行时生成 Bytes.bytes2Int(header, 9);
     */
    private byte[] header;
    private byte[] body;

    public Xhaka() {
        this.header = new byte[13];
    }

    public Xhaka(byte[] header) {
        this.header = header;
    }

    public void setEventType(byte eventType) {
        header[0] = (byte) ((header[0] << 1) | eventType);
    }

    public byte getEventType() {
        return (byte) (header[0] & XHAKA_EVENT_TYPE_MASK);
    }

    public void setPackType(byte packType) {
        header[0] = (byte) ((header[0] & ~XHAKA_PACK_TYPE_MASK) | (packType << 1));
    }

    public byte getPackType() {
        return (byte) ((header[0] & XHAKA_PACK_TYPE_MASK) >> 1);
    }

    public void setSerialization(byte serializationType) {
        header[0] = (byte) ((header[0] & ~XHAKA_SERIALIZATION_MASK) | (serializationType << 2));
    }

    public byte getSerialization() {
        return (byte) ((header[0] & XHAKA_SERIALIZATION_MASK) >> 2);
    }

    public void setXhakaStatus(byte xhakaStatus) {
        // 0001 0101
        // 1111 0000
        header[0] = (byte) ((header[0] & ~XHAKA_STATUS_MASK) | (xhakaStatus << 4));
    }

    public byte getXhakaStatus() {
        return (byte) (header[0] >> 4);
    }

    public Long getXhakaId() {
        return BytesUtils.bytes2long(header, 1);
    }

    public void setXhakaId(Long xhakaId) {
        BytesUtils.long2bytes(xhakaId, header, 1);
    }

    public Integer getBodyLength() {
        return BytesUtils.bytes2int(header, 9);
    }

    public void setBodyLength(Integer bodyLength) {
        BytesUtils.int2bytes(bodyLength, header, 9);
    }

    public void setBody(byte[] body) {
        this.body = body;
    }

    public byte[] getBody() {
        return this.body;
    }

    public byte[] getHeader() {
        return header;
    }
}