package com.cyb.feign.config;

import feign.Feign;
import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.context.annotation.Bean;

/**
 * @author cyb
 */
public class HystrixEnhancerConfig {
    @Bean
    public Feign.Builder feignHystrixBuilder(FeignContext feignContext) {
        com.jingmai.mall.goods.feign.EnhancedHystrixFeign.Builder builder = com.jingmai.mall.goods.feign.EnhancedHystrixFeign.builder(feignContext);
        return builder;
    }

}
