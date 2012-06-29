package com.yammer.metrics.core;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Base abstraction of a {@link Metric} that can be observed and have listeners
 * registered for change events. Most methods are protected or package protected
 * as they are meant to be called from the subclass or from the
 * {@link MetricListenersRegistry}.
 * 
 * @param <L>
 *        the specific {@link MetricListener} interface
 */
public abstract class ObservableMetric<L> {

    private final MetricName name;

    private final List<L> listeners;

    protected ObservableMetric(final MetricName name) {
        this.name = name;
        this.listeners = new CopyOnWriteArrayList<L>();
    }

    public MetricName getName() {
        return name;
    }

    protected Iterable<L> getListenersIterable() {
        return listeners;
    }

    void addListener(final L listener) {
        listeners.add(listener);
    }

    void removeAllListeners() {
        listeners.clear();
    }

    void removeListener(final L listener) {
        listeners.remove(listener);
    }
}
