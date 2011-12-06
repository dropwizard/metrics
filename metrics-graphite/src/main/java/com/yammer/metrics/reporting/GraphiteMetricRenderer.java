package com.yammer.metrics.reporting;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.yammer.metrics.core.MetricName;

public abstract class GraphiteMetricRenderer<MetricType> {
    protected final Logger logger;

    public GraphiteMetricRenderer() {
        this.logger = LoggerFactory.getLogger(getClass());
    }

    public abstract void renderMetric(MetricName metricName, MetricType metric, GraphiteRendererContext context);

    protected void sendToGraphite(String data, GraphiteRendererContext context) {
        try {
            context.writer.write(data);
        } catch (IOException e) {
            this.logger.error("Error sending to Graphite:", e);
        }
    }

    protected void printDoubleField(String name, double value, GraphiteRendererContext context) {
        sendToGraphite(String.format(context.locale, "%s%s %2.2f %d\n", context.prefix, sanitizeName(name), value, context.epoch), context);
    }

    protected void printLongField(String name, long value, GraphiteRendererContext context) {
        sendToGraphite(String.format(context.locale, "%s%s %d %d\n", context.prefix, sanitizeName(name), value, context.epoch), context);
    }

    protected String sanitizeName(MetricName name) {
        final StringBuilder sb = new StringBuilder().append(name.getGroup()).append('.').append(name.getType()).append('.');
        if (name.hasScope()) {
            sb.append(name.getScope()).append('.');
        }
        return sb.append(name.getName()).toString().replace(' ', '-');
    }

    protected String sanitizeName(String name) {
        return name.replace(' ', '-');
    }
}
