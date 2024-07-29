package com.cyb.feign.enhancer;

/**
 * @author cyb
 */
public abstract class AbstractCacheEnhancer<T> implements HystrixFeignEnhancer<T> {

    /**
     * 从缓存中获取数据
     * @param key
     * @return
     */
    protected abstract Object getFromCache(String key);

    @Override
    public final Object preHandle(String methodName, Object[] args) {
        return getFromCache(buildKey(methodName, args));
    }

    /**
     * 构建缓存key
     * @param methodName
     * @param args
     * @return
     */
    protected abstract String buildKey(String methodName, Object[] args);
    @Override
    public  final T postHandle(String methodName, Object[] args, T result) {
        this.put(buildKey(methodName, args),result);
        return result;
    }
    /**
     * 缓存数据
     */
    public abstract void put(String key, T result);

}
