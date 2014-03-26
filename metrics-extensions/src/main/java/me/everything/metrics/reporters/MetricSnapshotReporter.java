package me.everything.metrics.reporters;

import java.io.Closeable;
import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import me.everything.metrics.logging.Log;
import me.everything.metrics.snapshots.CounterSnapshot;
import me.everything.metrics.snapshots.GaugeSnapshot;
import me.everything.metrics.snapshots.HistogramSnapshot;
import me.everything.metrics.snapshots.MeterSnapshot;
import me.everything.metrics.snapshots.MetricSnapshot;
import me.everything.metrics.snapshots.TimerSnapshot;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.Histogram;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricFilter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Reporter;
import com.codahale.metrics.Timer;

/**
 * A reporter which outputs measurements to a {@link PrintStream}, like {@code System.out}.
 */
public abstract class MetricSnapshotReporter implements Closeable, Reporter {

	private static final String TAG = Log.makeLogTag(MetricSnapshotReporter.class);
	private boolean mReportToLogcat;
	
	protected final MetricRegistry mRegistry;
	protected final MetricFilter mFilter; 
	
	protected final String mReporterName;
	
	public interface IListener {
		public void onMetricsReport(int modifications);
	}
	
	private LinkedHashSet<IListener> mListeners;
	
	public void addListener(IListener listener) {
		mListeners.add(listener);
	}
	
	public void removeListener(IListener listener) {
		mListeners.remove(listener);
	}
	
	public boolean getLogcatEnabled() {
		return mReportToLogcat;
	}
	
	public void setLogcatEnabled(boolean enabled) {
		mReportToLogcat = enabled;
	}

	
    /**
     * A simple named thread factory.
     */
    private static class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        private NamedThreadFactory(String name) {
            this.namePrefix = "metrics-" + name + "-thread-";
        }

