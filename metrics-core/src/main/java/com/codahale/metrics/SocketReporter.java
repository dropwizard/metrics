package com.codahale.metrics;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A reporter which outputs measurements to a {@link Socket}, 
 */
public class SocketReporter extends ScheduledReporter {
    /**
     * Returns a new {@link Builder} for {@link SocketReporter}.
     *
     * @param registry the registry to report
     * @return a {@link Builder} instance for a {@link SocketReporter}
     */
    public static Builder forRegistry(MetricRegistry registry) {
        return new Builder(registry);
    }

    /**
     * A builder for {@link SocketReporter} instances. Defaults to socket to {@code metrics}, 
     * converting rates to events/second, converting durations to milliseconds, and
     * not filtering metrics.
     */
    public static class Builder {
        private final MetricRegistry registry;
        private String host;
        private String port;
        private TimeZone timeZone;
        private TimeUnit rateUnit;
        private TimeUnit durationUnit;
        private MetricFilter filter;

        private Builder(MetricRegistry registry) {
            this.registry = registry;
            this.timeZone = TimeZone.getDefault();
            this.rateUnit = TimeUnit.SECONDS;
            this.durationUnit = TimeUnit.MILLISECONDS;
            this.filter = MetricFilter.ALL;
        }

        public Builder withHost(String host) {
            this.host = host;
            return this;
        }
        
        public Builder withPort(String port) {
            this.port = port;
            return this;
        }

        /**
         * Use the given {@link TimeZone} for the time.
         *
         * @param timeZone a {@link TimeZone}
         * @return {@code this}
         */
        public Builder formattedFor(TimeZone timeZone) {
            this.timeZone = timeZone;
            return this;
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

        /**
         * Builds a {@link ConsoleReporter} with the given properties.
         *
         * @return a {@link ConsoleReporter}
         */
        public SocketReporter build() {
            return new SocketReporter(registry,
            						   host,
            						   port,
                                       timeZone,
                                       rateUnit,
                                       durationUnit,
                                       filter);
        }
    }

    private final String host;
    private final String port;
    private Socket connection;
    private ObjectOutputStream output;
    
    private static final Logger LOGGER = LoggerFactory.getLogger(SocketReporter.class);
    
    private SocketReporter(MetricRegistry registry,
    						String host,
    						String port,
                            TimeZone timeZone,
                            TimeUnit rateUnit,
                            TimeUnit durationUnit,
                            MetricFilter filter) {
        super(registry, "socket-reporter", filter, rateUnit, durationUnit);
        this.host = host;
        this.port = port;
    }

    @Override
    public void report(SortedMap<String, Gauge> gauges,
                       SortedMap<String, Counter> counters,
                       SortedMap<String, Histogram> histograms,
                       SortedMap<String, Meter> meters,
                       SortedMap<String, Timer> timers) {
        
        try {
        	connection = new Socket(InetAddress.getByName(this.host), Integer.parseInt(port));
        	output = new ObjectOutputStream(connection.getOutputStream());
	        if (!gauges.isEmpty()) {
	            for (Map.Entry<String, Gauge> entry : gauges.entrySet()) {
	               writeGauge(entry.getKey(), entry.getValue());
	            }
	        }
	
	        if (!counters.isEmpty()) {
	            for (Map.Entry<String, Counter> entry : counters.entrySet()) {
	            	 writeCounter(entry.getKey(), entry.getValue());
	            }
	        }

	        if (!histograms.isEmpty()) {
	            for (Map.Entry<String, Histogram> entry : histograms.entrySet()) {
	            	 writeHistogram(entry.getKey(), entry.getValue());
	            }
	        }
	
	        if (!meters.isEmpty()) {
	            for (Map.Entry<String, Meter> entry : meters.entrySet()) {
	            	 writeMeter(entry.getKey(), entry.getValue());
	            }
	        }
	
	        if (!timers.isEmpty()) {
	            for (Map.Entry<String, Timer> entry : timers.entrySet()) {
	            	 writeTimer(entry.getKey(), entry.getValue());
	            }
	        }
	        output.flush();
        }catch(IOException e) {
        	LOGGER.error("Exception occurred while reporting " + e.getMessage());
        	e.printStackTrace();
        }finally {
        	if(output != null) {
        		try{
        			output.close();
        		}catch(IOException e) {}
        	}
        	if(connection != null) {
        		try{
        			connection.close();
        		}catch(IOException e) {}
        	}
        }
    }
    
