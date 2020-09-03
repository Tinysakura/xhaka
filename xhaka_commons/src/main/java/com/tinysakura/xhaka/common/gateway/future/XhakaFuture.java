package com.tinysakura.xhaka.common.gateway.future;

import com.tinysakura.xhaka.common.gateway.constant.XhakaHttpHeaderConstant;
import com.tinysakura.xhaka.common.gateway.exception.XhakaSlaveTimeoutException;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * reference from dubbo : com.alibaba.dubbo.remoting.exchange.support.DefaultFuture
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/31
 */
@Slf4j
public class XhakaFuture {
    private static final Map<Long, XhakaFuture> FUTURES = new ConcurrentHashMap<Long, XhakaFuture>();

    private ReentrantLock lock = new ReentrantLock();

    private final Condition done = lock.newCondition();

    private FullHttpResponse response;

    public XhakaFuture(Long xhakaRequestId) {
        FUTURES.put(xhakaRequestId, this);
    }

    public FullHttpResponse get(Integer timeout) throws Exception {
        try {
            if (!isReceivedResponse()) {
                long start = System.currentTimeMillis();
                lock.lock();

                while (!isReceivedResponse()) {
                    done.await(timeout, TimeUnit.MILLISECONDS);
                    if (isReceivedResponse() || System.currentTimeMillis() - start > timeout) {
                        break;
                    }
                }
            }
        } catch (InterruptedException e) {
           log.error("get xhaka slave response occur InterruptedException", e);
        } finally {
            lock.unlock();
        }

        if (!isReceivedResponse()) {
            throw new XhakaSlaveTimeoutException();
        }

        return response;
    }

    public boolean isReceivedResponse() {
        return response != null;
    }

    public static void received(FullHttpResponse response) {
        Long responseId = Long.valueOf(response.headers().get(XhakaHttpHeaderConstant.HTTP_HEADER_XHAKA_ID));

        XhakaFuture xhakaFuture = FUTURES.get(responseId);
        xhakaFuture.doReceived(response);
    }

    public void doReceived(FullHttpResponse response) {
        // 可重入的
        lock.lock();

        try {
            this.response = response;
            // 唤醒调用get的业务线程
            done.signal();
        } catch (Exception e) {
            log.error("doReceived response occur error", e);
        } finally {
            lock.unlock();
        }
    }
}