        @Override
        public Thread newThread(Runnable r) {
            final Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            t.setDaemon(true);
            if (t.getPriority() != Thread.MIN_PRIORITY) {
                t.setPriority(Thread.MIN_PRIORITY);
            }
            return t;
        }
    }

    private static final AtomicInteger FACTORY_ID = new AtomicInteger();

    private final ScheduledExecutorService mExecutor;
    private final double mDurationFactor;
    private final String mDurationUnit;
    private final double mRateFactor;
    private final String mRateUnit;

    public void startRandomInitialDelay(long period, TimeUnit unit) {
    	Random r = new Random();
    	long initialDelay = r.nextLong();
    	initialDelay = initialDelay % period;
    	start(initialDelay, period, unit);
    }
    
    public void start(long period, TimeUnit unit) {
    	start(period, period, unit);
    }
    
    /**
     * Starts the reporter polling at the given period.
     *
     * @param period the amount of time between polls
     * @param unit   the unit for {@code period}
     */
    public void start(long initialDelay, long period, TimeUnit unit) {
    	Log.d(TAG, "Starting reporter " + mReporterName + " with period=" + period + ", initialDelay=" + initialDelay + ", unit=" + unit);
        mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
            	try {
            		report();
            	} catch (Exception ex) {
            		Log.e(TAG,  "Error in reporting from " + mReporterName, ex);
            	}
            }
        }, initialDelay, period, unit);
    }

    /**
     * Stops the reporter and shuts down its thread of execution.
     */
    public void stop() {
    	Log.d(TAG, "Stopping reporter " + mReporterName);
        mExecutor.shutdown();
        try {
            mExecutor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException ignored) {
            // do nothing
        }
    }

    /**
     * Stops the reporter and shuts down its thread of execution.
     */
    @Override
    public void close() {
        stop();
    }

    /**
     * Report the current values of all metrics in the registry.
     */
    public void report() {
        report(
        	mRegistry.getGauges(mFilter),
        	mRegistry.getCounters(mFilter),
        	mRegistry.getHistograms(mFilter),
        	mRegistry.getMeters(mFilter),
        	mRegistry.getTimers(mFilter));
    }

    protected String getRateUnit() {
        return mRateUnit;
    }

    protected String getDurationUnit() {
        return mDurationUnit;
    }

    protected double convertDuration(double duration) {
        return duration * mDurationFactor;
    }

    protected double convertRate(double rate) {
        return rate * mRateFactor;
    }

    private String calculateRateUnit(TimeUnit unit) {
        final String s = unit.toString().toLowerCase(Locale.US);
        return s.substring(0, s.length() - 1);
    }
	
    protected MetricSnapshotReporter(MetricRegistry registry,
    						String name,
                            TimeUnit rateUnit,
                            TimeUnit durationUnit,
                            MetricFilter filter, 
                            boolean enableLogcat) {
        mReportToLogcat = enableLogcat;
        mRegistry = registry;
        mFilter = filter;
        mReporterName = name; 
        
        mExecutor = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory(name + '-' + FACTORY_ID.incrementAndGet()));
        mRateFactor = rateUnit.toSeconds(1);
        mRateUnit = calculateRateUnit(rateUnit);
        mDurationFactor = 1.0 / durationUnit.toNanos(1);
        mDurationUnit = durationUnit.toString().toLowerCase(Locale.US);
        
        mListeners = new LinkedHashSet<MetricSnapshotReporter.IListener>();
    }

    @SuppressWarnings("rawtypes")
	public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {

    	int metricsCount = 0;
    	int modifications = 0;
    	
        Log.v(TAG, mReporterName + ": " + "Begin metrics report");
    	
        if (!gauges.isEmpty()) {
            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
            	GaugeSnapshot snap = new GaugeSnapshot(entry.getKey(), entry.getValue());
            	modifications += reportMetric(snap);
                metricsCount++;
            }
        }
        
        if (!counters.isEmpty()) {
            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
            	CounterSnapshot snap = new CounterSnapshot(entry.getKey(), entry.getValue());
            	modifications += reportMetric(snap);
                metricsCount++;
            }
        }
        
        if (!histograms.isEmpty()) {
            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                HistogramSnapshot snap = new HistogramSnapshot(entry.getKey(), entry.getValue());
                modifications += reportMetric(snap);
                metricsCount++;
            }
        }
        
        if (!meters.isEmpty()) {
            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                MeterSnapshot snap = new MeterSnapshot(entry.getKey(), entry.getValue(), mRateUnit, mRateFactor);
                modifications += reportMetric(snap);
                metricsCount++;
            }
        }

        if (!timers.isEmpty()) {
            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                TimerSnapshot snap = new TimerSnapshot(entry.getKey(), entry.getValue(), mRateUnit, mDurationUnit, mRateFactor, mDurationFactor);
                modifications += reportMetric(snap);
                metricsCount++;
            }
        }
        
        modifications += onMetricsIterationComplete();
        
        Log.v(TAG, mReporterName + ": " + "End metrics report (" + metricsCount + " total, " + modifications  + " modifications)");
        
        for (IListener listener : mListeners) {
        	try {
        		listener.onMetricsReport(modifications);
        	} catch (Exception ex) {
        		Log.e(TAG, "MetricsReporter Listener error", ex);
        	}
        }
        
        Log.v(TAG, mReporterName + ": " + "Invoked all " + mListeners.size() + " listeners");
    }
    
    protected int onMetricsIterationComplete() {
    	return 0;
    }

    private int reportMetric(MetricSnapshot snapshot) {
    	if (mReportToLogcat) {
    		Log.v(TAG, mReporterName + ": " + snapshot.toString() + ": " + snapshot.dataToString());
    	}
    	return performReportMetric(snapshot);
    }
    
    protected abstract int performReportMetric(MetricSnapshot snapshot);
  
    @SuppressWarnings("rawtypes")
	public SortedMap<String, MetricSnapshot> getMetrics() {
    	SortedMap<String, MetricSnapshot> metrics = new TreeMap<String, MetricSnapshot>();
    	
    	SortedMap<String, Gauge> gauges = mRegistry.getGauges(mFilter);
        SortedMap<String, Counter> counters = mRegistry.getCounters(mFilter);
        SortedMap<String, Histogram> histograms = mRegistry.getHistograms(mFilter);
        SortedMap<String, Meter> meters = mRegistry.getMeters(mFilter);
        SortedMap<String, Timer> timers = mRegistry.getTimers(mFilter);
    	
        String rateUnit = getRateUnit();
    	String durationUnit = getDurationUnit();
        double rateFactor = convertRate(1);
        double durationFactor = convertDuration(1);
    	
        if (!gauges.isEmpty()) {
            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
            	GaugeSnapshot snap = new GaugeSnapshot(entry.getKey(), entry.getValue());
        		metrics.put(entry.getKey(), snap);
            }
        }

        if (!counters.isEmpty()) {
            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
            	CounterSnapshot snap = new CounterSnapshot(entry.getKey(), entry.getValue());
            	metrics.put(entry.getKey(), snap);
            }
        }

        if (!histograms.isEmpty()) {
            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
                HistogramSnapshot snap = new HistogramSnapshot(entry.getKey(), entry.getValue());
                metrics.put(entry.getKey(), snap);
            }
        }

        if (!meters.isEmpty()) {
            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
                MeterSnapshot snap = new MeterSnapshot(entry.getKey(), entry.getValue(), rateUnit, rateFactor);
                metrics.put(entry.getKey(), snap);
            }
        }

        if (!timers.isEmpty()) {
            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
                TimerSnapshot snap = new TimerSnapshot(entry.getKey(), entry.getValue(), rateUnit, durationUnit, rateFactor, durationFactor);
                metrics.put(entry.getKey(), snap);
            }
        }
        
        return metrics;
    }
       
}
