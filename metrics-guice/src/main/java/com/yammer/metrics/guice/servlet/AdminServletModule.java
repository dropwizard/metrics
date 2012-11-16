package com.yammer.metrics.guice.servlet;

import com.fasterxml.jackson.core.JsonFactory;
import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import com.yammer.metrics.reporting.AdminServlet;

/**
 * A guice servlet module that registers the {@link AdminServlet} via guice and also configures all
 * healthchecks bound via guice to it.
 *
 * To use, install this module in your servlet module (or add as a separate module), and bind the health checks
 * via a multi binder:
 * <pre>
 * <code>install(new AdminServletModule());
 *
 * Multibinder&lt;HealthCheck&gt; healthChecksBinder = Multibinder.newSetBinder(binder(), HealthCheck.class);
 *
 * healthChecksBinder.addBinding().to(MyCoolHealthCheck.class);
 * healthChecksBinder.addBinding().to(MyOtherCoolHealthCheck.class);
 * </code>
 * </pre>
 * The module offers the same overloaded constructors to specifiy a custom {@link JsonFactory} and the uris
 * for the healthcheck, metrics, etc. E.g.
 * <pre>
 * <code>install(new AdminServletModule("/1.0/healthcheck", "/1.0/metrics", "/1.0/ping", "/1.0/threads"));
 * </code>
 * </pre>
 * In order to use this module, you need the <code>guice-servlet</code> and <code>guice-multibindings</code>
 * dependencies in addition to the normal <code>guice</code> dependency:
 * <pre>
 * {@code <dependency>
 *   <groupId>com.google.inject</groupId>
 *   <artifactId>guice</artifactId>
 *   <version>3.0</version>
 * </dependency>
 * <dependency>
 *   <groupId>com.google.inject.extensions</groupId>
 *   <artifactId>guice-servlet</artifactId>
 *   <version>3.0</version>
 * </dependency>
 * <dependency>
 *   <groupId>com.google.inject.extensions</groupId>
 *   <artifactId>guice-multibindings</artifactId>
 *   <version>3.0</version>
 * </dependency>
 * }
 * </pre>
 */
public class AdminServletModule extends ServletModule {
    private final JsonFactory jsonFactory;
    private final String healthcheckUri;
    private final String metricsUri;
    private final String pingUri;
    private final String threadsUri;

    public AdminServletModule() {
        this(null, AdminServlet.DEFAULT_HEALTHCHECK_URI, AdminServlet.DEFAULT_METRICS_URI,
             AdminServlet.DEFAULT_PING_URI, AdminServlet.DEFAULT_THREADS_URI);
    }

    public AdminServletModule(JsonFactory jsonFactory) {
        this(jsonFactory, AdminServlet.DEFAULT_HEALTHCHECK_URI, AdminServlet.DEFAULT_METRICS_URI,
             AdminServlet.DEFAULT_PING_URI, AdminServlet.DEFAULT_THREADS_URI);
    }

    public AdminServletModule(String healthcheckUri, String metricsUri, String pingUri, String threadsUri) {
        this(null, healthcheckUri, metricsUri, pingUri, threadsUri);
    }

    public AdminServletModule(JsonFactory jsonFactory, String healthcheckUri, String metricsUri, String pingUri, String threadsUri) {
        this.jsonFactory = jsonFactory;
        this.healthcheckUri = healthcheckUri;
        this.metricsUri = metricsUri;
        this.pingUri = pingUri;
        this.threadsUri = threadsUri;
    }

    @Override
    protected void configureServlets() {
        if (jsonFactory != null) {
            bind(JsonFactory.class).annotatedWith(Names.named("AdminServlet.JSON_FACTORY")).toInstance(jsonFactory);
        }
        bind(String.class).annotatedWith(Names.named("AdminServlet.HEALTHCHECK_URI")).toInstance(healthcheckUri);
        bind(String.class).annotatedWith(Names.named("AdminServlet.METRICS_URI")).toInstance(metricsUri);
        bind(String.class).annotatedWith(Names.named("AdminServlet.PING_URI")).toInstance(pingUri);
        bind(String.class).annotatedWith(Names.named("AdminServlet.THREADS_URI")).toInstance(threadsUri);
        bind(AdminServlet.class).toProvider(AdminServletProvider.class).asEagerSingleton();

        serve(healthcheckUri, metricsUri, pingUri, threadsUri).with(AdminServlet.class);
    }
}
