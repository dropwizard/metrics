/*
 * Copyright 2012 Martello Technologies
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yammer.metrics.spring;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.aop.Advisor;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.aop.support.NameMatchMethodPointcut;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.yammer.metrics.annotation.Metered;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;

public class MeteredAnnotationBeanPostProcessor implements BeanPostProcessor {

	private final MetricsRegistry metrics;

	public MeteredAnnotationBeanPostProcessor(MetricsRegistry metrics) {
		this.metrics = metrics;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {
		final Map<String, Meter> meters = new HashMap<String, Meter>();

		ReflectionUtils.doWithMethods(bean.getClass(), new ReflectionUtils.MethodCallback() {
			public void doWith(Method method) {
				if (Metered.class != null && method.isAnnotationPresent(Metered.class) && method.equals(AopUtils.getMostSpecificMethod(method, bean.getClass()))) {
					Metered metered = method.getAnnotation(Metered.class);
					String name = metered.name().isEmpty() ? method.getName() : metered.name();
					Meter meter = metrics.newMeter(ClassUtils.getUserClass(bean), name, metered.eventType(), metered.rateUnit());
					meters.put(method.getName(), meter);
				}
			}
		});

		return !meters.isEmpty() ? createProxy(bean, meters) : bean;
	}

	private Object createProxy(Object target, Map<String, Meter> meters) {
		NameMatchMethodPointcut pc = new NameMatchMethodPointcut();
		pc.setMappedNames(meters.keySet().toArray(new String[]{}));

		Advice advice = new MeteredMethodInterceptor(meters);
		Advisor advisor = new DefaultPointcutAdvisor(pc, advice);

		ProxyFactory pf = new ProxyFactory();
		pf.addAdvisor(advisor);
		pf.setTarget(target);

		return pf.getProxy();
	}

	private static class MeteredMethodInterceptor implements MethodInterceptor {

		private final Map<String, Meter> meters;

		public MeteredMethodInterceptor(final Map<String, Meter> meters) {
			this.meters = meters;
		}

		@Override
		public Object invoke(MethodInvocation i) throws Throwable {
			meters.get(i.getMethod().getName()).mark();
			return i.proceed();
		}

	}

}