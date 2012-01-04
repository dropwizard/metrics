package com.yammer.metrics.guice.servlet;

import org.codehaus.jackson.JsonFactory;

import com.google.inject.name.Names;
import com.google.inject.servlet.ServletModule;
import com.yammer.metrics.reporting.MetricsServlet;

/**
 * A guice servlet module that registers the {@link MetricsServlet} via guice and also configures all
 * healthchecks bound via guice to it.
 *
 * To use, install this module in your servlet module (or add as a separate module), and bind the health checks
 * via a multi binder:
 * <pre>
 * {@code
 * install(new MetricsServletModule());
 *
 * Multibinder<HealthCheck> healthChecksBinder = Multibinder.newSetBinder(binder(), HealthCheck.class);
 *
 * healthChecksBinder.addBinding().to(MyCoolHealthCheck.class);
 * healthChecksBinder.addBinding().to(MyOtherCoolHealthCheck.class);
 * }
 * </pre>
 * The module offers the same overloaded constructors to specifiy a custom {@link JsonFactory} and the uris
 * for the healthcheck, metrics, etc. E.g.
 * <pre>
 * {@code
 * install(new MetricsServletModule("/1.0/healthcheck", "/1.0/metrics", "/1.0/ping", "/1.0/threads"));
 * }
 * </pre>
 * In order to use this module, you need the <code>guice-servlet</code> and <code>guice-multibindings</code>
 * dependencies in addition to the normal <code>guice</code> dependency:
 * <pre>
 * {@code
 * <dependency>
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
public class MetricsServletModule extends ServletModule {
    private final JsonFactory jsonFactory;
    private final String healthcheckUri;
    private final String metricsUri;
    private final String pingUri;
    private final String threadsUri;

    public MetricsServletModule() {
        this(null, MetricsServlet.HEALTHCHECK_URI, MetricsServlet.METRICS_URI, MetricsServlet.PING_URI, MetricsServlet.THREADS_URI);
    }

    public MetricsServletModule(JsonFactory jsonFactory) {
        this(jsonFactory, MetricsServlet.HEALTHCHECK_URI, MetricsServlet.METRICS_URI, MetricsServlet.PING_URI, MetricsServlet.THREADS_URI);
    }

    public MetricsServletModule(String healthcheckUri, String metricsUri, String pingUri, String threadsUri) {
        this(null, healthcheckUri, metricsUri, pingUri, threadsUri);
    }

    public MetricsServletModule(JsonFactory jsonFactory, String healthcheckUri, String metricsUri, String pingUri, String threadsUri) {
        this.jsonFactory = jsonFactory;
        this.healthcheckUri = healthcheckUri;
        this.metricsUri = metricsUri;
        this.pingUri = pingUri;
        this.threadsUri = threadsUri;
    }

    @Override
    protected void configureServlets() {
        if (jsonFactory != null) {
            bind(JsonFactory.class).annotatedWith(Names.named("MetricsServlet.JSON_FACTORY")).toInstance(jsonFactory);
        }
        bind(String.class).annotatedWith(Names.named("MetricsServlet.HEALTHCHECK_URI")).toInstance(healthcheckUri);
        bind(String.class).annotatedWith(Names.named("MetricsServlet.METRICS_URI")).toInstance(metricsUri);
        bind(String.class).annotatedWith(Names.named("MetricsServlet.PING_URI")).toInstance(pingUri);
        bind(String.class).annotatedWith(Names.named("MetricsServlet.THREADS_URI")).toInstance(threadsUri);
        bind(MetricsServlet.class).toProvider(MetricsServletProvider.class).asEagerSingleton();

        serve(healthcheckUri, metricsUri, pingUri, threadsUri).with(MetricsServlet.class);
    }
}
