package com.tinysakura.xhaka.client.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/10/14
 */
@Slf4j
@Aspect
public class FilterSupportAsyncAspect {

    @Pointcut("execution(public * org.apache.catalina.core.ApplicationFilterRegistration.addMappingForServletNames(..))")
    public void asyncFilterSupport() {

    }

    @Around("asyncFilterSupport()")
    public Object around(ProceedingJoinPoint joinPoint) {
        CodeSignature signature = (CodeSignature) joinPoint.getSignature();

        // get dispatcherTypes
        String[] parameterNames = signature.getParameterNames();
        Object[] args = joinPoint.getArgs();

        for (int i = 0; i < parameterNames.length; i++) {
            if ("dispatcherTypes".equals(parameterNames[i])) {
                EnumSet dispatcherTypes = (EnumSet) args[i];
                if (!dispatcherTypes.contains(DispatcherType.ASYNC)) {
                    dispatcherTypes.add(DispatcherType.ASYNC);
                }
            }
        }

        try {
            return joinPoint.proceed(args);
        } catch (Throwable throwable) {
            log.error("asyncFilterSupport occur error", throwable);
        }

        return null;
    }
}