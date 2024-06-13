package io.dropwizard.metrics.jetty12.ee10;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.ResponseMeteredLevel;
import io.dropwizard.metrics.jetty12.AbstractInstrumentedHandler;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;
import org.eclipse.jetty.ee10.servlet.ServletApiRequest;
import org.eclipse.jetty.ee10.servlet.ServletChannelState;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletContextRequest;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;

import java.io.IOException;

/**
 * A Jetty {@link Handler} which records various metrics about an underlying {@link Handler}
 * instance. This {@link Handler} requires a {@link org.eclipse.jetty.ee10.servlet.ServletContextHandler} to be present.
 * For correct behaviour, the {@link org.eclipse.jetty.ee10.servlet.ServletContextHandler} should be before this handler
 * in the handler chain. To achieve this, one can use
 * {@link org.eclipse.jetty.ee10.servlet.ServletContextHandler#insertHandler(Singleton)}.
 */
public class InstrumentedEE10Handler extends AbstractInstrumentedHandler {
    private AsyncDispatchesAwareServletRequestListener asyncDispatchesAwareServletRequestListener;

    /**
     * Create a new instrumented handler using a given metrics registry.
     *
     * @param registry the registry for the metrics
     */
    public InstrumentedEE10Handler(MetricRegistry registry) {
        super(registry);
    }

    /**
     * Create a new instrumented handler using a given metrics registry.
     *
     * @param registry the registry for the metrics
     * @param prefix   the prefix to use for the metrics names
     */
    public InstrumentedEE10Handler(MetricRegistry registry, String prefix) {
        super(registry, prefix);
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
        asyncDispatchesAwareServletRequestListener = new AsyncDispatchesAwareServletRequestListener(getAsyncDispatches());
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
    }

    @Override
    protected void setupServletListeners(Request request, Response response) {
        ServletContextRequest servletContextRequest = Request.as(request, ServletContextRequest.class);
        if (servletContextRequest == null) {
            return;
        }

        ServletChannelState servletChannelState = servletContextRequest.getServletRequestState();
        // the ServletChannelState gets recycled after handling, so add a new listener for every request
        servletChannelState.addListener(new InstrumentedAsyncListener(getAsyncTimeouts()));

        ServletContextHandler servletContextHandler = servletContextRequest.getServletContextHandler();
        // addEventListener checks for duplicates, so we can try to add the listener for every request
        servletContextHandler.addEventListener(asyncDispatchesAwareServletRequestListener);
    }

    @Override
    protected boolean isSuspended(Request request, Response response) {
        ServletContextRequest servletContextRequest = Request.as(request, ServletContextRequest.class);
        if (servletContextRequest == null) {
            return false;
        }

        ServletChannelState servletChannelState = servletContextRequest.getServletRequestState();
        if (servletChannelState == null) {
            return false;
        }

        return servletChannelState.isSuspended();
    }

    private static class AsyncDispatchesAwareServletRequestListener implements ServletRequestListener {
        private final Meter asyncDispatches;

        private AsyncDispatchesAwareServletRequestListener(Meter asyncDispatches) {
            this.asyncDispatches = asyncDispatches;
        }

        @Override
        public void requestInitialized(ServletRequestEvent sre) {
            ServletRequest servletRequest = sre.getServletRequest();
            if (!(servletRequest instanceof ServletApiRequest)) {
                return;
            }

            ServletApiRequest servletApiRequest = (ServletApiRequest) servletRequest;

            ServletContextHandler.ServletRequestInfo servletRequestInfo = servletApiRequest.getServletRequestInfo();

            ServletChannelState servletChannelState = servletRequestInfo.getServletRequestState();

            // if the request isn't 'initial', the request was re-dispatched
            if (servletChannelState.isAsync() && !servletChannelState.isInitial()) {
                asyncDispatches.mark();
            }
        }
    }

    private static class InstrumentedAsyncListener implements AsyncListener {
        private final Meter asyncTimeouts;

        private InstrumentedAsyncListener(Meter asyncTimeouts) {
            this.asyncTimeouts = asyncTimeouts;
        }

        @Override
        public void onComplete(AsyncEvent event) throws IOException {}

        @Override
        public void onTimeout(AsyncEvent event) throws IOException {
            asyncTimeouts.mark();
        }

        @Override
        public void onError(AsyncEvent event) throws IOException {}

        @Override
        public void onStartAsync(AsyncEvent event) throws IOException {
            event.getAsyncContext().addListener(this);
        }
    }
}
