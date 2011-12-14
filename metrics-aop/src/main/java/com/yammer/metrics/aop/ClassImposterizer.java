package com.yammer.metrics.aop;

import net.sf.cglib.core.CodeGenerationException;
import net.sf.cglib.core.DefaultNamingPolicy;
import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.core.Predicate;
import net.sf.cglib.proxy.*;
import org.objenesis.ObjenesisStd;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

/**
 * Thanks to jMock & Mockito folks for this handy class that wraps all the cglib magic.
 */
class ClassImposterizer {
    static final ClassImposterizer INSTANCE = new ClassImposterizer();

    private ClassImposterizer() {
    }

    //TODO: after 1.8, in order to provide decent exception message when objenesis is not found,
    //have a constructor in this class that tries to instantiate ObjenesisStd and if it fails then show decent exception that dependency is missing
    //TODO: after 1.8, for the same reason catch and give better feedback when hamcrest core is not found.
    private final ObjenesisStd objenesis = new ObjenesisStd();

    private static final NamingPolicy NAMING_POLICY_THAT_ALLOWS_IMPOSTERISATION_OF_CLASSES_IN_SIGNED_PACKAGES = new DefaultNamingPolicy() {
        @Override
        public String getClassName(String prefix, String source, Object key, Predicate names) {
            return "codegen." + super.getClassName(prefix, source, key, names);
        }
    };

    private static final CallbackFilter IGNORE_BRIDGE_METHODS = new CallbackFilter() {
        public int accept(Method method) {
            return method.isBridge() ? 1 : 0;
        }
    };

    public <T> T imposterise(final MethodInterceptor interceptor, Class<T> mockedType, Class<?>... ancillaryTypes) {
        try {
            setConstructorsAccessible(mockedType, true);
            final Class<?> proxyClass = createProxyClass(mockedType, ancillaryTypes);
            return mockedType.cast(createProxy(proxyClass, interceptor));
        } finally {
            setConstructorsAccessible(mockedType, false);
        }
    }

    private void setConstructorsAccessible(Class<?> mockedType, boolean accessible) {
        for (Constructor<?> constructor : mockedType.getDeclaredConstructors()) {
            constructor.setAccessible(accessible);
        }
    }

    private Class<?> createProxyClass(Class<?> mockedType, Class<?>... interfaces) {
        final Enhancer enhancer = new Enhancer() {
            @Override
            @SuppressWarnings("unchecked")
            protected void filterConstructors(Class sc, List constructors) {
                // Don't filter
            }
        };
        enhancer.setClassLoader(SearchingClassLoader.combineLoadersOf(mockedType));
        enhancer.setUseFactory(true);
        if (mockedType.isInterface()) {
            enhancer.setSuperclass(Object.class);
            enhancer.setInterfaces(prepend(mockedType, interfaces));
        } else {
            enhancer.setSuperclass(mockedType);
            enhancer.setInterfaces(interfaces);
        }
        enhancer.setCallbackTypes(new Class[]{MethodInterceptor.class, NoOp.class});
        enhancer.setCallbackFilter(IGNORE_BRIDGE_METHODS);
        if (mockedType.getSigners() != null) {
            enhancer.setNamingPolicy(
                    NAMING_POLICY_THAT_ALLOWS_IMPOSTERISATION_OF_CLASSES_IN_SIGNED_PACKAGES);
        } else {
            enhancer.setNamingPolicy(new DefaultNamingPolicy());
        }

        try {
            return enhancer.createClass();
        } catch (CodeGenerationException e) {
            if (Modifier.isPrivate(mockedType.getModifiers())) {
                throw new RuntimeException("\n"
                                                   + "Metrics cannot instrument this class: " + mockedType
                                                   + ".\n"
                                                   + "Most likely it is a private class that is not visible by Metrics");
            }
            throw new RuntimeException("\n"
                                               + "Metrics cannot instrument this class: " + mockedType
                                               + "\n"
                                               + "Metrics can only instrument visible & non-final classes.",
                                       e);
        }
    }

    private Object createProxy(Class<?> proxyClass, final MethodInterceptor interceptor) {
        final Factory proxy = (Factory) objenesis.newInstance(proxyClass);
        proxy.setCallbacks(new Callback[]{interceptor, SerializableNoOp.SERIALIZABLE_INSTANCE});
        return proxy;
    }

    private Class<?>[] prepend(Class<?> first, Class<?>... rest) {
        final Class<?>[] all = new Class<?>[rest.length + 1];
        all[0] = first;
        System.arraycopy(rest, 0, all, 1, rest.length);
        return all;
    }
}
