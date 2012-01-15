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

import com.yammer.metrics.annotation.ExceptionMetered;
import com.yammer.metrics.core.Meter;
import com.yammer.metrics.core.MetricsRegistry;

import static com.yammer.metrics.annotation.ExceptionMetered.DEFAULT_NAME_SUFFIX;

public class ExceptionMeteredAnnotationBeanPostProcessor implements BeanPostProcessor {

	private final MetricsRegistry metrics;

	public ExceptionMeteredAnnotationBeanPostProcessor(final MetricsRegistry metrics) {
		this.metrics = metrics;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {
		final Map<String, Pair<Meter, Class<? extends Throwable>>> meters = new HashMap<String, Pair<Meter, Class<? extends Throwable>>>();

		ReflectionUtils.doWithMethods(bean.getClass(), new ReflectionUtils.MethodCallback() {
			public void doWith(Method method) {
				if (ExceptionMetered.class != null && method.isAnnotationPresent(ExceptionMetered.class) && method.equals(AopUtils.getMostSpecificMethod(method, bean.getClass()))) {
					ExceptionMetered metered = method.getAnnotation(ExceptionMetered.class);
					String name = metered.name().isEmpty() ? method.getName() + DEFAULT_NAME_SUFFIX : metered.name();
					Meter meter = metrics.newMeter(ClassUtils.getUserClass(bean), name, metered.eventType(), metered.rateUnit());
					meters.put(method.getName(), new Pair<Meter, Class<? extends Throwable>>(meter, metered.cause()));
				}
			}
		});

		return !meters.isEmpty() ? createProxy(bean, meters) : bean;
	}

	private Object createProxy(Object target, Map<String, Pair<Meter, Class<? extends Throwable>>> meters) {
		NameMatchMethodPointcut pc = new NameMatchMethodPointcut();
		pc.setMappedNames(meters.keySet().toArray(new String[]{}));

		Advice advice = new ExceptionMeteredMethodInterceptor(meters);
		Advisor advisor = new DefaultPointcutAdvisor(pc, advice);

		ProxyFactory pf = new ProxyFactory();
		pf.addAdvisor(advisor);
		pf.setTarget(target);

		return pf.getProxy();
	}

	private static class Pair<A, B> {

		private final A left;
		private final B right;

		public Pair(final A left, final B right) {
			this.left = left;
			this.right = right;
		}

		public A getLeft() {
			return left;
		}

		public B getRight() {
			return right;
		}

	}

	private static class ExceptionMeteredMethodInterceptor implements MethodInterceptor {

		private final Map<String, Pair<Meter, Class<? extends Throwable>>> meters;

		public ExceptionMeteredMethodInterceptor(final Map<String, Pair<Meter, Class<? extends Throwable>>> meters) {
			this.meters = meters;
		}

		@Override
		public Object invoke(MethodInvocation i) throws Throwable {
			try {
				return i.proceed();
			} catch (Throwable t) {
				Pair<Meter, Class<? extends Throwable>> pair = meters.get(i.getMethod().getName());
				if (pair.getRight().isAssignableFrom(t.getClass())) {
					pair.getLeft().mark();
				}
				throw t;
			}
		}

	}

}