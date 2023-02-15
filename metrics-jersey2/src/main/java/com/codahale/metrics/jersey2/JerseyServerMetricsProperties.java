package com.codahale.metrics.jersey2;

import org.glassfish.jersey.internal.util.PropertiesClass;

/**
 * The Jersey Server extension properties for Metrics.
 */

@PropertiesClass
public final class JerseyServerMetricsProperties {

    /**
     * {@link true} to search for meters annotations on declaration classes, {@link false} to search for meters annotations on definition classes.
     */

    public static final String INSTRUMENT_RESOURCE_BY_IMPLEMENTATION = "jersey.config.server.metrics.instrument-resource-by-implementation";

}
