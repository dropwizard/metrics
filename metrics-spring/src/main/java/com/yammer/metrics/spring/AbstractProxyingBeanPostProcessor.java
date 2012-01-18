package com.yammer.metrics.spring;

import org.aopalliance.intercept.MethodInterceptor;
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

public abstract class AbstractProxyingBeanPostProcessor extends ProxyConfig implements BeanPostProcessor {

	private static final long serialVersionUID = -3482052668071169769L;

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

		Class<?> targetClass = AopUtils.getTargetClass(bean);
		Pointcut pointcut = getPointcut();

		if (AopUtils.canApply(pointcut, targetClass)) {
			MethodInterceptor interceptor = getMethodInterceptor(targetClass);
			PointcutAdvisor advisor = new DefaultPointcutAdvisor(pointcut, interceptor);

			if (bean instanceof Advised) {
				((Advised) bean).addAdvisor(0, advisor);
				return bean;
			}

			ProxyFactory proxyFactory = new ProxyFactory(bean);
			proxyFactory.copyFrom(this);
			proxyFactory.addAdvisor(advisor);

			return proxyFactory.getProxy(this.beanClassLoader);
		}

		return bean;
	}

}