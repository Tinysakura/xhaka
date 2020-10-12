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

    private Long xhakaRequestId;

    public XhakaFuture(Long xhakaRequestId) {
        FUTURES.put(xhakaRequestId, this);
        this.xhakaRequestId = xhakaRequestId;
    }

    public FullHttpResponse get(Integer timeout) throws Exception {
        try {
            if (!isReceivedResponse()) {
                long start = System.currentTimeMillis();
                lock.lock();

                while (!isReceivedResponse()) {
                    log.info("进入循环，xhaka-id:{}", xhakaRequestId);
                    Boolean timeOut = !done.await(timeout, TimeUnit.SECONDS);
                    log.info("超时，xhaka-id:{}, timeOut:{}", xhakaRequestId, timeOut);

                    String xhakaId = response.headers().get("xhaka-id");
                    if (!timeOut) {
                        log.info("await end, xhaka-id:{} repsonse, now:{}", xhakaId, System.currentTimeMillis());
                    } else {
                        log.info("await timeOut, xhaka-id:{}, now:{}", xhakaId, System.currentTimeMillis());
                    }

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

        log.info("break loop, xhaka-id:{}", xhakaRequestId);
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
            String xhakaId = response.headers().get("xhaka-id");
            log.info("signal, xhaka-id:{} repsonse, now:{}", xhakaId, System.currentTimeMillis());
        } catch (Exception e) {
            log.error("doReceived response occur error", e);
        } finally {
            lock.unlock();
        }
    }
}