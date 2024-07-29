package com.cyb.feign.enhancer;

import java.lang.annotation.*;

/**
 * @author cyb
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HystrixFeignEnhance {
    Class<? extends HystrixFeignEnhancer> enhancer() ;

}
