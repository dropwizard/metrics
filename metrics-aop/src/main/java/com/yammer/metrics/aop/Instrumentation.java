package com.yammer.metrics.aop;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.annotation.Gauge;
import com.yammer.metrics.core.GaugeMetric;
import com.yammer.metrics.core.MetricsRegistry;
import net.sf.cglib.proxy.MethodProxy;
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
 * {@link com.yammer.metrics.annotation.Metered}-,
 * {@link com.yammer.metrics.annotation.Timed}-, and
 * {@link com.yammer.metrics.annotation.ExceptionMetered}-annotated classes.
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
            final Object proxy = ClassImposterizer.INSTANCE
                    .imposterise(MethodHandlerImpl.forClass(metricsRegistry, instance),
                                 instance.getClass());
            new LenientCopyTool().copyToMock(instance, proxy);
            return (T) proxy;
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
        private final Object proxy;
        private final Method method;
        private final MethodProxy methodProxy;
        private final Object[] args;

        public MethodInvocationImpl(Object proxy, Method method, MethodProxy methodProxy, Object[] args) {
            this.proxy = proxy;
            this.method = method;
            this.methodProxy = methodProxy;
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
                return methodProxy.invokeSuper(proxy, args);
            } catch (InvocationTargetException e) {
                throw e.getCause();
            }
        }

        @Override
        public Object getThis() {
            return proxy;
        }

        @Override
        public AccessibleObject getStaticPart() {
            return getMethod();
        }
    }

    private static class MethodHandlerImpl implements net.sf.cglib.proxy.MethodInterceptor {
        private static net.sf.cglib.proxy.MethodInterceptor forClass(MetricsRegistry metricsRegistry, Object instance) {
            final Map<Method, List<MethodInterceptor>> interceptors = new HashMap<Method, List<MethodInterceptor>>();
            for (Method method : instance.getClass().getMethods()) {
                method.setAccessible(true);
                final MethodInterceptor emI = ExceptionMeteredInterceptor.forMethod(metricsRegistry,
                                                                                    instance.getClass(),
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
                                                                          instance.getClass(),
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
                                                                        instance.getClass(),
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
        public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            final List<MethodInterceptor> list = interceptors.get(method);
            if (list != null) {
                final MethodInvocation invocation = new MethodInvocationImpl(proxy, method, methodProxy, args);
                Object result = null;
                for (MethodInterceptor interceptor : list) {
                    result = interceptor.invoke(invocation);
                }
                return result;
            } else {
                return methodProxy.invokeSuper(proxy, args);
            }
        }
    }
}
