package com.cyb.feign.enhancer;

import feign.*;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.codec.ErrorDecoder;
import feign.hystrix.*;
import org.springframework.beans.BeansException;
import org.springframework.cloud.openfeign.FeignContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.GenericApplicationContext;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.config.BeanDefinition;
/**
 * @author cyb
 */
public class EnhancedHystrixFeign   {
    public static EnhancedHystrixFeign.Builder builder(FeignContext feignContext) {
        return new EnhancedHystrixFeign.Builder(feignContext);
    }

    public static final class Builder extends Feign.Builder implements ApplicationContextAware {

        private ApplicationContext applicationContext;

        private FeignContext feignContext;

        public Builder(FeignContext feignContext) {
            this.feignContext = feignContext;
        }
        @Override
        public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
            this.applicationContext=applicationContext;
        }
        private Contract contract = new Contract.Default();
        private SetterFactory setterFactory = new SetterFactory.Default();


        /**
         * Allows you to override hystrix properties such as thread pools and command keys.
         */
        public EnhancedHystrixFeign.Builder setterFactory(SetterFactory setterFactory) {
            this.setterFactory = setterFactory;
            return this;
        }



        @Override
        public Feign.Builder invocationHandlerFactory(InvocationHandlerFactory invocationHandlerFactory) {
            throw new UnsupportedOperationException();
        }

        @Override
        public EnhancedHystrixFeign.Builder contract(Contract contract) {
            this.contract = contract;
            return this;
        }

        @Override
        public Feign build() {
            super.invocationHandlerFactory(new InvocationHandlerFactory() {
                @Override
                public InvocationHandler create(Target target,
                                                Map<Method, MethodHandler> dispatch) {
                    GenericApplicationContext gctx = (GenericApplicationContext)EnhancedHystrixFeign.Builder.this.applicationContext;
                    BeanDefinition def = gctx.getBeanDefinition(target.type().getName());
                    String contextId = (String)def.getPropertyValues().get("contextId");
                    Class<FallbackFactory> nullableFallbackFactoryClass = (Class)def.getPropertyValues().get("fallbackFactory");
                    FallbackFactory fallbackFactory = feignContext.getInstance(contextId, nullableFallbackFactoryClass);

                    Class fallbackClass = (Class)def.getPropertyValues().get("fallback");
                    if(fallbackClass!=void.class){
                        Object instance = feignContext.getInstance(contextId, fallbackClass);
                        fallbackFactory = new FallbackFactory.Default(instance);
                    }
                    Map<Method, MethodHandler> preHandlerMap = parsePreHandler(contextId,dispatch);
                    return new EnhancedHystrixInvocationHandler(target, preHandlerMap, setterFactory,
                            fallbackFactory);
                }
                private Map<Method, MethodHandler> parsePreHandler(String contextId,Map<Method, InvocationHandlerFactory.MethodHandler> dispatch) {
                    return dispatch.entrySet().stream().map(e->{
                        Method method = e.getKey();
                        HystrixFeignEnhance annotation = method.getAnnotation(HystrixFeignEnhance.class);
                        if(annotation!=null){
                            Class<? extends HystrixFeignEnhancer> aClass = annotation.enhancer();
                            HystrixFeignEnhancer instance = feignContext.getInstance(contextId, aClass);
                            e.setValue(new DelegatingMethodHandler(method.getName(),e.getValue(),instance));
                        }
                        return e;
                    }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
                }

            });
            super.contract(new HystrixDelegatingContract(contract));
            return super.build();
        }

        /** Configures components needed for hystrix integration. */

        // Covariant overrides to support chaining to new fallback method.
        @Override
        public EnhancedHystrixFeign.Builder logLevel(Logger.Level logLevel) {
            return (EnhancedHystrixFeign.Builder) super.logLevel(logLevel);
        }

        @Override
        public EnhancedHystrixFeign.Builder client(Client client) {
            return (EnhancedHystrixFeign.Builder) super.client(client);
        }

        @Override
        public EnhancedHystrixFeign.Builder retryer(Retryer retryer) {
            return (EnhancedHystrixFeign.Builder) super.retryer(retryer);
        }

        @Override
        public EnhancedHystrixFeign.Builder logger(Logger logger) {
            return (EnhancedHystrixFeign.Builder) super.logger(logger);
        }

        @Override
        public EnhancedHystrixFeign.Builder encoder(Encoder encoder) {
            return (EnhancedHystrixFeign.Builder) super.encoder(encoder);
        }

        @Override
        public EnhancedHystrixFeign.Builder decoder(Decoder decoder) {
            return (EnhancedHystrixFeign.Builder) super.decoder(decoder);
        }

        @Override
        public EnhancedHystrixFeign.Builder mapAndDecode(ResponseMapper mapper, Decoder decoder) {
            return (EnhancedHystrixFeign.Builder) super.mapAndDecode(mapper, decoder);
        }

        @Override
        public EnhancedHystrixFeign.Builder decode404() {
            return (EnhancedHystrixFeign.Builder) super.decode404();
        }

        @Override
        public EnhancedHystrixFeign.Builder errorDecoder(ErrorDecoder errorDecoder) {
            return (EnhancedHystrixFeign.Builder) super.errorDecoder(errorDecoder);
        }

        @Override
        public EnhancedHystrixFeign.Builder options(Request.Options options) {
            return (EnhancedHystrixFeign.Builder) super.options(options);
        }

        @Override
        public EnhancedHystrixFeign.Builder requestInterceptor(RequestInterceptor requestInterceptor) {
            return (EnhancedHystrixFeign.Builder) super.requestInterceptor(requestInterceptor);
        }

        @Override
        public EnhancedHystrixFeign.Builder requestInterceptors(Iterable<RequestInterceptor> requestInterceptors) {
            return (EnhancedHystrixFeign.Builder) super.requestInterceptors(requestInterceptors);
        }
    }
}
