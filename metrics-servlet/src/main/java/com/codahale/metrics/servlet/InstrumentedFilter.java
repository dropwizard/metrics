package com.codahale.metrics.servlet;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the {@link AbstractInstrumentedFilter} which provides a default set of response codes
 * to capture information about. <p>Use it in your servlet.xml like this:<p>
 * <pre>{@code
 * <filter>
 *     <filter-name>instrumentedFilter</filter-name>
 *     <filter-class>com.codahale.metrics.servlet.InstrumentedFilter</filter-class>
 * </filter>
 * <filter-mapping>
 *     <filter-name>instrumentedFilter</filter-name>
 *     <url-pattern>/*</url-pattern>
 * </filter-mapping>
 * }</pre>
 */
public class InstrumentedFilter extends AbstractInstrumentedFilter {
    public static final String REGISTRY_ATTRIBUTE = InstrumentedFilter.class.getName() + ".registry";

    private static final String NAME_PREFIX = "responseCodes.";
    private static final int OK = 200;
    private static final int CREATED = 201;
    private static final int NO_CONTENT = 204;
    private static final int BAD_REQUEST = 400;
    private static final int NOT_FOUND = 404;
    private static final int SERVER_ERROR = 500;

    /**
     * Creates a new instance of the filter.
     */
    public InstrumentedFilter() {
        super(REGISTRY_ATTRIBUTE, createMeterNamesByStatusCode(), NAME_PREFIX + "other");
    }

    private static Map<Integer, String> createMeterNamesByStatusCode() {
        final Map<Integer, String> meterNamesByStatusCode = new HashMap<>(6);
        meterNamesByStatusCode.put(OK, NAME_PREFIX + "ok");
        meterNamesByStatusCode.put(CREATED, NAME_PREFIX + "created");
        meterNamesByStatusCode.put(NO_CONTENT, NAME_PREFIX + "noContent");
        meterNamesByStatusCode.put(BAD_REQUEST, NAME_PREFIX + "badRequest");
        meterNamesByStatusCode.put(NOT_FOUND, NAME_PREFIX + "notFound");
        meterNamesByStatusCode.put(SERVER_ERROR, NAME_PREFIX + "serverError");
        return meterNamesByStatusCode;
    }
}
