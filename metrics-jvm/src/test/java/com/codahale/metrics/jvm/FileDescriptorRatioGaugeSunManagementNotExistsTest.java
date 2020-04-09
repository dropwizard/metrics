package com.codahale.metrics.jvm;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

@RunWith(FileDescriptorRatioGaugeSunManagementNotExistsTest.SunManagementNotExistsTestRunner.class)
public class FileDescriptorRatioGaugeSunManagementNotExistsTest {

    @Test
    public void validateFileDescriptorRatioWhenSunManagementNotExists() {
        assertThat(new FileDescriptorRatioGauge().getValue()).isNaN();
    }

    public static class SunManagementNotExistsTestRunner extends BlockJUnit4ClassRunner {

        public SunManagementNotExistsTestRunner(Class<?> clazz) throws InitializationError {
            super(getFromSunManagementNotExistsClassLoader(clazz));
        }

        private static Class<?> getFromSunManagementNotExistsClassLoader(Class<?> clazz) throws InitializationError {
            try {
                return Class.forName(clazz.getName(), true,
                        new SunManagementNotExistsClassLoader(SunManagementNotExistsTestRunner.class.getClassLoader()));
            } catch (ClassNotFoundException e) {
                throw new InitializationError(e);
            }
        }
    }

    public static class SunManagementNotExistsClassLoader extends URLClassLoader {
        private static final PermissionCollection NO_PERMS = new Permissions();

        public SunManagementNotExistsClassLoader(ClassLoader parent) {
            super(((URLClassLoader) getSystemClassLoader()).getURLs(), parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (getClass().getName().equals(name)) {
                return getClass();
            }
            if (name.startsWith("com.sun.management.")) {
                throw new ClassNotFoundException(name);
            }
            if (name.startsWith("com.codahale.metrics.")) {
                Class<?> ret = findLoadedClass(name);
                if (ret != null) {
                    return ret;
                }
                return findClass(name);
            }
            return super.loadClass(name, resolve);
        }

        @Override
        protected PermissionCollection getPermissions(CodeSource codesource) {
            return NO_PERMS;
        }
    }
}
