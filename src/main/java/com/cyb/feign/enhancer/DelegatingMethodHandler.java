package com.cyb.feign.enhancer;

import feign.InvocationHandlerFactory;

/**
 * @author cyb
 */
public class DelegatingMethodHandler implements InvocationHandlerFactory.MethodHandler{
    private final InvocationHandlerFactory.MethodHandler delegate;
    private final HystrixFeignEnhancer handler;

    private final String methodName;

    public DelegatingMethodHandler(String methodName,InvocationHandlerFactory.MethodHandler delegate, HystrixFeignEnhancer handler) {
        this.delegate = delegate;
        this.handler = handler;
        this.methodName = methodName;
    }

    @Override
    public Object invoke(Object[] argv) throws Throwable {
        Object result;
        if((result=handler.preHandle(methodName,argv))!=null){
            return result;
        }
        result = delegate.invoke(argv);
        result = handler.postHandle(methodName,argv,result);
        return result;
    }
}
