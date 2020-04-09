package com.codahale.metrics.jvm;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.util.LinkedHashSet;
import java.util.Set;

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
        private static final URL[] CLASSPATH_ENTRY_URLS;
        private static final PermissionCollection NO_PERMS = new Permissions();

        static {
            String[] classpathEntries = AccessController.doPrivileged(new PrivilegedAction<String>() {
                @Override
                public String run() {
                    return System.getProperty("java.class.path");
                }
            }).split(File.pathSeparator);
            CLASSPATH_ENTRY_URLS = getClasspathEntryUrls(classpathEntries);
        }

        private static URL[] getClasspathEntryUrls(String... classpathEntries) {
            Set<URL> classpathEntryUrls = new LinkedHashSet<>(classpathEntries.length, 1);
            for (String classpathEntry : classpathEntries) {
                URL classpathEntryUrl = getClasspathEntryUrl(classpathEntry);
                if (classpathEntryUrl != null) {
                    classpathEntryUrls.add(classpathEntryUrl);
                }
            }
            return classpathEntryUrls.toArray(new URL[classpathEntryUrls.size()]);
        }

        private static URL getClasspathEntryUrl(String classpathEntry) {
            try {
                if (classpathEntry.endsWith(".jar")) {
                    return new URL("file:jar:" + classpathEntry);
                }
                if (!classpathEntry.endsWith("/")) {
                    classpathEntry = classpathEntry + "/";
                }
                return new URL("file:" + classpathEntry);
            } catch (MalformedURLException mue) {
                // do nothing
            }
            return null;
        }

        public SunManagementNotExistsClassLoader(ClassLoader parent) {
            super(CLASSPATH_ENTRY_URLS, parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (getClass().getName().equals(name)) {
                return getClass();
            }
            if (name.startsWith("com.sun.management.")) {
                throw new ClassNotFoundException(name);
            }
            if (name.startsWith("com.codahale.metrics.jvm.")) {
                return loadMetricsClasses(name);
            }
            return super.loadClass(name, resolve);
        }

        private Class<?> loadMetricsClasses(String name) throws ClassNotFoundException {
            Class<?> ret = findLoadedClass(name);
            if (ret != null) {
                return ret;
            }
            return findClass(name);
        }

        @Override
        protected PermissionCollection getPermissions(CodeSource codesource) {
            return NO_PERMS;
        }
    }
}
