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
import org.springframework.core.PriorityOrdered;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;

public class TimedAnnotationBeanPostProcessor implements BeanPostProcessor, PriorityOrdered {
	
	private final MetricsRegistry metrics;

	public TimedAnnotationBeanPostProcessor(final MetricsRegistry metrics) {
		this.metrics = metrics;
	}

	@Override
	public int getOrder() {
		return HIGHEST_PRECEDENCE;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {
		final Map<String, Timer> timers = new HashMap<String, Timer>();

		ReflectionUtils.doWithMethods(bean.getClass(), new ReflectionUtils.MethodCallback() {
			public void doWith(Method method) {
				if (Timed.class != null && method.isAnnotationPresent(Timed.class) && method.equals(AopUtils.getMostSpecificMethod(method, bean.getClass()))) {
					Timed timed = method.getAnnotation(Timed.class);
					String name = timed.name().isEmpty() ? method.getName() : timed.name();
					Timer timer = metrics.newTimer(ClassUtils.getUserClass(bean), name, timed.durationUnit(), timed.rateUnit());
					timers.put(method.getName(), timer);
				}
			}
		});

		return !timers.isEmpty() ? createProxy(bean, timers) : bean;
	}

	private Object createProxy(Object target, Map<String, Timer> timers) {
		NameMatchMethodPointcut pc = new NameMatchMethodPointcut();
		pc.setMappedNames(timers.keySet().toArray(new String[]{}));

		Advice advice = new TimedMethodInterceptor(timers);
		Advisor advisor = new DefaultPointcutAdvisor(pc, advice);

		ProxyFactory pf = new ProxyFactory();
		pf.addAdvisor(advisor);
		pf.setTarget(target);

		return pf.getProxy();
	}

	private static class TimedMethodInterceptor implements MethodInterceptor {

		private final Map<String, Timer> timers;

		public TimedMethodInterceptor(final Map<String, Timer> timers) {
			this.timers = timers;
		}

		@Override
		public Object invoke(MethodInvocation i) throws Throwable {
			TimerContext context = timers.get(i.getMethod().getName()).time();
			try {
				return i.proceed();
			} finally {
				context.stop();
			}
		}

	}

}