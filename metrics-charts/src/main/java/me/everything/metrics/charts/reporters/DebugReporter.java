package me.everything.metrics.charts.reporters;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

import me.everything.metrics.charts.models.MetricSnapshotMultiTimeSeries;
import me.everything.metrics.snapshots.MetricSnapshot;
import me.everything.metrics.utils.TimeUtils;

import com.codahale.metrics.ConsoleReporter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;

public class DebugReporter extends MetricSnapshotReporter {
	
	private boolean mEnableTimeWindow = false;
	
    private DebugReporter(MetricRegistry registry,
				          TimeUnit rateUnit,
				          TimeUnit durationUnit,
				          MetricFilter filter,
				          long windowSizeMs,
				          boolean enableLogcat) {
		super(registry, "evDebugReporter", rateUnit, durationUnit, filter, enableLogcat);
		mLatestValues = new ConcurrentSkipListMap<String, MetricSnapshot>();
		mEnableTimeWindow = windowSizeMs > 0;
		if (mEnableTimeWindow) {
			mDataset = new MetricSnapshotMultiTimeSeries();
			mDataset.setWindowSize(windowSizeMs);
		} else {
			mDataset = null;
		}
	}
    
    private Map<String, MetricSnapshot> mLatestValues;
    private MetricSnapshotMultiTimeSeries mDataset;

    public MetricSnapshotMultiTimeSeries getDataset() {
    	return mDataset;
    }
    
	@Override
	protected int performReportMetric(MetricSnapshot snapshot) {
		mLatestValues.put(snapshot.name(), snapshot);
		if (mEnableTimeWindow) {
			return mDataset.add(snapshot);
		} else {
			return 1;
		}
	}
	
	@Override
    protected int onMetricsIterationComplete() {
		if (mEnableTimeWindow) {
			return mDataset.cullTimesOutsideWindow();
		} else {
			return 0;
		}
    }
	
	public Collection<MetricSnapshot> getLatestMetrics() {
		return mLatestValues.values();
	}
	
	public void clear() {
		mLatestValues.clear();
	}
	
    /**
     * A builder for {@link ConsoleReporter} instances. Defaults to using the default locale and
     * time zone, writing to {@code System.out}, converting rates to events/second, converting
     * durations to milliseconds, and not filtering metrics.
     */
    public static class Builder {
        protected final MetricRegistry registry;
        protected TimeUnit rateUnit;
        protected TimeUnit durationUnit;
        protected MetricFilter filter;
        protected boolean enableLogcat;
        protected long windowSizeMs;

        protected Builder(MetricRegistry registry) {
            this.registry = registry;
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
            this.enableLogcat = false;
            this.windowSizeMs = TimeUtils.MSECS_IN_SECOND * TimeUtils.SECONDS_IN_MINUTE;
        }

        /**
         * Convert rates to the given time unit.
         *
         * @param rateUnit a unit of time
         * @return {@code this}
         */
        public Builder convertRatesTo(TimeUnit rateUnit) {
            this.rateUnit = rateUnit;
            return this;
        }

        /**
         * Convert durations to the given time unit.
         *
         * @param durationUnit a unit of time
         * @return {@code this}
         */
        public Builder convertDurationsTo(TimeUnit durationUnit) {
            this.durationUnit = durationUnit;
            return this;
        }

        /**
         * Only report metrics which match the given filter.
         *
         * @param filter a {@link MetricFilter}
         * @return {@code this}
         */
        public Builder filter(MetricFilter filter) {
            this.filter = filter;
            return this;
        }
        
        public Builder enableReportToLogcat(boolean enable) {
        	this.enableLogcat = enable;
        	return this;
        }
        
        public Builder windowSize(long windowSizeMs) {
        	this.windowSizeMs = windowSizeMs;
        	return this;
        }
        
        public DebugReporter build() {
        	DebugReporter ret =  new DebugReporter(registry, rateUnit, durationUnit, filter, windowSizeMs, enableLogcat);
        	sReporterInstance = new WeakReference<DebugReporter>(ret);
        	return ret;
        }

        public static DebugReporter getReporterInstance() {
        	if (sReporterInstance.get() != null) {
        		return sReporterInstance.get();
        	}
        	return null;
        }
        
        private static WeakReference<DebugReporter> sReporterInstance = null;
    }
    
	/**
     * Returns a new {@link Builder} for {@link ConsoleReporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link ConsoleReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    public static DebugReporter getInstance() {
    	return Builder.getReporterInstance();
    }
    
   
}
