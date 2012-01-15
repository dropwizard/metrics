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

import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import com.yammer.metrics.annotation.Gauge;
import com.yammer.metrics.core.MetricsRegistry;

public class GaugeAnnotationBeanPostProcessor implements BeanPostProcessor, Ordered {

	private final MetricsRegistry metrics;

	public GaugeAnnotationBeanPostProcessor(final MetricsRegistry metrics) {
		this.metrics = metrics;
	}

	@Override
	public int getOrder() {
		return LOWEST_PRECEDENCE;
	}

	@Override
	public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
		return bean;
	}

	@Override
	public Object postProcessAfterInitialization(final Object bean, String beanName) throws BeansException {
		ReflectionUtils.doWithMethods(bean.getClass(), new ReflectionUtils.MethodCallback() {
			public void doWith(final Method method) {
				if (Gauge.class != null && method.isAnnotationPresent(Gauge.class) && method.equals(AopUtils.getMostSpecificMethod(method, bean.getClass()))) {
					if (method.getParameterTypes().length == 0) {
						Gauge gauge = method.getAnnotation(Gauge.class);
						String name = gauge.name().isEmpty() ? method.getName() : gauge.name();
						metrics.newGauge(ClassUtils.getUserClass(bean), name, new com.yammer.metrics.core.Gauge<Object>() {
							@Override
							public Object value() {
								try {
									return method.invoke(bean);
								} catch (Exception e) {
									ReflectionUtils.rethrowRuntimeException(e);
									return null;
								}
							}
						});
					} else {
						throw new IllegalStateException("Method " + method.getName() + " is annotated with @Gauge but requires parameters.");
					}
				}
			}
		});

		return bean;
	}

}