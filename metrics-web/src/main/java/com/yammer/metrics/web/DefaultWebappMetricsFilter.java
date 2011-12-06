package com.yammer.metrics.web;

import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of the {@link WebappMetricsFilter} which provides a default set of response codes
 * to capture information about. <p>Use it in your web.xml like this:</p>
 * <pre>{@code
 * <filter>
 *     <filter-name>webappMetricsFilter</filter-name>
 *     <filter-class>com.yammer.metrics.web.DefaultWebappMetricsFilter</filter-class>
 * </filter>
 * <filter-mapping>
 *     <filter-name>webappMetricsFilter</filter-name>
 *     <url-pattern>/*</url-pattern>
 * </filter-mapping>
 * }</pre>
 */
public class DefaultWebappMetricsFilter extends WebappMetricsFilter {
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
    public DefaultWebappMetricsFilter() {
        super(createMeterNamesByStatusCode(), NAME_PREFIX + "other");
    }

    private static Map<Integer, String> createMeterNamesByStatusCode() {
        final Map<Integer, String> meterNamesByStatusCode = new HashMap<Integer, String>(6);
        meterNamesByStatusCode.put(OK, NAME_PREFIX + "ok");
        meterNamesByStatusCode.put(CREATED, NAME_PREFIX + "created");
        meterNamesByStatusCode.put(NO_CONTENT, NAME_PREFIX + "noContent");
        meterNamesByStatusCode.put(BAD_REQUEST, NAME_PREFIX + "badRequest");
        meterNamesByStatusCode.put(NOT_FOUND, NAME_PREFIX + "notFound");
        meterNamesByStatusCode.put(SERVER_ERROR, NAME_PREFIX + "serverError");
        return meterNamesByStatusCode;
    }
}
