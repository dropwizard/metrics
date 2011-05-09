package com.yammer.metrics.reporting;

import com.yammer.metrics.util.Utils;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * A servlet context listener which shuts down the various thread pools when
 * the context is destroyed.
 */
public class MetricServletContextListener implements ServletContextListener {
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Utils.shutdownThreadPools();
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // nothing needs to happen
    }
}
