package com.yammer.metrics.jetty;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.yammer.metrics.core.CounterMetric;
import com.yammer.metrics.core.MeterMetric;
import com.yammer.metrics.core.Metrics;
import com.yammer.metrics.core.TimerMetric;
import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.continuation.ContinuationListener;
import org.eclipse.jetty.server.AsyncContinuation;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;

/**
 * A Jetty {@link Handler} which records various metrics about an underlying
 * {@link Handler} instance.
 *
 * @author coda
 */
public class InstrumentedHandler extends HandlerWrapper {
	private final TimerMetric dispatches;
	private final MeterMetric requests;
	private final MeterMetric resumes;
	private final MeterMetric suspends;
	private final MeterMetric expires;

	private final CounterMetric activeRequests;
	private final CounterMetric activeSuspendedRequests;
	private final CounterMetric activeDispatches;

	private final MeterMetric[] responses;

	private final ContinuationListener listener;

	/**
	 * Create a new instrumented handler.
	 *
	 * @param underlying the handler about which metrics will be collected
	 */
	public InstrumentedHandler(Handler underlying) {
		super();
		this.dispatches = Metrics.newTimer(underlying.getClass(), "dispatches", TimeUnit.MILLISECONDS, TimeUnit.SECONDS);
		this.requests = Metrics.newMeter(underlying.getClass(), "requests", "requests", TimeUnit.SECONDS);
		this.resumes = Metrics.newMeter(underlying.getClass(), "resumes", "requests", TimeUnit.SECONDS);
		this.suspends = Metrics.newMeter(underlying.getClass(), "suspends", "requests", TimeUnit.SECONDS);
		this.expires = Metrics.newMeter(underlying.getClass(), "expires", "requests", TimeUnit.SECONDS);

		this.activeRequests = Metrics.newCounter(underlying.getClass(), "active-requests");
		this.activeSuspendedRequests = Metrics.newCounter(underlying.getClass(), "active-suspended-requests");
		this.activeDispatches = Metrics.newCounter(underlying.getClass(), "active-dispatches");

		this.responses = new MeterMetric[]{
				Metrics.newMeter(underlying.getClass(), "1xx-reponses", "responses", TimeUnit.SECONDS), // 1xx
				Metrics.newMeter(underlying.getClass(), "2xx-reponses", "responses", TimeUnit.SECONDS), // 2xx
				Metrics.newMeter(underlying.getClass(), "3xx-reponses", "responses", TimeUnit.SECONDS), // 3xx
				Metrics.newMeter(underlying.getClass(), "4xx-reponses", "responses", TimeUnit.SECONDS), // 4xx
				Metrics.newMeter(underlying.getClass(), "5xx-reponses", "responses", TimeUnit.SECONDS)  // 5xx
		};

		this.listener = new ContinuationListener() {
			@Override
			public void onComplete(Continuation continuation) {
				expires.mark();
			}

			@Override
			public void onTimeout(Continuation continuation) {
				final Request request = ((AsyncContinuation) continuation).getBaseRequest();
				updateResponses(request);
				if (!continuation.isResumed()) {
					activeSuspendedRequests.dec();
				}
			}
		};

		setHandler(underlying);
	}

	@Override
	public void handle(String target, Request request,
					   HttpServletRequest httpRequest, HttpServletResponse httpResponse)
			throws IOException, ServletException {
		activeDispatches.inc();

		long startTime = 0L;
		final AsyncContinuation continuation = request.getAsyncContinuation();
		if (continuation.isInitial()) {
			activeRequests.inc();
			startTime = request.getTimeStamp();
		} else {
			startTime = System.currentTimeMillis();
			activeSuspendedRequests.dec();
			if (continuation.isResumed()) {
				resumes.mark();
			}
		}

		final long start = System.nanoTime();
		try {
			super.handle(target, request, httpRequest, httpResponse);
		} finally {
			dispatches.update(System.nanoTime() - start, TimeUnit.NANOSECONDS);
			activeDispatches.dec();
			if (continuation.isSuspended()) {
				if (continuation.isInitial()) {
					continuation.addContinuationListener(listener);
				}
				suspends.mark();
				activeSuspendedRequests.inc();
			} else if (continuation.isInitial()) {
				updateResponses(request);
			}
		}
	}

	private void updateResponses(Request request) {
		final int response = request.getResponse().getStatus() / 100;
		if (response >= 1 && response <= 5) {
			responses[response - 1].mark();
		}
		activeRequests.dec();
		requests.mark();
	}
}
