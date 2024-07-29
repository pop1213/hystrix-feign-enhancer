package com.cyb.feign.enhancer;

/**
 * @author cyb
 */
public interface HystrixFeignEnhancer<T> {
    /**
     * 在调用之前执行,返回null则继续执行,否则直接返回
     * @param methodName
     * @param args
     * @return
     */
  Object preHandle(String methodName,Object[] args);

    /**
     * 在调用之后执行
     * @param methodName
     * @param args
     * @param result
     * @return
     */
 T postHandle(String methodName,Object[] args,T result);
}
