package io.dropwizard.metrics.jetty12.ee10;

import io.dropwizard.metrics5.MetricRegistry;
import io.dropwizard.metrics5.annotation.ResponseMeteredLevel;
import io.dropwizard.metrics.jetty12.AbstractInstrumentedHandler;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import org.eclipse.jetty.ee10.servlet.AsyncContextState;
import org.eclipse.jetty.ee10.servlet.ServletApiRequest;
import org.eclipse.jetty.ee10.servlet.ServletApiResponse;
import org.eclipse.jetty.ee10.servlet.ServletChannelState;
import org.eclipse.jetty.ee10.servlet.ServletContextRequest;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static io.dropwizard.metrics5.annotation.ResponseMeteredLevel.COARSE;

/**
 * A Jetty {@link Handler} which records various metrics about an underlying {@link Handler}
 * instance. This {@link Handler} requires a {@link org.eclipse.jetty.ee10.servlet.ServletContextHandler} to be present.
 * For correct behaviour, the {@link org.eclipse.jetty.ee10.servlet.ServletContextHandler} should be before this handler
 * in the handler chain. To achieve this, one can use
 * {@link org.eclipse.jetty.ee10.servlet.ServletContextHandler#insertHandler(Singleton)}.
 */
public class InstrumentedEE10Handler extends AbstractInstrumentedHandler {
    private AsyncListener listener;

    /**
     * Create a new instrumented handler using a given metrics registry.
     *
     * @param registry the registry for the metrics
     */
    public InstrumentedEE10Handler(MetricRegistry registry) {
        super(registry, null);
    }

    /**
     * Create a new instrumented handler using a given metrics registry.
     *
     * @param registry the registry for the metrics
     * @param prefix   the prefix to use for the metrics names
     */
    public InstrumentedEE10Handler(MetricRegistry registry, String prefix) {
        super(registry, prefix, COARSE);
    }

    /**
     * Create a new instrumented handler using a given metrics registry.
     *
     * @param registry the registry for the metrics
     * @param prefix   the prefix to use for the metrics names
     * @param responseMeteredLevel the level to determine individual/aggregate response codes that are instrumented
     */
    public InstrumentedEE10Handler(MetricRegistry registry, String prefix, ResponseMeteredLevel responseMeteredLevel) {
        super(registry, prefix, responseMeteredLevel);
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();

        this.listener = new AsyncAttachingListener();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
    }

    @Override
    public boolean handle(Request request, Response response, Callback callback) throws Exception {
        ServletContextRequest servletContextRequest = Request.as(request, ServletContextRequest.class);

        // only handle servlet requests with the InstrumentedHandler
        // because it depends on the ServletRequestState
        if (servletContextRequest == null) {
            return super.handle(request, response, callback);
        }

        activeDispatches.inc();

        final long start;
        final ServletChannelState state = servletContextRequest.getServletRequestState();
        if (state.isInitial()) {
            // new request
            activeRequests.inc();
            start = Request.getTimeStamp(request);
            state.addListener(listener);
        } else {
            // resumed request
            start = System.currentTimeMillis();
            activeSuspended.dec();
            if (state.getState() == ServletChannelState.State.HANDLING) {
                asyncDispatches.mark();
            }
        }

        boolean handled = false;

        try {
            handled = super.handle(request, response, callback);
        } finally {
            final long now = System.currentTimeMillis();
            final long dispatched = now - start;

            activeDispatches.dec();
            dispatches.update(dispatched, TimeUnit.MILLISECONDS);

            if (state.isSuspended()) {
                activeSuspended.inc();
            } else if (state.isInitial()) {
                updateResponses(request, response, start, handled);
            }
            // else onCompletion will handle it.
        }

        return handled;
    }

    private class AsyncAttachingListener implements AsyncListener {

        @Override
        public void onTimeout(AsyncEvent event) throws IOException {}

        @Override
        public void onStartAsync(AsyncEvent event) throws IOException {
            event.getAsyncContext().addListener(new InstrumentedAsyncListener());
        }

        @Override
        public void onError(AsyncEvent event) throws IOException {}

        @Override
        public void onComplete(AsyncEvent event) throws IOException {}
    }

    private class InstrumentedAsyncListener implements AsyncListener {
        private final long startTime;

        InstrumentedAsyncListener() {
            this.startTime = System.currentTimeMillis();
        }

        @Override
        public void onTimeout(AsyncEvent event) throws IOException {
            asyncTimeouts.mark();
        }

        @Override
        public void onStartAsync(AsyncEvent event) throws IOException {
        }

        @Override
        public void onError(AsyncEvent event) throws IOException {
        }

        @Override
        public void onComplete(AsyncEvent event) throws IOException {
            final AsyncContextState state = (AsyncContextState) event.getAsyncContext();
            final ServletApiRequest request = (ServletApiRequest) state.getRequest();
            final ServletApiResponse response = (ServletApiResponse) state.getResponse();
            updateResponses(request.getRequest(), response.getResponse(), startTime, true);

            final ServletContextRequest servletContextRequest = Request.as(request.getRequest(), ServletContextRequest.class);
            final ServletChannelState servletRequestState = servletContextRequest.getServletRequestState();
            if (!servletRequestState.isSuspended()) {
                activeSuspended.dec();
            }
        }
    }
}
