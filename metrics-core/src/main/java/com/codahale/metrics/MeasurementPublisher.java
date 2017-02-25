package com.codahale.metrics;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public interface MeasurementPublisher {
    MeasurementPublisher DO_NOT_PUBLISH = new DoNothingMeasurementPublisher();

    void addListener(MeasurementListener listener);

    void removeListener(MeasurementListener listener);

    void counterIncremented(String name, long n);

    void counterDecremented(String name, long n);

    void timerUpdated(String name, long duration, TimeUnit timeUnit);

    void histogramUpdated(String name, long value);

    void meterMarked(String name, long n);

    class DoNothingMeasurementPublisher implements MeasurementPublisher {

        private DoNothingMeasurementPublisher() {
        }

        @Override
        public void addListener(MeasurementListener listener) {

        }

        @Override
        public void removeListener(MeasurementListener listener) {

        }

        @Override
        public void counterIncremented(String name, long n) {

        }

        @Override
        public void counterDecremented(String name, long n) {

        }

        @Override
        public void timerUpdated(String name, long duration, TimeUnit timeUnit) {

        }

        @Override
        public void histogramUpdated(String name, long value) {

        }

        @Override
        public void meterMarked(String name, long n) {

        }
    }

    class DefaultMeasurementPublisher implements MeasurementPublisher {
        private Set<MeasurementListener> listeners = new HashSet<MeasurementListener>();

        DefaultMeasurementPublisher() {
        }

        @Override
        public void addListener(MeasurementListener listener) {
            listeners.add(listener);
        }

        @Override
        public void removeListener(MeasurementListener listener) {
            listeners.remove(listener);
        }

        @Override
        public void counterIncremented(String name, long n) {
            for (MeasurementListener listener: listeners) {
                listener.counterIncremented(name, n);
            }
        }

        @Override
        public void counterDecremented(String name, long n) {
            for (MeasurementListener listener: listeners) {
                listener.counterDecremented(name, n);
            }
        }

        @Override
        public void timerUpdated(String name, long duration, TimeUnit unit) {
            for (MeasurementListener listener: listeners) {
                listener.timerUpdated(name, duration, unit);
            }
        }

        @Override
        public void histogramUpdated(String name, long value) {
            for (MeasurementListener listener: listeners) {
                listener.histogramUpdated(name, value);
            }
        }

        @Override
        public void meterMarked(String name, long n) {
            for (MeasurementListener listener: listeners) {
                listener.meterMarked(name, n);
            }
        }
    }
}
