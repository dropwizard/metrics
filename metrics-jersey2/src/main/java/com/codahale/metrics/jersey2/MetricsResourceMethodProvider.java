package com.codahale.metrics.jersey2;

import org.glassfish.jersey.internal.util.PropertiesHelper;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.ResourceMethod;
import org.glassfish.jersey.server.monitoring.RequestEvent;

import javax.ws.rs.core.FeatureContext;
import java.lang.reflect.Method;

/**
 * This class, initialized by hand or by {@link MetricsFeature}, provides helper methods to
 * get references to the right resource metrics annotated method.
 */

public final class MetricsResourceMethodProvider {

    private boolean instrumentResourceByImplementation = false;

    public static final MetricsResourceMethodProvider INSTANCE = new MetricsResourceMethodProvider();

    private MetricsResourceMethodProvider() {
    }

    /**
     * Initialize itself by a {@link FeatureContext} instance.
     * @param context The {@link FeatureContext} instance.
     */

    public void initialize(FeatureContext context) {
        instrumentResourceByImplementation =
            PropertiesHelper.isProperty(
                context
                    .getConfiguration()
                    .getProperty(JerseyServerMetricsProperties.INSTRUMENT_RESOURCE_BY_IMPLEMENTATION)
            );
    }

    /**
     * Initialize itself by hand.
     * @param instrumentResourceByImplementation {@link true} to search for meters annotations on declaration classes, {@link false} to search for meters annotations on definition classes.
     */

    public void initialize(boolean instrumentResourceByImplementation) {
        this.instrumentResourceByImplementation = instrumentResourceByImplementation;
    }

    /**
     * Get the resource method by {@link MetricsResourceMethodProvider::instrumentResourceByImplementation}.
     * @param invocable {@link Invocable} instance.
     * @return Annotated resource method.
     */

    public Method getMethod(Invocable invocable) {
        return (
              instrumentResourceByImplementation
            ? invocable.getHandlingMethod()
            : invocable.getDefinitionMethod()
        );
    }

    /**
     * Get the resource method by {@link MetricsResourceMethodProvider::instrumentResourceByImplementation}.
     * @param resourceMethod {@link ResourceMethod} instance.
     * @return Annotated resource method.
     */

    public Method getMethod(ResourceMethod resourceMethod) {
        return getMethod(resourceMethod.getInvocable());
    }

    /**
     * Get the resource method by {@link MetricsResourceMethodProvider::instrumentResourceByImplementation}.
     * @param requestEvent {@link RequestEvent} instance.
     * @return Annotated resource method.
     */

    public Method getMethod(RequestEvent requestEvent) {
        final ResourceMethod resourceMethod = requestEvent.getUriInfo().getMatchedResourceMethod();

        if (null == resourceMethod) {
            return null;
        }

        return getMethod(
            resourceMethod.getInvocable()
        );
    }

}
