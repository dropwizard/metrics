package com.yammer.metrics.reporting;


import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.Clock;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Gauge;
import com.yammer.metrics.core.MetricName;
import com.yammer.metrics.core.MetricPredicate;
import com.yammer.metrics.core.MetricsRegistry;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import com.yammer.metrics.core.VirtualMachineMetrics;
import com.yammer.metrics.reporting.AbstractPollingReporter;
import com.yammer.metrics.reporting.SocketProvider;
import com.yammer.metrics.reporting.TsdbReporter;
import com.yammer.metrics.reporting.JmxReporter.GaugeMBean;
import com.yammer.metrics.reporting.tests.AbstractPollingReporterTest;

import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author wouterdb
 *
 *	http://HOST:4242/q?start=3m-ago&ignore=67&m=sum:com.yammer.metrics.reporting.ToTSDBTest.value&o=axis%20x1y2&m=sum:test.count.count{name=cte}&o=axis%20x1y2&m=sum:rate:test.count.count{name=rise}&o=&m=sum:test.time.instantMean&o=&m=sum:test.time.mean&o=&m=sum:10m-avg:test.time.instantMean&o=&yrange=[0:2.5]&y2range=[0:120]&wxh=1660x666&png
 */
public class ToTSDBTest extends Gauge<Long> implements Runnable  {
	
	public static void main(String[] args) {
		TsdbReporter.enable(2,TimeUnit.SECONDS, args[0], 4242);
		new Thread(new ToTSDBTest()).run();
	}

	@Override
	public void run() {
		MetricsRegistry reg = Metrics.defaultRegistry();
		reg.newGauge(new MetricName(getClass(), "gauge"), this);
		reg.newCounter(new MetricName("test","count","cte")).inc(75);
		
		Counter riser = reg.newCounter(new MetricName("test","count","rise"));
		Timer wait = reg.newTimer(new MetricName("test","time","wait"),TimeUnit.SECONDS,TimeUnit.SECONDS);
		
		long start = System.currentTimeMillis();
		while(true){
			TimerContext tcx = wait.time();
			riser.inc(2);
			start += 1000;
			long now=System.currentTimeMillis();
			try {
				long sleeptime = start-now+(long)(Math.random()*50);
				Thread.sleep(sleeptime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tcx.stop();
		}
		
	}

	@Override
	public Long getValue() {
		return (System.nanoTime()%100000000000L) / 1000000000L;
	}
   
}
