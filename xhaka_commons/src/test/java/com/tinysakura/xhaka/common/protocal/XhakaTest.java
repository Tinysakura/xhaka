package com.tinysakura.xhaka.common.protocal;

import com.tinysakura.xhaka.common.protocal.constant.XhakaHeaderConstant;
import com.tinysakura.xhaka.common.util.BytesUtil;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.nio.charset.Charset;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/20
 */
@SpringBootTest
public class XhakaTest {
    @Test
    public void testByte2BinaryStr() {
        System.out.println(BytesUtil.byte2BinaryStr((byte) 15));
    }


    @Test
    public void XhakaTest() {

        Xhaka xhaka = new Xhaka();
        xhaka.setEventType(XhakaHeaderConstant.XHAKA_EVENT_TYPE_REQUEST);
        xhaka.setPackType(XhakaHeaderConstant.XHAKA_PACK_TYPE_NORMAL);
        xhaka.setSerialization(XhakaHeaderConstant.XHAKA_SERIALIZATION_PROTOBUF);
        xhaka.setXhakaStatus(XhakaHeaderConstant.XHAKA_STATUS_BAD_REQUEST_BODY);
        xhaka.setXhakaId(1928L);
        byte[] body = "abc".getBytes(Charset.forName("UTF-8"));
        xhaka.setBody(body);
        //System.out.println("length:" + body.length);
        xhaka.setBodyLength(body.length);


        for (byte b : xhaka.getHeader()) {
            System.out.println(BytesUtil.byte2BinaryStr(b));
        }


        Assert.assertEquals(XhakaHeaderConstant.XHAKA_EVENT_TYPE_REQUEST, xhaka.getEventType());
        Assert.assertEquals(XhakaHeaderConstant.XHAKA_PACK_TYPE_NORMAL, xhaka.getPackType());
        Assert.assertEquals(XhakaHeaderConstant.XHAKA_SERIALIZATION_PROTOBUF, xhaka.getSerialization());
        Assert.assertEquals(XhakaHeaderConstant.XHAKA_STATUS_BAD_REQUEST_BODY, xhaka.getXhakaStatus());
        Assert.assertEquals(java.util.Optional.of(1928L).get(), xhaka.getXhakaId());
        Assert.assertEquals(java.util.Optional.of(body.length).get(), xhaka.getBodyLength());
        Assert.assertEquals("abc", new String(xhaka.getBody(), Charset.forName("UTF-8")));
    }

}