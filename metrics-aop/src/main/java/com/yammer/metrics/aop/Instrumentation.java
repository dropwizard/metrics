package com.yammer.metrics.aop;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.aop.annotation.Gauge;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.MetricsRegistry;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A utility class which instruments {@link Gauge}-,
 * {@link com.yammer.metrics.aop.annotation.Metered}-,
 * {@link com.yammer.metrics.aop.annotation.Timed}-, and
 * {@link com.yammer.metrics.aop.annotation.ExceptionMetered}-annotated classes.
 */
public class Instrumentation {
    private Instrumentation() { /* singleton */ }

    /**
     * Instruments the given object.
     *
     * @param instance    an object
     * @param <T>         the type of the given {@code instance}
     * @return {@code instance}, instrumented
     */
    public static <T> T instrument(T instance) {
        return instrument(Metrics.defaultRegistry(), instance);
    }

    /**
     * Instruments the given object.
     *
     * @param metricsRegistry    the {@link MetricsRegistry} to register metrics with
     * @param instance           an object
     * @param <T>                the type of the given {@code instance}
     * @return {@code instance}, instrumented
     */
    @SuppressWarnings("unchecked")
    public static <T> T instrument(MetricsRegistry metricsRegistry, T instance) {
        try {
            addGauges(metricsRegistry, instance);

            final ProxyFactory factory = new ProxyFactory();
            final Class<?> klass = instance.getClass();
            factory.setSuperclass(klass);
            factory.setFilter(new AnnotatedMethodFilter());

            final Class instrumentedKlass = factory.createClass();
            final MethodHandler handler = MethodHandlerImpl.forClass(metricsRegistry, klass);

            final T o = (T) instrumentedKlass.newInstance();
            ((ProxyObject) o).setHandler(handler);
            return o;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void addGauges(MetricsRegistry registry, final Object obj) {
        for (final Method method : obj.getClass().getMethods()) {
            if (method.isAnnotationPresent(Gauge.class)) {
                final Gauge gauge = method.getAnnotation(Gauge.class);
                final String name = gauge.name().isEmpty() ? method.getName() : gauge.name();
                registry.newGauge(obj.getClass(), name, new GaugeMetric<Object>() {
                    @Override
                    public Object value() {
                        try {
                            return method.invoke(obj);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            }
        }
    }

    private static class MethodInvocationImpl implements MethodInvocation {
        private final Object self;
        private final Method method;
        private final Object[] args;

        public MethodInvocationImpl(Object self, Method method, Object[] args) {
            this.self = self;
            this.method = method;
            this.args = args;
        }

        @Override
        public Method getMethod() {
            return method;
        }

        @Override
        public Object[] getArguments() {
            return args;
        }

        @Override
        public Object proceed() throws Throwable {
            try {
                return method.invoke(self, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        @Override
        public Object getThis() {
            return self;
        }

        @Override
        public AccessibleObject getStaticPart() {
            return getMethod();
        }
    }

    private static class MethodHandlerImpl implements MethodHandler {
        private static MethodHandler forClass(MetricsRegistry metricsRegistry, Class<?> klass) {
            final Map<Method, List<MethodInterceptor>> interceptors = new HashMap<Method, List<MethodInterceptor>>();
            for (Method method : klass.getMethods()) {
                final MethodInterceptor emI = ExceptionMeteredInterceptor.forMethod(metricsRegistry,
                                                                                    klass,
                                                                                    method);
                if (emI != null) {
                    List<MethodInterceptor> list = interceptors.get(method);
                    if (list == null) {
                        list = new ArrayList<MethodInterceptor>();
                        interceptors.put(method, list);
                    }
                    list.add(emI);
                }

                final MethodInterceptor mI = MeteredInterceptor.forMethod(metricsRegistry,
                                                                          klass,
                                                                          method);
                if (mI != null) {
                    List<MethodInterceptor> list = interceptors.get(method);
                    if (list == null) {
                        list = new ArrayList<MethodInterceptor>();
                        interceptors.put(method, list);
                    }
                    list.add(mI);
                }

                final MethodInterceptor tI = TimedInterceptor.forMethod(metricsRegistry,
                                                                        klass,
                                                                        method);

                if (tI != null) {
                    List<MethodInterceptor> list = interceptors.get(method);
                    if (list == null) {
                        list = new ArrayList<MethodInterceptor>();
                        interceptors.put(method, list);
                    }
                    list.add(tI);
                }
            }
            return new MethodHandlerImpl(interceptors);
        }

        private final Map<Method, List<MethodInterceptor>> interceptors;

        private MethodHandlerImpl(Map<Method, List<MethodInterceptor>> interceptors) {
            this.interceptors = interceptors;
        }

        @Override
        public Object invoke(final Object self, final Method m, final Method method, final Object[] args) throws Throwable {
            final List<MethodInterceptor> list = interceptors.get(m);
            if (list != null) {
                final MethodInvocation invocation = new MethodInvocationImpl(self,
                                                                             method,
                                                                             args);
                Object result = null;
                for (MethodInterceptor interceptor : list) {
                    result = interceptor.invoke(invocation);
                }
                return result;
            } else {
                return method.invoke(self, args);
            }
        }
    }
}
