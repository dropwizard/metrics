package com.yammer.metrics.spring;

import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.Pointcut;
import org.springframework.aop.PointcutAdvisor;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.AopInfrastructureBean;
import org.springframework.aop.framework.ProxyConfig;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ClassUtils;

public abstract class AbstractProxyingBeanPostProcessor extends ProxyConfig implements
                                                                            BeanPostProcessor {

    private static final long serialVersionUID = -3482052668071169769L;

    private final Log log = LogFactory.getLog(getClass());

    private final ClassLoader beanClassLoader = ClassUtils.getDefaultClassLoader();

    public abstract MethodInterceptor getMethodInterceptor(Class<?> targetClass);

    public abstract Pointcut getPointcut();

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof AopInfrastructureBean) {
            return bean;
        }

        final Class<?> targetClass = AopUtils.getTargetClass(bean);
        final Pointcut pointcut = getPointcut();

        if (AopUtils.canApply(pointcut, targetClass)) {
            final MethodInterceptor interceptor = getMethodInterceptor(targetClass);
            final PointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, interceptor);

            if (bean instanceof Advised) {
                if (log.isDebugEnabled()) {
                    log.debug("Bean " + beanName + " is already proxied, adding Advisor to existing proxy");
                }

                ((Advised) bean).addAdvisor(0, advisor);
                return bean;
            }

            if (log.isDebugEnabled()) {
                log.debug("Proxying bean " + beanName + " of type " + targetClass.getCanonicalName());
            }

            final ProxyFactory proxyFactory = new ProxyFactory(bean);
            proxyFactory.copyFrom(this);
            proxyFactory.addAdvisor(advisor);

            return proxyFactory.getProxy(this.beanClassLoader);
        }

        return bean;
    }

}
