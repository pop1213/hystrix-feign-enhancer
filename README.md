# Getting Started

### hystrix 增强

通过注解配置以及少量代码，实现hystrix增强，实现限流、缓存、监控等功能。

使用方法如下
1. 配置Feign.Builder
```java
    @Bean
    public Feign.Builder feignHystrixBuilder(FeignContext feignContext) {
        EnhancedHystrixFeign.Builder builder = EnhancedHystrixFeign.builder(feignContext);
        return builder;
    }

```
2. 自定义增强器实现，并注入到spring容器
```java
    @Bean
    AbstractCacheEnhancer abstractCachePreHandler(RedisTemplate redisTemplate) {
        return new AbstractCacheEnhancer() {

            @Override
            protected Object getFromCache(String key) {
                return getFromCache(key);
            }

            @Override
            protected String buildKey(String methodName, Object[] args) {
                String key = methodName + ":" + args[0].toString();
                return key;
            }

            public void put(String key, Object result) {
                //do put cache
            }
        };
    }

```
3.配置注解
```java
@FeignClient(name = "shop")
public interface FeignService {
    @HystrixFeignEnhance(enhancer = AbstractCacheEnhancer.class)
    @GetMapping({"/shop/feginFindById/{id}"})
    Result<Shop> feginFindByShopId(@PathVariable("id") BigInteger id);
}
```