    private void writeGauge(String key, Gauge gauge)  {
    	StringBuilder gaugeBuilder  = new StringBuilder(20);
    	
    	gaugeBuilder.append("[type=GAUGE")
    				  .append(", name=").append(key)
    				  .append(", value=").append(gauge.getValue())
    				  .append("]");
    	
    	write(gaugeBuilder.toString());
    }
    
    private void writeCounter(String key, Counter counter)  {
    	StringBuilder counterBuilder  = new StringBuilder(20);
    	
    	counterBuilder.append("[type=COUNTER")
    				  .append(", name=").append(key)
    				  .append(", count=").append(counter.getCount())
    				  .append("]");
    	
    	write(counterBuilder.toString());
    }
    
    private void writeHistogram(String key, Histogram histogram)  {
    	 final Snapshot snapshot = histogram.getSnapshot();
    	 StringBuilder histogramBuilder  = new StringBuilder(75);
    	 
    	 histogramBuilder.append("[type=HISTOGRAM")
    	 				 .append(", name=").append(key)
    	 				 .append(", count=").append(histogram.getCount())
    	 				 .append(", min=").append(snapshot.getMin())
    	 				 .append(", max=").append(snapshot.getMax())
    	 				 .append(", mean=").append(snapshot.getMean())
    	 				 .append(", stddev=").append(snapshot.getStdDev())
    	 				 .append(", median=").append(snapshot.getMedian())
    	 				 .append(", p75=").append(snapshot.get75thPercentile())
    	 				 .append(", p95=").append(snapshot.get95thPercentile())
    	 				 .append(", p98=").append(snapshot.get98thPercentile())
    	 				 .append(", p99=").append(snapshot.get99thPercentile())
    	 				 .append(", p999=").append(snapshot.get999thPercentile())
    	 				 .append("]");
    	 	
    	write(histogramBuilder.toString());
    }
    
    private void writeMeter(String key, Meter meter) {
    	StringBuilder meterBuilder = new StringBuilder(25);
    	
    	meterBuilder.append("[type=METER")
    				.append(", name=").append(key)
    				.append(", count=").append(meter.getCount())
    				.append(", mean_rate=").append(meter.getMeanRate())
    				.append(", m1=").append(meter.getOneMinuteRate())
    				.append(", m5=").append(meter.getFiveMinuteRate())
    				.append(", m15=").append(meter.getFifteenMinuteRate())
    				.append(", rate_unit=").append(getRateUnit())
    				.append("]");
    	
    	write(meterBuilder.toString());
    }
    
    private void writeTimer(String key, Timer timer) {
    	final Snapshot snapshot = timer.getSnapshot();
    	 StringBuilder timerBuilder  = new StringBuilder(100);
    	 
    	 timerBuilder.append("[type=TIMER")
			 .append(", name=").append(key)
			 .append(", count=").append(convertDuration(timer.getCount()))
			 .append(", min=").append(convertDuration(snapshot.getMin()))
			 .append(", max=").append(convertDuration(snapshot.getMax()))
			 .append(", mean=").append(convertDuration(snapshot.getMean()))
			 .append(", stddev=").append(convertDuration(snapshot.getStdDev()))
			 .append(", median=").append(convertDuration(snapshot.getMedian()))
			 .append(", p75=").append(convertDuration(snapshot.get75thPercentile()))
			 .append(", p95=").append(convertDuration(snapshot.get95thPercentile()))
			 .append(", p98=").append(convertDuration(snapshot.get98thPercentile()))
			 .append(", p99=").append(convertDuration(snapshot.get99thPercentile()))
			 .append(", p999=").append(convertDuration(snapshot.get999thPercentile()))
			 .append(", mean_rate=").append(timer.getMeanRate())
			 .append(", m1=").append(timer.getOneMinuteRate())
    		 .append(", m5=").append(timer.getFiveMinuteRate())
    		 .append(", m15=").append(timer.getFifteenMinuteRate())
    		 .append(", rate_unit=").append(getRateUnit())
    	     .append(", duration_unit=").append(getDurationUnit())
			 .append("]");

    	write(timerBuilder.toString());
    }
    
    private void write(String string) {
    	try{
    		output.writeObject(string);
    	}catch(IOException e) {
    		LOGGER.error("Writing to outputstream failed due to " + e.getMessage());
    		e.printStackTrace();
    	}
    }
    
}
